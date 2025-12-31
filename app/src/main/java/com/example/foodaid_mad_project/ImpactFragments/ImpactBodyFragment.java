package com.example.foodaid_mad_project.ImpactFragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodaid_mad_project.Model.FoodItem;
import com.example.foodaid_mad_project.R;
import com.example.foodaid_mad_project.Utils.ImageUtil;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * ImpactBodyFragment
 *
 * Displays the core content of the Impact screen.
 * Features:
 * - Statistics for Items Claimed, Donated, and Weight Saved.
 * - Dynamic Charts (Bar for Week, Line for Month, Pie for Year).
 * - Scrollable list of recent contributions (Donations & Claims).
 * - Time range navigation (Previous/Next).
 */
public class ImpactBodyFragment extends Fragment {

    public static final String MODE_WEEK = "WEEK";
    public static final String MODE_MONTH = "MONTH";
    public static final String MODE_YEAR = "YEAR";

    private String currentMode = MODE_WEEK;
    private Calendar displayedDate;

    // Data
    private List<FoodItem> claimedItems = new ArrayList<>();
    private List<FoodItem> donatedItems = new ArrayList<>();
    private List<FoodItem> allItems = new ArrayList<>(); // Unified list
    private List<DocumentSnapshot> rawClaims = new ArrayList<>();
    private List<DocumentSnapshot> rawLegacy = new ArrayList<>();
    private List<DocumentSnapshot> rawMyDonations = new ArrayList<>();

    // Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ListenerRegistration lrClaims, lrLegacy, lrMyDonations;
    private ImpactCalculator calculator = new ImpactCalculator();

    // UI Elements
    private RecyclerView rvContributions;
    private View headerView; // The header inserted into RecyclerView

    // Header Components
    private TextView tvDateRange, tvContributionsHeader, tvStatClaimedValue, tvStatClaimedDesc,
            tvStatDonatedValue, tvStateDonatedDesc, tvStatSavedValue, tvMonthYear;
    private ImageButton btnPrevDate, btnNextDate;
    private BarChart chartWeek;
    private LineChart chartMonth;
    private PieChart chartYear;
    private LinearLayout headerMonthYear;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_impact_body, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        displayedDate = Calendar.getInstance();

        // 1. Setup RecyclerView
        rvContributions = view.findViewById(R.id.rvContributions);
        rvContributions.setLayoutManager(new LinearLayoutManager(getContext()));

        // 2. Initialize Header View & Components
        initHeaderView();

