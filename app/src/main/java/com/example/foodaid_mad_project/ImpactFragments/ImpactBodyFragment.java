package com.example.foodaid_mad_project.ImpactFragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import java.util.ArrayList;
import java.util.List;

public class ImpactBodyFragment extends Fragment {

    public static final String MODE_WEEK = "WEEK";
    public static final String MODE_MONTH = "MONTH";
    public static final String MODE_YEAR = "YEAR";

    private TextView tvDateRange, tvChartTitle;
    private BarChart chartWeek;
    private LineChart chartMonth;
    private PieChart chartYear;
    private LinearLayout statsContainer;
    private LinearLayout headerMonthYear;
    private RecyclerView rvContributions;
    private ConstraintLayout impactConstraint;

    private String currentMode = MODE_WEEK;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_impact_body, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind Views
        tvDateRange = view.findViewById(R.id.tvDateRange);
        tvChartTitle = view.findViewById(R.id.tvChartTitle);
        chartWeek = view.findViewById(R.id.chartWeek);
        chartMonth = view.findViewById(R.id.chartMonth);
        chartYear = view.findViewById(R.id.chartYear);
        statsContainer = view.findViewById(R.id.statsContainer);
        headerMonthYear = view.findViewById(R.id.header_Month_Year);
        rvContributions = view.findViewById(R.id.rvContributions);
        impactConstraint = view.findViewById(R.id.impactConstraint);

        rvContributions.setLayoutManager(new LinearLayoutManager(getContext()));

