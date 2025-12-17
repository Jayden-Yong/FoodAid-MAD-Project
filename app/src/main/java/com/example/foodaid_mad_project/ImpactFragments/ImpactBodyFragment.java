package com.example.foodaid_mad_project.ImpactFragments;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodaid_mad_project.Model.FoodItem;
import com.example.foodaid_mad_project.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ImpactBodyFragment extends Fragment {

    public static final String MODE_WEEK = "WEEK";
    public static final String MODE_MONTH = "MONTH";
    public static final String MODE_YEAR = "YEAR";

    private TextView tvDateRange, tvContributionsHeader, tvStatClaimedValue, tvStatClaimedDesc, tvStatDonatedValue,
            tvStateDonatedDesc, tvStatSavedValue, tvMonthYear;
    private ImageButton btnPrevDate, btnNextDate;
    private BarChart chartWeek;
    private LineChart chartMonth;
    private PieChart chartYear;
    private LinearLayout statsContainer, headerMonthYear;
    private RecyclerView rvContributions;
    private ConstraintLayout impactConstraint;

    private String currentMode = MODE_WEEK;
    private Calendar displayedDate;

    // Real Data Lists
    private List<FoodItem> claimedItems = new ArrayList<>();
    private List<FoodItem> donatedItems = new ArrayList<>();

    // Raw Snapshots for merging
    private List<com.google.firebase.firestore.DocumentSnapshot> rawClaims = new ArrayList<>();
    private List<com.google.firebase.firestore.DocumentSnapshot> rawLegacy = new ArrayList<>();
    private List<com.google.firebase.firestore.DocumentSnapshot> rawMyDonations = new ArrayList<>();

    private com.google.firebase.firestore.ListenerRegistration lrClaims;
    private com.google.firebase.firestore.ListenerRegistration lrLegacy;
    private com.google.firebase.firestore.ListenerRegistration lrMyDonations;

    private ImpactCalculator calculator = new ImpactCalculator();
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_impact_body, container, false);
    }

    // Header View Reference
    private View headerView;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        displayedDate = Calendar.getInstance();

        // 1. Setup RecyclerView (The only thing in the main layout)
        rvContributions = view.findViewById(R.id.rvContributions);
        rvContributions.setLayoutManager(new LinearLayoutManager(getContext()));

        // 2. Inflate Header View manually
        headerView = LayoutInflater.from(getContext()).inflate(R.layout.item_impact_header, rvContributions, false);

        // 3. Find Views INSIDE the Header
        btnPrevDate = headerView.findViewById(R.id.btnPrevDate);
        btnNextDate = headerView.findViewById(R.id.btnNextDate);
        tvDateRange = headerView.findViewById(R.id.tvDateRange);

        chartWeek = headerView.findViewById(R.id.chartWeek);
        chartMonth = headerView.findViewById(R.id.chartMonth);
        chartYear = headerView.findViewById(R.id.chartYear);

        statsContainer = headerView.findViewById(R.id.statsContainer);
        tvStatClaimedValue = headerView.findViewById(R.id.tvStatClaimedValue);
        tvStatClaimedDesc = headerView.findViewById(R.id.tvStatClaimedDesc);
        tvStatDonatedValue = headerView.findViewById(R.id.tvStatDonatedValue);
        tvStateDonatedDesc = headerView.findViewById(R.id.tvStateDonatedDesc);
        tvStatSavedValue = headerView.findViewById(R.id.tvStatSavedValue);

        tvContributionsHeader = headerView.findViewById(R.id.tvContributionsHeader);
        tvMonthYear = headerView.findViewById(R.id.tvMonthYear);
        headerMonthYear = headerView.findViewById(R.id.header_Month_Year);
        impactConstraint = headerView.findViewById(R.id.impactConstraint);

        setupListeners();
        setupDateNavigation();
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

    private void setupListeners() {
        if (auth.getCurrentUser() == null)
            return;
        String uid = auth.getCurrentUser().getUid();

        // 1. New Claims
        if (lrClaims != null)
            lrClaims.remove();
        lrClaims = db.collectionGroup("claims")
                .whereEqualTo("claimerId", uid)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("Impact", "Claims Error", e);
                        return;
                    }
                    if (snapshots != null) {
                        rawClaims = snapshots.getDocuments();
                        processData();
                    }
                });

        // 2. Legacy Claims (Donations)
        if (lrLegacy != null)
            lrLegacy.remove();
        lrLegacy = db.collection("donations")
                .whereEqualTo("claimedBy", uid)
                .whereEqualTo("status", "CLAIMED")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("Impact", "Legacy Error", e);
                        return;
                    }
                    if (snapshots != null) {
                        rawLegacy = snapshots.getDocuments();
                        processData();
                    }
                });

        // 3. My Donations
        if (lrMyDonations != null)
            lrMyDonations.remove();
        lrMyDonations = db.collection("donations")
                .whereEqualTo("donatorId", uid)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("Impact", "Donations Error", e);
                        return;
                    }
                    if (snapshots != null) {
                        rawMyDonations = snapshots.getDocuments();
                        processData();
                    }
                });
    }

    private void processData() {
        claimedItems.clear();
        donatedItems.clear();
        List<String> processedDonationIds = new ArrayList<>();

        // A. Process New Claims
        for (com.google.firebase.firestore.DocumentSnapshot doc : rawClaims) {
            FoodItem item = new FoodItem();
            item.setTitle(doc.getString("foodTitle"));
            Object img = doc.get("foodImage");
            if (img instanceof String)
                item.setImageUri((String) img);
            item.setLocationName(doc.getString("location"));
            Double w = doc.getDouble("weight");
            item.setWeight(w != null ? w : 0.0);
            Long ts = doc.getLong("timestamp");
            item.setTimestamp(ts != null ? ts : 0);

            if (item.getTitle() == null)
                item.setTitle("Claimed Item");
            claimedItems.add(item);

            if (doc.getReference().getParent().getParent() != null) {
                processedDonationIds.add(doc.getReference().getParent().getParent().getId());
            }
        }

        // B. Process Legacy Claims
        for (com.google.firebase.firestore.DocumentSnapshot doc : rawLegacy) {
            if (!processedDonationIds.contains(doc.getId())) {
                claimedItems.add(doc.toObject(FoodItem.class));
            }
        }

        // Sort Combined List
        java.util.Collections.sort(claimedItems, (o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));

        // C. Process Donated Items
        for (com.google.firebase.firestore.DocumentSnapshot doc : rawMyDonations) {
            donatedItems.add(doc.toObject(FoodItem.class));
        }

        checkBadges();
        refreshStats();
    }

    private void checkBadges() {
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
                    .addOnSuccessListener(aVoid -> Log.d("Impact", "Badges updated"));
        }
    }

    public void updateViewMode(String mode) {
        this.currentMode = mode;
        if (tvDateRange != null) {
            // Reset to today when switching modes? Or keep the date?
            // Let's reset to today for better UX when switching granularity
            displayedDate = Calendar.getInstance();
            refreshStats(); // Trigger refresh with new mode
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

    private void refreshStats() {
        if (getContext() == null)
            return;

        // Clone the displayed date so we don't mess up the state variable
        Calendar calSnapshot = (Calendar) displayedDate.clone();

        long startTime = 0;
        long endTime = 0;
        String dateRangeText = "";
        SimpleDateFormat sdfDayMonth = new SimpleDateFormat("dd MMM", Locale.getDefault());
        SimpleDateFormat sdfFull = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

        // Filter items for the list
        List<FoodItem> filteredItems = new ArrayList<>();

        switch (currentMode) {
            case MODE_WEEK:
                // Set to start of week
                calSnapshot.set(Calendar.DAY_OF_WEEK, calSnapshot.getFirstDayOfWeek());
                calSnapshot.set(Calendar.HOUR_OF_DAY, 0);
                calSnapshot.set(Calendar.MINUTE, 0);
                calSnapshot.set(Calendar.SECOND, 0);
                calSnapshot.set(Calendar.MILLISECOND, 0);
                startTime = calSnapshot.getTimeInMillis();

                // Set to end of week
                calSnapshot.add(Calendar.DAY_OF_YEAR, 6);
                calSnapshot.set(Calendar.HOUR_OF_DAY, 23);
                calSnapshot.set(Calendar.MINUTE, 59);
                calSnapshot.set(Calendar.SECOND, 59);
                endTime = calSnapshot.getTimeInMillis();

                // Format: "03 Dec - 09 Dec 2024" or simpler
                // Let's do: "03 Dec - 09 Dec" (and maybe year if different?)
                String startStr = sdfDayMonth.format(new Date(startTime));
                String endStr = sdfFull.format(new Date(endTime));
                dateRangeText = startStr + " - " + endStr;

                tvContributionsHeader.setText("Contribution (Week)");
                headerMonthYear.setVisibility(View.GONE);
                chartWeek.setVisibility(View.VISIBLE);
                chartMonth.setVisibility(View.GONE);
                chartYear.setVisibility(View.GONE);
                setupBarChart(startTime, endTime);
                break;

            case MODE_MONTH:
                calSnapshot.set(Calendar.DAY_OF_MONTH, 1);
                calSnapshot.set(Calendar.HOUR_OF_DAY, 0);
                calSnapshot.set(Calendar.MINUTE, 0);
                calSnapshot.set(Calendar.SECOND, 0);
                startTime = calSnapshot.getTimeInMillis();

                int maxDay = calSnapshot.getActualMaximum(Calendar.DAY_OF_MONTH);
                calSnapshot.set(Calendar.DAY_OF_MONTH, maxDay);
                calSnapshot.set(Calendar.HOUR_OF_DAY, 23);
                calSnapshot.set(Calendar.MINUTE, 59);
                endTime = calSnapshot.getTimeInMillis();

                dateRangeText = new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(new Date(startTime));

                tvContributionsHeader.setText("Contribution (Month)");
                tvMonthYear.setText("Week");
                headerMonthYear.setVisibility(View.VISIBLE);
                chartWeek.setVisibility(View.GONE);
                chartMonth.setVisibility(View.VISIBLE);
                chartYear.setVisibility(View.GONE);
                setupLineChart(startTime, endTime);
                break;

            case MODE_YEAR:
                calSnapshot.set(Calendar.DAY_OF_YEAR, 1);
                calSnapshot.set(Calendar.HOUR_OF_DAY, 0);
                calSnapshot.set(Calendar.MINUTE, 0);
                startTime = calSnapshot.getTimeInMillis();

                // End of year
                calSnapshot.set(Calendar.MONTH, 11); // Dec
                calSnapshot.set(Calendar.DAY_OF_MONTH, 31);
                calSnapshot.set(Calendar.HOUR_OF_DAY, 23);
                calSnapshot.set(Calendar.MINUTE, 59);
                endTime = calSnapshot.getTimeInMillis();

                dateRangeText = new SimpleDateFormat("yyyy", Locale.getDefault()).format(new Date(startTime));

                tvContributionsHeader.setText("Contribution (Year)");
                tvMonthYear.setText("Month");
                headerMonthYear.setVisibility(View.VISIBLE);
                chartWeek.setVisibility(View.GONE);
                chartMonth.setVisibility(View.GONE);
                chartYear.setVisibility(View.VISIBLE);
                setupPieChart(startTime, endTime);
                break;
        }
        tvDateRange.setText(dateRangeText);

        // Calculate Stats using Calculator
        int claimedCount = calculator.getItemCountForRange(claimedItems, startTime, endTime);
        int donatedCount = calculator.getItemCountForRange(donatedItems, startTime, endTime);
        double savedWeight = calculator.getWeightForRange(claimedItems, startTime, endTime);

        tvStatClaimedValue.setText(String.valueOf(claimedCount));
        tvStatDonatedValue.setText(String.valueOf(donatedCount));
        tvStatSavedValue.setText(String.format(Locale.getDefault(), "%.1f kg", savedWeight));

        tvStatClaimedDesc.setText(getString(R.string.Items_Claimed, currentMode.toLowerCase()));
        tvStateDonatedDesc.setText(getString(R.string.Items_Donated, currentMode.toLowerCase()));

        // Filter List for Adpater
        for (FoodItem item : claimedItems) {
            if (item.getTimestamp() >= startTime && item.getTimestamp() <= endTime) {
                filteredItems.add(item);
            }
        }
        // PASSED HEADER VIEW
        rvContributions.setAdapter(new WeekAdapter(filteredItems, headerView));
    }

    // --- Chart Setup (Visuals only for now, can perform further data binding if
    // needed) ---
    // --- Chart Setup ---

    private void setupBarChart(long startOfWeek, long endOfWeek) {
        if (claimedItems == null || claimedItems.isEmpty()) {
            chartWeek.clear();
            return;
        }

        // 2. Aggregate Daily Weights
        // Map: DayOfYear -> Weight
        // We only care about 7 days
        float[] dailyWeights = new float[7];
        // 0=Sun, 1=Mon ... depending on Locale. Let's align with Calendar.DAY_OF_WEEK
        // (1-7)

        for (FoodItem item : claimedItems) {
            long ts = item.getTimestamp();
            if (ts >= startOfWeek && ts <= endOfWeek) {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(ts);
                int dayOfWeek = c.get(Calendar.DAY_OF_WEEK); // 1 (Sun) - 7 (Sat)
                // Normalize to array index 0-6
                int index = dayOfWeek - 1;
                if (index >= 0 && index < 7) {
                    dailyWeights[index] += (float) item.getWeight();
                }
            }
        }

        List<BarEntry> entries = new ArrayList<>();
        // Create entries for each day
        for (int i = 0; i < 7; i++) {
            entries.add(new BarEntry(i + 1, dailyWeights[i]));
        }

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
        if (claimedItems == null) {
            chartMonth.clear();
            return;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(startOfMonth);
        int maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // 2. Aggregate Daily Weights for the Month
        float[] dailyWeights = new float[maxDays + 1]; // Index 1 to maxDays

        for (FoodItem item : claimedItems) {
            if (item == null)
                continue;
            long ts = item.getTimestamp();
            if (ts >= startOfMonth && ts <= endOfMonth) {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(ts);
                int day = c.get(Calendar.DAY_OF_MONTH);
                if (day >= 1 && day <= maxDays) {
                    dailyWeights[day] += (float) item.getWeight();
                }
            }
        }

        List<Entry> entries = new ArrayList<>();
        // Show ALL days in the month, not just up to today
        for (int i = 1; i <= maxDays; i++) {
            entries.add(new Entry(i, dailyWeights[i]));
        }

        LineDataSet set = new LineDataSet(entries, "Saved (kg)");
        set.setColor(getResources().getColor(R.color.teal_200)); // Use app color
        set.setLineWidth(2.5f);
        set.setCircleColor(getResources().getColor(R.color.teal_200));
        set.setCircleRadius(4f);
        set.setDrawValues(false); // Cleaner look
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER); // Smooth curves
        set.setDrawFilled(true);
        set.setFillColor(getResources().getColor(R.color.teal_200));
        set.setFillAlpha(50);

        LineData data = new LineData(set);
        chartMonth.setData(data);
        chartMonth.getDescription().setEnabled(false);
        chartMonth.getXAxis().setDrawGridLines(false);
        chartMonth.getAxisRight().setEnabled(false);
        chartMonth.animateX(1000);
        chartMonth.invalidate();
    }

    private void setupPieChart(long startOfYear, long endOfYear) {
        int claimedCount = 0;
        for (FoodItem i : claimedItems) {
            if (i.getTimestamp() >= startOfYear && i.getTimestamp() <= endOfYear)
                claimedCount++;
        }

        int donatedCount = 0;
        for (FoodItem i : donatedItems) {
            if (i.getTimestamp() >= startOfYear && i.getTimestamp() <= endOfYear)
                donatedCount++;
        }

        List<PieEntry> entries = new ArrayList<>();
        if (claimedCount > 0)
            entries.add(new PieEntry((float) claimedCount, "Claimed"));
        if (donatedCount > 0)
            entries.add(new PieEntry((float) donatedCount, "Donated"));

        // Handle Empty State
        if (entries.isEmpty()) {
            chartYear.setCenterText("No Activity");
            chartYear.setData(null);
            chartYear.invalidate();
            return;
        } else {
            chartYear.setCenterText("");
        }

        PieDataSet set = new PieDataSet(entries, ""); // Blank label to hide legend title if redundant

        // Custom Colors: Teal (App Primary) for Claims, Orange for Donations
        List<Integer> colors = new ArrayList<>();
        if (claimedCount > 0)
            colors.add(getResources().getColor(R.color.teal_200));
        if (donatedCount > 0)
            colors.add(Color.parseColor("#FF9800")); // Orange
        set.setColors(colors);

        set.setValueTextSize(14f);
        set.setValueTextColor(Color.WHITE);
        set.setSliceSpace(3f);
        set.setSelectionShift(5f);

        PieData data = new PieData(set);
        chartYear.setData(data);
        chartYear.getDescription().setEnabled(false);
        chartYear.setHoleRadius(40f);
        chartYear.setTransparentCircleRadius(45f);
        chartYear.animateY(1000);
        chartYear.invalidate();
    }

    // --- ADAPTERS ---

    private class WeekAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_HEADER = 0;
        private static final int TYPE_ITEM = 1;

        List<FoodItem> data;
        View headerView;

        public WeekAdapter(List<FoodItem> data, View headerView) {
            this.data = data;
            this.headerView = headerView;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_HEADER) {
                // Return the existing header view
                // We must ensure it doesn't have a parent, or remove it if it does
                if (headerView.getParent() != null) {
                    ((ViewGroup) headerView.getParent()).removeView(headerView);
                }
                // Creating a simplified holder
                return new RecyclerView.ViewHolder(headerView) {
                };
            }
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_week_contribution, parent,
                    false);
            return new ItemViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (getItemViewType(position) == TYPE_HEADER) {
                return; // Header is already bound to logic in Fragment
            }

            // Adjust position for item
            int itemPos = position - 1;
            FoodItem item = data.get(itemPos);
            ItemViewHolder itemHolder = (ItemViewHolder) holder;

            itemHolder.tvItemName.setText(item.getTitle());
            itemHolder.tvItemDate
                    .setText(new SimpleDateFormat("dd/MM", Locale.getDefault()).format(new Date(item.getTimestamp())));
            itemHolder.tvStatus.setText("Saved");
            itemHolder.tvWeight.setText(item.getWeight() + " kg");

            // Handle image logic
            if (item.getImageUri() != null && !item.getImageUri().isEmpty()) {
                String imageStr = item.getImageUri();
                if (imageStr.startsWith("http")) {
                    com.bumptech.glide.Glide.with(holder.itemView.getContext())
                            .load(imageStr)
                            .placeholder(R.drawable.ic_launcher_background)
                            .into(itemHolder.ivItemImage);
                } else {
                    // Optimization: Try to load bytes only if needed, OR relies on Glide caching
                    try {
                        byte[] imageBytes = com.example.foodaid_mad_project.Utils.ImageUtil.base64ToBytes(imageStr);
                        com.bumptech.glide.Glide.with(holder.itemView.getContext())
                                .asBitmap()
                                .load(imageBytes)
                                .placeholder(R.drawable.ic_launcher_background)
                                .into(itemHolder.ivItemImage);
                    } catch (Exception e) {
                        itemHolder.ivItemImage.setImageResource(R.drawable.ic_launcher_background);
                    }
                }
            } else {
                itemHolder.ivItemImage.setImageResource(R.drawable.ic_launcher_background);
            }
        }

        @Override
        public int getItemCount() {
            return data.size() + 1; // +1 for Header
        }

        @Override
        public int getItemViewType(int position) {
            return position == 0 ? TYPE_HEADER : TYPE_ITEM;
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {
            TextView tvItemName, tvItemDate, tvStatus, tvWeight;
            ImageView ivItemImage;

            public ItemViewHolder(View itemView) {
                super(itemView);
                ivItemImage = itemView.findViewById(R.id.ivItemImage);
                tvItemName = itemView.findViewById(R.id.tvItemName);
                tvItemDate = itemView.findViewById(R.id.tvItemDate);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                tvWeight = itemView.findViewById(R.id.tvWeight);
            }
        }
    }

    // Retaining dummy adapters for Month/Year to avoid UI crashes if layouts are
    // specific
    private class MonthAdapter extends RecyclerView.Adapter<MonthAdapter.ViewHolder> {
        List<String> data;

        public MonthAdapter(List<String> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_month_contribution, parent,
                    false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            public ViewHolder(View itemView) {
                super(itemView);
            }
        }
    }

    private class YearAdapter extends RecyclerView.Adapter<YearAdapter.ViewHolder> {
        List<String> data;

        public YearAdapter(List<String> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_year_contribution, parent,
                    false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            public ViewHolder(View itemView) {
                super(itemView);
            }
        }
    }
}