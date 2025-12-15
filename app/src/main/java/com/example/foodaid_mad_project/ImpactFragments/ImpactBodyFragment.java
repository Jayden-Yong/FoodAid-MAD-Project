package com.example.foodaid_mad_project.ImpactFragments;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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

import com.example.foodaid_mad_project.AuthFragments.User;
import com.example.foodaid_mad_project.Model.FoodItem;
import com.example.foodaid_mad_project.R;
import com.example.foodaid_mad_project.UserManager;
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
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

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
    private Calendar currentCalendar;
    private FirebaseFirestore db;
    private User currentUser;
    private List<FoodItem> allUserItems = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_impact_body, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        currentUser = UserManager.getInstance().getUser();
        currentCalendar = Calendar.getInstance();

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

        if (currentUser != null) {
            fetchUserImpactData();
        } else {
            Toast.makeText(getContext(), "Please login to view impact", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchUserImpactData() {
        // Fetch items where user is donor OR claimee
        // Firestore doesn't support OR queries natively well for this combined list
        // without multiple queries
        // We will fetch all items (likely from a global 'food_items' collection or
        // similar)
        // ideally efficient method: fetch items where donorId == uid AND items where
        // claimeeId == uid

        // For simplicity, let's assume valid implementation has a 'food_items'
        // collection
        // In reality, if volume is high, we need better indexing or structure.

        allUserItems.clear();

        db.collection("food_items")
                .whereEqualTo("donorId", currentUser.getUid())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<FoodItem> donatedItems = querySnapshot.toObjects(FoodItem.class);
                    allUserItems.addAll(donatedItems);

                    db.collection("food_items")
                            .whereEqualTo("claimeeId", currentUser.getUid())
                            .get()
                            .addOnSuccessListener(claimSnapshot -> {
                                List<FoodItem> claimedItems = claimSnapshot.toObjects(FoodItem.class);
                                // Avoid duplicates if user claimed their own item (edge case)
                                for (FoodItem item : claimedItems) {
                                    boolean exists = false;
                                    for (FoodItem existing : allUserItems) {
                                        if (existing.getId().equals(item.getId())) {
                                            exists = true;
                                            break;
                                        }
                                    }
                                    if (!exists)
                                        allUserItems.add(item);
                                }
                                updateViewMode(currentMode);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("ImpactBodyFragment", "Error fetching data", e);
                    // Fallback to offline/empty
                    updateViewMode(currentMode);
                });
    }

    public void updateViewMode(String mode) {
        this.currentMode = mode;

        if (tvDateRange == null || getContext() == null) {
            return;
        }

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(impactConstraint);

        calculateAndDisplayStats(mode);

        switch (mode) {
            case MODE_WEEK:
                updateDateRangeLabel(Calendar.WEEK_OF_YEAR);
                btnPrevDate.setOnClickListener(v -> adjustDate(Calendar.WEEK_OF_YEAR, -1));
                btnNextDate.setOnClickListener(v -> adjustDate(Calendar.WEEK_OF_YEAR, 1));

                constraintSet.connect(R.id.statsContainer, ConstraintSet.TOP, R.id.chartWeek, ConstraintSet.BOTTOM);
                constraintSet.applyTo(impactConstraint);

                headerMonthYear.setVisibility(View.GONE);

                // Charts Visibility
                chartWeek.setVisibility(View.VISIBLE);
                chartMonth.setVisibility(View.GONE);
                chartYear.setVisibility(View.GONE);

                // Adapter setup happens in calculateAndDisplayStats
                break;

            case MODE_MONTH:
                updateDateRangeLabel(Calendar.MONTH);
                btnPrevDate.setOnClickListener(v -> adjustDate(Calendar.MONTH, -1));
                btnNextDate.setOnClickListener(v -> adjustDate(Calendar.MONTH, 1));

                constraintSet.connect(R.id.statsContainer, ConstraintSet.TOP, R.id.chartMonth, ConstraintSet.BOTTOM);
                constraintSet.applyTo(impactConstraint);

                tvMonthYear.setText(getString(R.string.String, "Week"));
                headerMonthYear.setVisibility(View.VISIBLE);

                // Charts Visibility
                chartWeek.setVisibility(View.GONE);
                chartMonth.setVisibility(View.VISIBLE);
                chartYear.setVisibility(View.GONE);
                break;

            case MODE_YEAR:
                updateDateRangeLabel(Calendar.YEAR);
                btnPrevDate.setOnClickListener(v -> adjustDate(Calendar.YEAR, -1));
                btnNextDate.setOnClickListener(v -> adjustDate(Calendar.YEAR, 1));

                constraintSet.connect(R.id.statsContainer, ConstraintSet.TOP, R.id.chartYear, ConstraintSet.BOTTOM);
                constraintSet.applyTo(impactConstraint);

                tvMonthYear.setText(getString(R.string.String, "Month"));
                headerMonthYear.setVisibility(View.VISIBLE);

                // Charts Visibility
                chartWeek.setVisibility(View.GONE);
                chartMonth.setVisibility(View.GONE);
                chartYear.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void adjustDate(int field, int amount) {
        currentCalendar.add(field, amount);
        updateViewMode(currentMode);
    }

    private void updateDateRangeLabel(int field) {
        SimpleDateFormat sdf;
        if (field == Calendar.WEEK_OF_YEAR) {
            Calendar startOfWeek = (Calendar) currentCalendar.clone();
            startOfWeek.set(Calendar.DAY_OF_WEEK, startOfWeek.getFirstDayOfWeek());
            Calendar endOfWeek = (Calendar) startOfWeek.clone();
            endOfWeek.add(Calendar.DAY_OF_WEEK, 6);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String range = dateFormat.format(startOfWeek.getTime()) + "-" + dateFormat.format(endOfWeek.getTime());

            // Format for title: "2025 Dec 8-14" style roughly
            // Reusing the string resource structure
            // Using a simplified direct text for clarity as resource args are specific
            tvDateRange.setText(range);
            tvContributionsHeader.setText(getString(R.string.Personal_Contribution, range));

        } else if (field == Calendar.MONTH) {
            sdf = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
            tvDateRange.setText(sdf.format(currentCalendar.getTime()));
            tvContributionsHeader
                    .setText(getString(R.string.Personal_Contribution, sdf.format(currentCalendar.getTime())));
        } else {
            sdf = new SimpleDateFormat("yyyy", Locale.getDefault());
            tvDateRange.setText(sdf.format(currentCalendar.getTime()));
            tvContributionsHeader
                    .setText(getString(R.string.Personal_Contribution, sdf.format(currentCalendar.getTime())));
        }
    }

    private void calculateAndDisplayStats(String mode) {
        List<FoodItem> filteredItems = new ArrayList<>();

        long startTime = 0;
        long endTime = 0;

        Calendar cal = (Calendar) currentCalendar.clone();

        if (mode.equals(MODE_WEEK)) {
            cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            startTime = cal.getTimeInMillis();
            cal.add(Calendar.DAY_OF_WEEK, 7);
            endTime = cal.getTimeInMillis();
        } else if (mode.equals(MODE_MONTH)) {
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            startTime = cal.getTimeInMillis();
            cal.add(Calendar.MONTH, 1);
            endTime = cal.getTimeInMillis();
        } else {
            cal.set(Calendar.DAY_OF_YEAR, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            startTime = cal.getTimeInMillis();
            cal.add(Calendar.YEAR, 1);
            endTime = cal.getTimeInMillis();
        }

        int claimedCount = 0;
        int donatedCount = 0;
        double savedWeight = 0.0;

        for (FoodItem item : allUserItems) {
            if (item.getTimestamp() >= startTime && item.getTimestamp() < endTime) {
                filteredItems.add(item);
                if (item.getDonorId() != null && item.getDonorId().equals(currentUser.getUid())) {
                    donatedCount++;
                    // Only count saved weight if donation was successful (assumed) or simply
                    // tracking potential
                    savedWeight += item.getWeightKg();
                }
                if (item.getClaimeeId() != null && item.getClaimeeId().equals(currentUser.getUid())) {
                    claimedCount++;
                    savedWeight += item.getWeightKg(); // Claiming also saves food
                }
            }
        }

        // Update UI Text
        tvStatClaimedValue.setText(String.valueOf(claimedCount));
        tvStatClaimedDesc.setText(getString(R.string.Items_Claimed, mode.toLowerCase()));

        tvStatDonatedValue.setText(String.valueOf(donatedCount));
        tvStateDonatedDesc.setText(getString(R.string.Items_Donated, mode.toLowerCase()));

        tvStatSavedValue.setText(getString(R.string.Food_Saved_Value, savedWeight));

        // Setup Charts and Lists
        if (mode.equals(MODE_WEEK)) {
            setupBarChart(filteredItems);
            rvContributions.setAdapter(new WeekAdapter(filteredItems));
        } else if (mode.equals(MODE_MONTH)) {
            setupLineChart(filteredItems, startTime);
            rvContributions.setAdapter(new MonthAdapter(filteredItems)); // Simplified
        } else {
            setupPieChart(claimedCount, donatedCount);
            rvContributions.setAdapter(new YearAdapter(filteredItems)); // Simplified
        }
    }

    // --- Chart Setup Methods ---
    private void setupBarChart(List<FoodItem> items) {
        List<BarEntry> entries = new ArrayList<>();
        // Group by Day of Week
        Map<Integer, Float> dailyWeights = new HashMap<>();
        for (int i = 1; i <= 7; i++)
            dailyWeights.put(i, 0f); // Init 1-7 (Sun-Sat or Mon-Sun depending on locale)

        Calendar cal = Calendar.getInstance();
        for (FoodItem item : items) {
            cal.setTimeInMillis(item.getTimestamp());
            // Android Calendar.DAY_OF_WEEK: Sun=1, Mon=2...
            int day = cal.get(Calendar.DAY_OF_WEEK);
            // Re-map to Mon=1, Sun=7 for chart if desired, or keep standard
            dailyWeights.put(day, dailyWeights.get(day) + (float) item.getWeightKg());
        }

        for (Map.Entry<Integer, Float> entry : dailyWeights.entrySet()) {
            entries.add(new BarEntry(entry.getKey(), entry.getValue()));
        }

        BarDataSet set = new BarDataSet(entries, "Kg Saved");
        set.setColors(getResources().getColor(R.color.chart_unselected));
        BarData data = new BarData(set);
        chartWeek.setData(data);
        chartWeek.invalidate();
    }

    private void setupLineChart(List<FoodItem> items, long monthStart) {
        List<Entry> entries = new ArrayList<>();
        // Simple grouping: Week 1, 2, 3, 4
        float[] weeklyWeights = new float[5];

        Calendar cal = Calendar.getInstance();
        for (FoodItem item : items) {
            long diff = item.getTimestamp() - monthStart;
            int week = (int) (diff / (7 * 24 * 60 * 60 * 1000L)); // 0-indexed
            if (week >= 0 && week < 5)
                weeklyWeights[week] += (float) item.getWeightKg();
        }

        for (int i = 0; i < 5; i++) {
            entries.add(new Entry(i + 1, weeklyWeights[i]));
        }

        LineDataSet set = new LineDataSet(entries, "Kg Saved");
        set.setColor(Color.BLUE);
        LineData data = new LineData(set);
        chartMonth.setData(data);
        chartMonth.invalidate();
    }

    private void setupPieChart(int claimed, int donated) {
        List<PieEntry> entries = new ArrayList<>();
        if (claimed > 0)
            entries.add(new PieEntry((float) claimed, "Claimed"));
        if (donated > 0)
            entries.add(new PieEntry((float) donated, "Donated"));

        // "Saved" could be total? defaulting to these two categories for now

        if (entries.isEmpty())
            entries.add(new PieEntry(1f, "No Data"));

        PieDataSet set = new PieDataSet(entries, "Impact");
        set.setColors(ColorTemplate.JOYFUL_COLORS);
        PieData data = new PieData(set);
        chartYear.setData(data);
        chartYear.invalidate();
    }

    // --- ADAPTERS (Inner Classes) ---

    // 1. Week Adapter
    private class WeekAdapter extends RecyclerView.Adapter<WeekAdapter.ViewHolder> {
        List<FoodItem> data;
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, hh:mm a", Locale.getDefault());

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
            holder.tvItemName.setText(item.getName());
            holder.tvItemDate.setText(sdf.format(new Date(item.getTimestamp())));

            String statusSpec = "Available";
            if (currentUser != null) {
                if (currentUser.getUid().equals(item.getClaimeeId()))
                    statusSpec = "Claimed";
                else if (currentUser.getUid().equals(item.getDonorId()))
                    statusSpec = "Donated";
            }
            holder.tvStatus.setText(statusSpec);
            holder.tvWeight.setText(String.format(Locale.getDefault(), "+%.2fkg", item.getWeightKg()));
            holder.ivItemImage.setImageResource(R.drawable.ic_launcher_background); // Placeholder
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

    // 2. Month Adapter (Showing items instead of weeks for simplicity in dynamic
    // v1)
    private class MonthAdapter extends RecyclerView.Adapter<MonthAdapter.ViewHolder> {
        List<FoodItem> data;

        public MonthAdapter(List<FoodItem> data) {
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
            // Reusing weekly layout logic logic roughly for demo or proper aggregation
            // Since User requested update, let's just list items for now or aggregate if
            // advanced
            FoodItem item = data.get(position);
            holder.tvWeekTitle.setText(item.getName());
            // Reuse fields arbitrarily to show data or fix layout if we strictly need
            // aggregation
            // For V1 dynamic, listing items is often clearer than arbitrary "Week 1"
            // buckets if data is sparse
            holder.tvDateRange.setText(new Date(item.getTimestamp()).toString());
            holder.tvSavedAmount.setText(String.format("%.2f kg", item.getWeightKg()));
            holder.tvClaimedCount.setText("1"); // Per item
            holder.tvDonatedCount.setText("0");
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvWeekTitle, tvDateRange, tvClaimedCount, tvDonatedCount, tvSavedAmount;

            public ViewHolder(View itemView) {
                super(itemView);
                tvWeekTitle = itemView.findViewById(R.id.tvWeekTitle);
                tvDateRange = itemView.findViewById(R.id.tvDateRange);
                tvClaimedCount = itemView.findViewById(R.id.tvClaimedCount);
                tvDonatedCount = itemView.findViewById(R.id.tvDonatedCount);
                tvSavedAmount = itemView.findViewById(R.id.tvSavedAmount);
            }
        }
    }

    // 3. Year Adapter (Uses fragment_year_contribution.xml)
    private class YearAdapter extends RecyclerView.Adapter<YearAdapter.ViewHolder> {
        List<FoodItem> data;

        public YearAdapter(List<FoodItem> data) {
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
            FoodItem item = data.get(position);
            holder.tvMonthName.setText(item.getName());
            holder.tvYearSaved.setText(String.format("%.2f", item.getWeightKg()));
            holder.tvYearClaimed.setText("-");
            holder.tvYearDonated.setText("-");
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvMonthName, tvYearClaimed, tvYearDonated, tvYearSaved;

            public ViewHolder(View itemView) {
                super(itemView);
                tvMonthName = itemView.findViewById(R.id.tvMonthName);
                tvYearClaimed = itemView.findViewById(R.id.tvYearClaimed);
                tvYearDonated = itemView.findViewById(R.id.tvYearDonated);
                tvYearSaved = itemView.findViewById(R.id.tvYearSaved);
            }
        }
    }
}