        updateViewMode(currentMode);
    }

    public void updateViewMode(String mode) {
        this.currentMode = mode;

        if (tvDateRange == null || getContext() == null) {
            return;
        }

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(impactConstraint);
        String[] timeList = getResources().getStringArray(R.array.Time_List);

        switch (mode) {
            case MODE_WEEK:
                // UI Setup
                tvDateRange.setText(String.format(timeList[0], 2025, "Dec", 8, 14));
                constraintSet.connect(R.id.statsContainer, ConstraintSet.TOP, R.id.chartWeek, ConstraintSet.BOTTOM);
                constraintSet.applyTo(impactConstraint);
                headerMonthYear.setVisibility(View.GONE);

                // Charts Visibility
                chartWeek.setVisibility(View.VISIBLE);
                chartMonth.setVisibility(View.GONE);
                chartYear.setVisibility(View.GONE);

                setupBarChart(); // Mock Data
                rvContributions.setAdapter(new WeekAdapter(getMockWeekData()));
                break;

            case MODE_MONTH:
                // UI Setup
                tvDateRange.setText(String.format(timeList[1], 2025, "Dec"));
                constraintSet.connect(R.id.statsContainer, ConstraintSet.TOP, R.id.chartMonth, ConstraintSet.BOTTOM);
                constraintSet.applyTo(impactConstraint);
                headerMonthYear.setVisibility(View.VISIBLE);

                // Charts Visibility
                chartWeek.setVisibility(View.GONE);
                chartMonth.setVisibility(View.VISIBLE);
                chartYear.setVisibility(View.GONE);

                setupLineChart(); // Mock Data
                rvContributions.setAdapter(new MonthAdapter(getMockMonthData()));
                break;

            case MODE_YEAR:
                // UI Setup
                tvDateRange.setText(String.format(timeList[2], 2025));
                constraintSet.connect(R.id.statsContainer, ConstraintSet.TOP, R.id.chartYear, ConstraintSet.BOTTOM);
                constraintSet.applyTo(impactConstraint);
                headerMonthYear.setVisibility(View.VISIBLE);

                // Charts Visibility
                chartWeek.setVisibility(View.GONE);
                chartMonth.setVisibility(View.GONE);
                chartYear.setVisibility(View.VISIBLE);

                setupPieChart(); // Mock Data
                rvContributions.setAdapter(new YearAdapter(getMockYearData()));
                break;
        }
    }

    // --- Mock Chart Setup Methods ---
    private void setupBarChart() {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(1, 10)); // Mon
        entries.add(new BarEntry(2, 5));  // Tue
        entries.add(new BarEntry(3, 8));  // Wed
        BarDataSet set = new BarDataSet(entries, "Items");
        //TODO: Maybe set the onValueSelect to primary color
        set.setColors(getResources().getColor(R.color.chart_unselected));
        BarData data = new BarData(set);
        chartWeek.setData(data);
        chartWeek.invalidate(); // Refresh
    }

    private void setupLineChart() {
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(1, 20)); // Week 1
        entries.add(new Entry(2, 45)); // Week 2
        entries.add(new Entry(3, 30)); // Week 3
        LineDataSet set = new LineDataSet(entries, "Kg Saved");
        set.setColor(Color.BLUE);
        LineData data = new LineData(set);
        chartMonth.setData(data);
        chartMonth.invalidate();
    }

    private void setupPieChart() {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(40f, "Saved"));
        entries.add(new PieEntry(30f, "Donated"));
        entries.add(new PieEntry(30f, "Claimed"));
        PieDataSet set = new PieDataSet(entries, "Impact");
        set.setColors(ColorTemplate.JOYFUL_COLORS);
        PieData data = new PieData(set);
        chartYear.setData(data);
        chartYear.invalidate();
    }

    // --- Mock Data Generators ---
    private List<String> getMockWeekData() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) list.add("Bread");
        return list;
    }
    private List<String> getMockMonthData() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 4; i++) list.add("Week " + (i+1));
        return list;
    }
    private List<String> getMockYearData() {
        List<String> list = new ArrayList<>();
        list.add("January"); list.add("February"); list.add("March");
        return list;
    }

    // --- ADAPTERS (Inner Classes) ---

    // 1. Week Adapter (Uses fragment_week_contribution.xml)
    private class WeekAdapter extends RecyclerView.Adapter<WeekAdapter.ViewHolder> {
        List<String> data;
        public WeekAdapter(List<String> data) { this.data = data; }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_week_contribution, parent, false);
            return new ViewHolder(v);
        }
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.tvItemName.setText(data.get(position));
            holder.tvStatus.setText("Saved");
        }
        @Override public int getItemCount() { return data.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvItemName, tvStatus;
            public ViewHolder(View itemView) {
                super(itemView);
                tvItemName = itemView.findViewById(R.id.tvItemName);
                tvStatus = itemView.findViewById(R.id.tvStatus);
            }
        }
    }

    // 2. Month Adapter (Uses fragment_month_contribution.xml)
    private class MonthAdapter extends RecyclerView.Adapter<MonthAdapter.ViewHolder> {
        List<String> data;
        public MonthAdapter(List<String> data) { this.data = data; }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_month_contribution, parent, false);
            return new ViewHolder(v);
        }
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.tvWeekTitle.setText(data.get(position));
            holder.tvClaimedCount.setText("5");
            holder.tvDonatedCount.setText("2");
            holder.tvSavedAmount.setText("10kg");
        }
        @Override public int getItemCount() { return data.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvWeekTitle, tvClaimedCount, tvDonatedCount, tvSavedAmount;
            public ViewHolder(View itemView) {
                super(itemView);
                tvWeekTitle = itemView.findViewById(R.id.tvWeekTitle);
                tvClaimedCount = itemView.findViewById(R.id.tvClaimedCount);
                tvDonatedCount = itemView.findViewById(R.id.tvDonatedCount);
                tvSavedAmount = itemView.findViewById(R.id.tvSavedAmount);
            }
        }
    }

    // 3. Year Adapter (Uses fragment_year_contribution.xml)
    private class YearAdapter extends RecyclerView.Adapter<YearAdapter.ViewHolder> {
        List<String> data;
        public YearAdapter(List<String> data) { this.data = data; }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_year_contribution, parent, false);
            return new ViewHolder(v);
        }
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.tvMonthName.setText(data.get(position));
            holder.tvYearClaimed.setText("20");
            holder.tvYearDonated.setText("15");
            holder.tvYearSaved.setText("50kg");
        }
        @Override public int getItemCount() { return data.size(); }

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