        // 3. Setup Listeners & Logic
        setupFirestoreListeners();
        setupDateNavigation();
        loadStatsFromCache(); // Fast load previous state
    }

    private void initHeaderView() {
        // Inflate header layout (not attached yet)
        headerView = LayoutInflater.from(getContext()).inflate(R.layout.item_impact_header, rvContributions, false);

        btnPrevDate = headerView.findViewById(R.id.btnPrevDate);
        btnNextDate = headerView.findViewById(R.id.btnNextDate);
        tvDateRange = headerView.findViewById(R.id.tvDateRange);

        chartWeek = headerView.findViewById(R.id.chartWeek);
        chartMonth = headerView.findViewById(R.id.chartMonth);
        chartYear = headerView.findViewById(R.id.chartYear);

        tvStatClaimedValue = headerView.findViewById(R.id.tvStatClaimedValue);
        tvStatClaimedDesc = headerView.findViewById(R.id.tvStatClaimedDesc);
        tvStatDonatedValue = headerView.findViewById(R.id.tvStatDonatedValue);
        tvStateDonatedDesc = headerView.findViewById(R.id.tvStateDonatedDesc);
        tvStatSavedValue = headerView.findViewById(R.id.tvStatSavedValue);

        tvContributionsHeader = headerView.findViewById(R.id.tvContributionsHeader);
        tvMonthYear = headerView.findViewById(R.id.tvMonthYear);
        headerMonthYear = headerView.findViewById(R.id.header_Month_Year);
    }

    private void setupFirestoreListeners() {
        if (auth.getCurrentUser() == null)
            return;
        String userId = auth.getCurrentUser().getUid();

        // A. Listen for Claims (Where user is claimer)
        if (lrClaims != null)
            lrClaims.remove();
        lrClaims = db.collection("claims").whereEqualTo("claimerId", userId)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null)
                        return;
                    rawClaims = snapshots.getDocuments();
                    processData();
                });

        // B. Listen for Legacy Claims (Old format via Donations collection)
        if (lrLegacy != null)
            lrLegacy.remove();
        lrLegacy = db.collection("donations")
                .whereEqualTo("claimedBy", userId)
                .whereEqualTo("status", "CLAIMED")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null)
                        return;
                    rawLegacy = snapshots.getDocuments();
                    processData();
                });

        // C. Listen for My Donations
        if (lrMyDonations != null)
            lrMyDonations.remove();
        lrMyDonations = db.collection("donations").whereEqualTo("donatorId", userId)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null)
                        return;
                    rawMyDonations = snapshots.getDocuments();
                    processData();
                });
    }

    /**
     * Consolidates raw snapshots into mapped FoodItem objects and updates UI.
     */
    private void processData() {
        claimedItems.clear();
        donatedItems.clear();
        allItems.clear();
        List<String> processedDonationIds = new ArrayList<>();

        // 1. Process New Claims
        for (DocumentSnapshot doc : rawClaims) {
            FoodItem item = new FoodItem();
            item.setTitle(doc.getString("foodTitle") != null ? doc.getString("foodTitle") : "Claimed Item");
            Object img = doc.get("foodImage");
            if (img instanceof String)
                item.setImageUri((String) img);
            item.setLocationName(doc.getString("location"));

            Double w = doc.getDouble("weight");
            item.setWeight(w != null ? w : 0.0);
            item.setCategory(doc.getString("category"));

            Long ts = doc.getLong("timestamp");
            item.setTimestamp(ts != null ? ts : 0);

            // Use claimed quantity specifically
            Long q = doc.getLong("quantityClaimed");
            item.setQuantity(q != null ? q.intValue() : 1);

            claimedItems.add(item);

            // Track ID to avoid duplication with legacy query if data overlaps
            // (Assuming parent logic for deep reference, otherwise logic is safe)
            if (doc.getReference().getParent().getParent() != null) {
                processedDonationIds.add(doc.getReference().getParent().getParent().getId());
            }
        }

        // 2. Process Legacy Claims (Skip if ID matches known parent donation)
        for (DocumentSnapshot doc : rawLegacy) {
            if (!processedDonationIds.contains(doc.getId())) {
                claimedItems.add(doc.toObject(FoodItem.class));
            }
        }
        Collections.sort(claimedItems, (o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));

        // 3. Process My Donations
        for (DocumentSnapshot doc : rawMyDonations) {
            FoodItem item = doc.toObject(FoodItem.class);
            if (item != null) {
                // Ensure timestamp exists
                if (item.getTimestamp() == 0) {
                    Long ts = doc.getLong("timestamp"); // Try field
                    if (ts == null || ts == 0)
                        ts = doc.getLong("endTime"); // Fallback
                    if (ts == null || ts == 0)
                        ts = doc.getLong("startTime");
                    if (ts != null)
                        item.setTimestamp(ts);
                }
                // Ensure title
                if (item.getTitle() == null || item.getTitle().isEmpty()) {
                    String desc = doc.getString("description");
                    item.setTitle(desc != null ? desc : "Donation");
                }
                // Use initial quantity for history
                if (item.getInitialQuantity() > 0) {
                    item.setQuantity(item.getInitialQuantity());
                }
                donatedItems.add(item);
            }
        }

        // 4. Merge
        allItems.addAll(claimedItems);
        allItems.addAll(donatedItems);
        Collections.sort(allItems, (o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));

        checkBadges();
        refreshStats();
    }

    private void checkBadges() {
        if (auth.getCurrentUser() == null)
            return;
        double totalKg = calculator.getAllTimeWeight(claimedItems);
        List<String> newBadges = new ArrayList<>();

        if (totalKg >= 10)
            newBadges.add("badge_10kg");
        if (totalKg >= 50)
            newBadges.add("badge_50kg");
        if (totalKg >= 100)
            newBadges.add("badge_100kg");

        if (!newBadges.isEmpty()) {
            db.collection("users").document(auth.getCurrentUser().getUid())
                    .update("earnedBadges", FieldValue.arrayUnion(newBadges.toArray()))
                    .addOnFailureListener(e -> Log.e("Impact", "Badge Update Failed", e));
        }
    }

    private void setupDateNavigation() {
        btnPrevDate.setOnClickListener(v -> {
            adjustDate(-1);
            refreshStats();
        });
        btnNextDate.setOnClickListener(v -> {
            adjustDate(1);
            refreshStats();
        });
    }

    private void adjustDate(int amount) {
        switch (currentMode) {
            case MODE_WEEK:
                displayedDate.add(Calendar.WEEK_OF_YEAR, amount);
                break;
            case MODE_MONTH:
                displayedDate.add(Calendar.MONTH, amount);
                break;
            case MODE_YEAR:
                displayedDate.add(Calendar.YEAR, amount);
                break;
        }
    }

    public void updateViewMode(String mode) {
        this.currentMode = mode;
        if (tvDateRange != null) {
            displayedDate = Calendar.getInstance(); // Reset to today
            refreshStats();
        }
    }

    private void refreshStats() {
        if (getContext() == null)
            return;

        Calendar calSnapshot = (Calendar) displayedDate.clone();
        long startTime = 0, endTime = 0;
        String dateRangeText = "";
        SimpleDateFormat sdfDayMonth = new SimpleDateFormat("dd MMM", Locale.getDefault());
        SimpleDateFormat sdfFull = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

        // Calculate Range based on Mode
        switch (currentMode) {
            case MODE_WEEK:
                calSnapshot.set(Calendar.DAY_OF_WEEK, calSnapshot.getFirstDayOfWeek());
                setStartOfDay(calSnapshot);
                startTime = calSnapshot.getTimeInMillis();

                calSnapshot.add(Calendar.DAY_OF_YEAR, 6);
                setEndOfDay(calSnapshot);
                endTime = calSnapshot.getTimeInMillis();

                dateRangeText = sdfDayMonth.format(new Date(startTime)) + " - " + sdfFull.format(new Date(endTime));
                updateChartsVisibility(View.VISIBLE, View.GONE, View.GONE);
                tvContributionsHeader.setText("Contribution (Week)");
                setupBarChart(startTime, endTime);
                break;

            case MODE_MONTH:
                calSnapshot.set(Calendar.DAY_OF_MONTH, 1);
                setStartOfDay(calSnapshot);
                startTime = calSnapshot.getTimeInMillis();

                calSnapshot.set(Calendar.DAY_OF_MONTH, calSnapshot.getActualMaximum(Calendar.DAY_OF_MONTH));
                setEndOfDay(calSnapshot);
                endTime = calSnapshot.getTimeInMillis();

                dateRangeText = new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(new Date(startTime));
                updateChartsVisibility(View.GONE, View.VISIBLE, View.GONE);
                tvContributionsHeader.setText("Contribution (Month)");
                setupLineChart(startTime, endTime);
                break;

            case MODE_YEAR:
                calSnapshot.set(Calendar.DAY_OF_YEAR, 1);
                setStartOfDay(calSnapshot);
                startTime = calSnapshot.getTimeInMillis();

                calSnapshot.set(Calendar.MONTH, 11);
                calSnapshot.set(Calendar.DAY_OF_MONTH, 31);
                setEndOfDay(calSnapshot);
                endTime = calSnapshot.getTimeInMillis();

                dateRangeText = new SimpleDateFormat("yyyy", Locale.getDefault()).format(new Date(startTime));
                updateChartsVisibility(View.GONE, View.GONE, View.VISIBLE);
                tvContributionsHeader.setText("Contribution (Year)");
                setupPieChart(startTime, endTime);
                break;
        }

        tvDateRange.setText(dateRangeText);
        headerMonthYear.setVisibility(currentMode.equals(MODE_WEEK) ? View.GONE : View.VISIBLE);
        tvMonthYear.setText(currentMode.equals(MODE_WEEK) ? "" : (currentMode.equals(MODE_MONTH) ? "Week" : "Month"));

        // Update Text Stats
        int claimedCount = calculator.getItemCountForRange(claimedItems, startTime, endTime);
        int donatedCount = calculator.getItemCountForRange(donatedItems, startTime, endTime);
        double savedWeight = calculator.getWeightForRange(claimedItems, startTime, endTime);

        tvStatClaimedValue.setText(String.valueOf(claimedCount));
        tvStatDonatedValue.setText(String.valueOf(donatedCount));
        tvStatSavedValue.setText(String.format(Locale.getDefault(), "%.1f kg", savedWeight));

        tvStatClaimedDesc.setText(getString(R.string.Items_Claimed, currentMode.toLowerCase()));
        tvStateDonatedDesc.setText(getString(R.string.Items_Donated, currentMode.toLowerCase()));

        saveStatsToCache(String.valueOf(claimedCount), String.valueOf(donatedCount),
                String.format(Locale.getDefault(), "%.1f kg", savedWeight));

        // Update List Adapter
        List<FoodItem> filteredItems = new ArrayList<>();
        for (FoodItem item : allItems) {
            if (item.getTimestamp() >= startTime && item.getTimestamp() <= endTime) {
                filteredItems.add(item);
            }
        }
        String currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
        rvContributions.setAdapter(new ImpactAdapter(filteredItems, headerView, currentUserId));
    }

    private void updateChartsVisibility(int week, int month, int year) {
        chartWeek.setVisibility(week);
        chartMonth.setVisibility(month);
        chartYear.setVisibility(year);
    }

    private void setStartOfDay(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    private void setEndOfDay(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
    }

    // --- Caching for Fast Load ---
    private void saveStatsToCache(String claimed, String donated, String saved) {
        if (getContext() == null || auth.getCurrentUser() == null)
            return;
        getActivity().getSharedPreferences("ImpactStats_" + auth.getCurrentUser().getUid(), Context.MODE_PRIVATE)
                .edit()
                .putString("claimed", claimed)
                .putString("donated", donated)
                .putString("saved", saved)
                .apply();
    }

    private void loadStatsFromCache() {
        if (getContext() == null || auth.getCurrentUser() == null)
            return;
        android.content.SharedPreferences prefs = getActivity()
                .getSharedPreferences("ImpactStats_" + auth.getCurrentUser().getUid(), Context.MODE_PRIVATE);
        tvStatClaimedValue.setText(prefs.getString("claimed", "0"));
        tvStatDonatedValue.setText(prefs.getString("donated", "0"));
        tvStatSavedValue.setText(prefs.getString("saved", "0.0 kg"));
    }

    // ================= Charts =================

    private void setupBarChart(long startOfWeek, long endOfWeek) {
        if (allItems == null || allItems.isEmpty()) {
            chartWeek.clear();
            return;
        }
        float[] dailyWeights = new float[7];
        for (FoodItem item : allItems) {
            long ts = item.getTimestamp();
            if (ts >= startOfWeek && ts <= endOfWeek) {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(ts);
                int index = c.get(Calendar.DAY_OF_WEEK) - 1; // 0=Sun to 6=Sat
                if (index >= 0 && index < 7)
                    dailyWeights[index] += (float) item.getWeight();
            }
        }
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < 7; i++)
            entries.add(new BarEntry(i + 1, dailyWeights[i]));

        BarDataSet set = new BarDataSet(entries, "Saved (kg)");
        set.setColor(getResources().getColor(R.color.teal_200));
        set.setValueTextSize(10f);

        BarData data = new BarData(set);
        data.setBarWidth(0.9f);
        chartWeek.setData(data);
        chartWeek.getDescription().setEnabled(false);
        chartWeek.setFitBars(true);
        chartWeek.invalidate();
    }

    private void setupLineChart(long startOfMonth, long endOfMonth) {
        if (allItems == null) {
            chartMonth.clear();
            return;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(startOfMonth);
        int maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        float[] dailyWeights = new float[maxDays + 1];

        for (FoodItem item : allItems) {
            long ts = item.getTimestamp();
            if (ts >= startOfMonth && ts <= endOfMonth) {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(ts);
                int day = c.get(Calendar.DAY_OF_MONTH);
                if (day >= 1 && day <= maxDays)
                    dailyWeights[day] += (float) item.getWeight();
            }
        }
        List<Entry> entries = new ArrayList<>();
        for (int i = 1; i <= maxDays; i++)
            entries.add(new Entry(i, dailyWeights[i]));

        LineDataSet set = new LineDataSet(entries, "Saved (kg)");
        set.setColor(getResources().getColor(R.color.teal_200));
        set.setLineWidth(2.5f);
        set.setDrawValues(false);
        set.setDrawFilled(true);
        set.setFillColor(getResources().getColor(R.color.teal_200));
        set.setFillAlpha(50);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData data = new LineData(set);
        chartMonth.setData(data);
        chartMonth.getDescription().setEnabled(false);
        chartMonth.getXAxis().setDrawGridLines(false);
        chartMonth.getAxisRight().setEnabled(false);
        chartMonth.invalidate();
    }

    private void setupPieChart(long startOfYear, long endOfYear) {
        int claimedCount = calculator.getItemCountForRange(claimedItems, startOfYear, endOfYear);
        int donatedCount = calculator.getItemCountForRange(donatedItems, startOfYear, endOfYear);

        if (claimedCount == 0 && donatedCount == 0) {
            chartYear.setCenterText("No Activity");
            chartYear.setData(null);
            chartYear.invalidate();
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        if (claimedCount > 0) {
            entries.add(new PieEntry(claimedCount, "Claimed"));
            colors.add(getResources().getColor(R.color.teal_200));
        }
        if (donatedCount > 0) {
            entries.add(new PieEntry(donatedCount, "Donated"));
            colors.add(android.graphics.Color.parseColor("#FF9800"));
        }

        PieDataSet set = new PieDataSet(entries, "");
        set.setColors(colors);
        set.setValueTextSize(14f);
        set.setValueTextColor(android.graphics.Color.WHITE);

        PieData data = new PieData(set);
        chartYear.setData(data);
        chartYear.getDescription().setEnabled(false);
        chartYear.setCenterText("");
        chartYear.invalidate();
    }

    // ================= Adapter =================

    private static class ImpactAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_HEADER = 0;
        private static final int TYPE_ITEM = 1;

        List<FoodItem> data;
        View headerView;
        String currentUserId;

        public ImpactAdapter(List<FoodItem> data, View headerView, String userId) {
            this.data = data;
            this.headerView = headerView;
            this.currentUserId = userId;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_HEADER) {
                if (headerView.getParent() != null)
                    ((ViewGroup) headerView.getParent()).removeView(headerView);
                return new RecyclerView.ViewHolder(headerView) {
                };
            }
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_week_contribution, parent,
                    false);
            return new ItemViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (getItemViewType(position) == TYPE_HEADER)
                return;

            FoodItem item = data.get(position - 1);
            ItemViewHolder h = (ItemViewHolder) holder;

            h.tvItemName.setText(item.getTitle());
            h.tvItemDate.setText(
                    new SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(new Date(item.getTimestamp())));
            h.tvWeight.setText(String.format(Locale.getDefault(), "%.1f kg", item.getWeight()));

            boolean isMyDonation = item.getDonatorId() != null && item.getDonatorId().equals(currentUserId);
            h.tvStatus.setText(isMyDonation ? "Donated" : "Saved");

            // Image Loading
            String imgUrl = item.getImageUri();
            if (imgUrl != null && !imgUrl.isEmpty()) {
                if (imgUrl.startsWith("http")) {
                    Glide.with(h.itemView).load(imgUrl).placeholder(R.drawable.ic_launcher_background)
                            .into(h.ivItemImage);
                } else {
                    try {
                        Glide.with(h.itemView).asBitmap().load(ImageUtil.base64ToBytes(imgUrl))
                                .placeholder(R.drawable.ic_launcher_background).into(h.ivItemImage);
                    } catch (Exception e) {
                        h.ivItemImage.setImageResource(R.drawable.ic_launcher_background);
                    }
                }
            } else {
                h.ivItemImage.setImageResource(R.drawable.ic_launcher_background);
            }
        }

        @Override
        public int getItemCount() {
            return data.size() + 1;
        }

        @Override
        public int getItemViewType(int position) {
            return position == 0 ? TYPE_HEADER : TYPE_ITEM;
        }

        static class ItemViewHolder extends RecyclerView.ViewHolder {
            TextView tvItemName, tvItemDate, tvStatus, tvWeight;
            ImageView ivItemImage;

            public ItemViewHolder(View v) {
                super(v);
                tvItemName = v.findViewById(R.id.tvItemName);
                tvItemDate = v.findViewById(R.id.tvItemDate);
                tvStatus = v.findViewById(R.id.tvStatus);
                tvWeight = v.findViewById(R.id.tvWeight);
                ivItemImage = v.findViewById(R.id.ivItemImage);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (lrClaims != null)
            lrClaims.remove();
        if (lrLegacy != null)
            lrLegacy.remove();
        if (lrMyDonations != null)
            lrMyDonations.remove();
    }
}
