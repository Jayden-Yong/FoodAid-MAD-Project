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

    // Real Data Lists
    private List<FoodItem> claimedItems = new ArrayList<>();
    private List<FoodItem> donatedItems = new ArrayList<>();
    private ImpactCalculator calculator = new ImpactCalculator();
    private FirebaseAuth auth;
    private FirebaseFirestore db;

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

        // Date Range
        btnPrevDate = view.findViewById(R.id.btnPrevDate);
        btnNextDate = view.findViewById(R.id.btnNextDate);
        tvDateRange = view.findViewById(R.id.tvDateRange);
        // Chart
        chartWeek = view.findViewById(R.id.chartWeek);
        chartMonth = view.findViewById(R.id.chartMonth);
        chartYear = view.findViewById(R.id.chartYear);
        // Stats
        statsContainer = view.findViewById(R.id.statsContainer);
        tvStatClaimedValue = view.findViewById(R.id.tvStatClaimedValue);
        tvStatClaimedDesc = view.findViewById(R.id.tvStatClaimedDesc);
        tvStatDonatedValue = view.findViewById(R.id.tvStatDonatedValue);
        tvStateDonatedDesc = view.findViewById(R.id.tvStateDonatedDesc);
        tvStatSavedValue = view.findViewById(R.id.tvStatSavedValue);
        // Contribution Title
        tvContributionsHeader = view.findViewById(R.id.tvContributionsHeader);
        tvMonthYear = view.findViewById(R.id.tvMonthYear);
        // Contribution Recycler
        headerMonthYear = view.findViewById(R.id.header_Month_Year);
        rvContributions = view.findViewById(R.id.rvContributions);
        impactConstraint = view.findViewById(R.id.impactConstraint);

        rvContributions.setLayoutManager(new LinearLayoutManager(getContext()));

        fetchData();
    }

    private void fetchData() {
        if (auth.getCurrentUser() == null)
            return;
        String uid = auth.getCurrentUser().getUid();

        // 1. Fetch New Claims (Collection Group)
        com.google.android.gms.tasks.Task<com.google.firebase.firestore.QuerySnapshot> taskClaims = db
                .collectionGroup("claims")
                .whereEqualTo("claimerId", uid)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get();

        // 2. Fetch Legacy Claims (Donations)
        com.google.android.gms.tasks.Task<com.google.firebase.firestore.QuerySnapshot> taskLegacy = db
                .collection("donations")
                .whereEqualTo("claimedBy", uid)
                .whereEqualTo("status", "CLAIMED")
                .get();

        // 3. Fetch Donated Items
        com.google.android.gms.tasks.Task<com.google.firebase.firestore.QuerySnapshot> taskMyDonations = db
                .collection("donations")
                .whereEqualTo("donatorId", uid)
                .get();

        com.google.android.gms.tasks.Tasks.whenAllSuccess(taskClaims, taskLegacy, taskMyDonations)
                .addOnSuccessListener(results -> {
                    // Process Results
                    com.google.firebase.firestore.QuerySnapshot claimsSnap = (com.google.firebase.firestore.QuerySnapshot) results
                            .get(0);
                    com.google.firebase.firestore.QuerySnapshot legacySnap = (com.google.firebase.firestore.QuerySnapshot) results
                            .get(1);
                    com.google.firebase.firestore.QuerySnapshot myDonationsSnap = (com.google.firebase.firestore.QuerySnapshot) results
                            .get(2);

                    claimedItems.clear();
                    List<String> processedDonationIds = new ArrayList<>();

                    // A. Process New Claims
                    for (com.google.firebase.firestore.DocumentSnapshot doc : claimsSnap) {
                        FoodItem item = new FoodItem();
                        // Map fields from claim sub-collection to FoodItem for display
                        item.setTitle(doc.getString("foodTitle"));
                        // Retrieve Image: could be URL or Resource ID.
                        // Our model mostly uses String imageUri.
                        Object img = doc.get("foodImage");
                        if (img instanceof String)
                            item.setImageUri((String) img);

                        item.setLocationName(doc.getString("location"));

                        Double w = doc.getDouble("weight");
                        item.setWeight(w != null ? w : 0.0);

                        Long ts = doc.getLong("timestamp");
                        item.setTimestamp(ts != null ? ts : 0);

                        // If title is null (legacy data in subcoll?), fallback
                        if (item.getTitle() == null)
                            item.setTitle("Claimed Item");

                        claimedItems.add(item);

                        // Track parent ID to avoid duplicates if also in legacy list
                        if (doc.getReference().getParent().getParent() != null) {
                            processedDonationIds.add(doc.getReference().getParent().getParent().getId());
                        }
                    }

                    // B. Process Legacy Claims (only if not covered by new claims)
                    for (com.google.firebase.firestore.DocumentSnapshot doc : legacySnap) {
                        if (!processedDonationIds.contains(doc.getId())) {
                            claimedItems.add(doc.toObject(FoodItem.class));
                        }
                    }

                    // Sort Combined List
                    java.util.Collections.sort(claimedItems,
                            (o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));

                    // C. Process Donated Items
                    donatedItems.clear();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : myDonationsSnap) {
                        donatedItems.add(doc.toObject(FoodItem.class));
                    }

                    checkBadges();
                    refreshStats();
                })
                .addOnFailureListener(e -> Log.e("Impact", "Error fetching data", e));
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
            refreshStats(); // Trigger refresh with new mode
        }
    }

    private void refreshStats() {
        if (getContext() == null)
            return;

        Calendar now = Calendar.getInstance();
        long endTime = now.getTimeInMillis();
        long startTime = 0;
        String dateRangeText = "";

        // Calculate Start Time based on Mode
        Calendar startCal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(impactConstraint);

        // Filter items for the list
        List<FoodItem> filteredItems = new ArrayList<>();

        switch (currentMode) {
            case MODE_WEEK:
                startCal.set(Calendar.DAY_OF_WEEK, startCal.getFirstDayOfWeek());
                startTime = startCal.getTimeInMillis();
                dateRangeText = sdf.format(startCal.getTime()) + " - " + sdf.format(now.getTime());

                constraintSet.connect(R.id.statsContainer, ConstraintSet.TOP, R.id.chartWeek, ConstraintSet.BOTTOM);
                tvContributionsHeader.setText("This Week's Contribution");
                headerMonthYear.setVisibility(View.GONE);
                chartWeek.setVisibility(View.VISIBLE);
                chartMonth.setVisibility(View.GONE);
                chartYear.setVisibility(View.GONE);
                setupBarChart();
                break;

            case MODE_MONTH:
                startCal.set(Calendar.DAY_OF_MONTH, 1);
                startTime = startCal.getTimeInMillis();
                dateRangeText = new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(now.getTime());

                constraintSet.connect(R.id.statsContainer, ConstraintSet.TOP, R.id.chartMonth, ConstraintSet.BOTTOM);
                tvContributionsHeader.setText("This Month's Contribution");
                tvMonthYear.setText("Week");
                headerMonthYear.setVisibility(View.VISIBLE);
                chartWeek.setVisibility(View.GONE);
                chartMonth.setVisibility(View.VISIBLE);
                chartYear.setVisibility(View.GONE);
                setupLineChart();
                break;

            case MODE_YEAR:
                startCal.set(Calendar.DAY_OF_YEAR, 1);
                startTime = startCal.getTimeInMillis();
                dateRangeText = new SimpleDateFormat("yyyy", Locale.getDefault()).format(now.getTime());

                constraintSet.connect(R.id.statsContainer, ConstraintSet.TOP, R.id.chartYear, ConstraintSet.BOTTOM);
                tvContributionsHeader.setText("This Year's Contribution");
                tvMonthYear.setText("Month");
                headerMonthYear.setVisibility(View.VISIBLE);
                chartWeek.setVisibility(View.GONE);
                chartMonth.setVisibility(View.GONE);
                chartYear.setVisibility(View.VISIBLE);
                setupPieChart();
                break;
        }

        constraintSet.applyTo(impactConstraint);
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
        rvContributions.setAdapter(new WeekAdapter(filteredItems)); // Reuse WeekAdapter (layout) for all
    }

    // --- Chart Setup (Visuals only for now, can perform further data binding if
    // needed) ---
    private void setupBarChart() {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(1, 10)); // Dummy data to keep UI stable
        BarDataSet set = new BarDataSet(entries, "Items");
        set.setColor(getResources().getColor(R.color.teal_200));
        BarData data = new BarData(set);
        chartWeek.setData(data);
        chartWeek.invalidate();
    }

    private void setupLineChart() {
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(1, 10)); // Dummy
        LineDataSet set = new LineDataSet(entries, "Kg");
        set.setColor(Color.BLUE);
        LineData data = new LineData(set);
        chartMonth.setData(data);
        chartMonth.invalidate();
    }

    private void setupPieChart() {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry((float) claimedItems.size(), "Claimed"));
        entries.add(new PieEntry((float) donatedItems.size(), "Donated"));
        PieDataSet set = new PieDataSet(entries, "Impact");
        set.setColors(ColorTemplate.JOYFUL_COLORS);
        PieData data = new PieData(set);
        chartYear.setData(data);
        chartYear.invalidate();
    }

    // --- ADAPTERS ---
    // Simplified adapters to just show lists of FoodItems for the demo

    private class WeekAdapter extends RecyclerView.Adapter<WeekAdapter.ViewHolder> {
        List<FoodItem> data;

        public WeekAdapter(List<FoodItem> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_week_contribution, parent,
                    false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            FoodItem item = data.get(position);
            holder.tvItemName.setText(item.getTitle());
            holder.tvItemDate
                    .setText(new SimpleDateFormat("dd/MM", Locale.getDefault()).format(new Date(item.getTimestamp())));
            holder.tvStatus.setText("Saved");
            holder.tvWeight.setText(item.getWeight() + " kg");
            holder.ivItemImage.setImageResource(R.drawable.ic_launcher_background); // Placeholder or load URI
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvItemName, tvItemDate, tvStatus, tvWeight;
            ImageView ivItemImage;

            public ViewHolder(View itemView) {
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