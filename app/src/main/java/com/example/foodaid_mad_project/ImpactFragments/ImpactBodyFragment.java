package com.example.foodaid_mad_project.ImpactFragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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

    private TextView tvDateRange, tvContributionsHeader, tvStatClaimedValue, tvStatClaimedDesc, tvStatDonatedValue, tvStateDonatedDesc, tvStatSavedValue, tvMonthYear;
    private ImageButton btnPrevDate, btnNextDate;
    private BarChart chartWeek;
    private LineChart chartMonth;
    private PieChart chartYear;
    private LinearLayout statsContainer, headerMonthYear;
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

        //Date Range
        btnPrevDate = view.findViewById(R.id.btnPrevDate);
        btnNextDate = view.findViewById(R.id.btnNextDate);
        tvDateRange = view.findViewById(R.id.tvDateRange);
        //Chart
        chartWeek = view.findViewById(R.id.chartWeek);
        chartMonth = view.findViewById(R.id.chartMonth);
        chartYear = view.findViewById(R.id.chartYear);
        //Stats
        statsContainer = view.findViewById(R.id.statsContainer);
        tvStatClaimedValue = view.findViewById(R.id.tvStatClaimedValue);
        tvStatClaimedDesc = view.findViewById(R.id.tvStatClaimedDesc);
        tvStatDonatedValue = view.findViewById(R.id.tvStatDonatedValue);
        tvStateDonatedDesc = view.findViewById(R.id.tvStateDonatedDesc);
        tvStatSavedValue = view.findViewById(R.id.tvStatSavedValue);
        //Contribution Title
        tvContributionsHeader = view.findViewById(R.id.tvContributionsHeader);
        tvMonthYear = view.findViewById(R.id.tvMonthYear);
        //Contribution Recycler
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
                // TODO: Get Time, get week, month, year
                tvDateRange.setText(String.format(timeList[0], 2025, "Dec", 8, 14));
                btnPrevDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO:Calculate Week
                    }
                });
                btnNextDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO:Calculate Week
                    }
                });
                constraintSet.connect(R.id.statsContainer, ConstraintSet.TOP, R.id.chartWeek, ConstraintSet.BOTTOM);
                constraintSet.applyTo(impactConstraint);
                //TODO: Get Total value from database
                tvStatClaimedValue.setText(getString(R.string.Number, 2));
                tvStatClaimedDesc.setText(getString(R.string.Items_Claimed, "Week"));
                tvStatDonatedValue.setText(getString(R.string.Number, 5));
                tvStateDonatedDesc.setText(getString(R.string.Items_Donated, "Week"));
                var weightWeek = 2.5;
                tvStatSavedValue.setText(getString(R.string.Food_Saved_Value, weightWeek));
                tvContributionsHeader.setText(getString(R.string.Personal_Contribution, "24/11/2025-30/11/2025"));
                headerMonthYear.setVisibility(View.GONE);

                // Charts Visibility
                chartWeek.setVisibility(View.VISIBLE);
                chartMonth.setVisibility(View.GONE);
                chartYear.setVisibility(View.GONE);

                setupBarChart(); // Mock Data
                rvContributions.setAdapter(new WeekAdapter(getMockWeekData()));
                break;

            case MODE_MONTH:
                // TODO: Get Time, get week, month, year
                tvDateRange.setText(String.format(timeList[1], 2025, "Dec"));
                btnPrevDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO:Calculate Month
                    }
                });
                btnNextDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO:Calculate Month
                    }
                });
                constraintSet.connect(R.id.statsContainer, ConstraintSet.TOP, R.id.chartMonth, ConstraintSet.BOTTOM);
                constraintSet.applyTo(impactConstraint);
                //TODO: Get Total value from database
                tvStatClaimedValue.setText(getString(R.string.Number, 2));
                tvStatClaimedDesc.setText(getString(R.string.Items_Claimed, "Month"));
                tvStatDonatedValue.setText(getString(R.string.Number, 5));
                tvStateDonatedDesc.setText(getString(R.string.Items_Donated, "Month"));
                var weightMonth = 2.5;
                tvStatSavedValue.setText(getString(R.string.Food_Saved_Value, weightMonth));
                tvContributionsHeader.setText(getString(R.string.Personal_Contribution, "24/11/2025-30/11/2025"));
                tvContributionsHeader.setText(getString(R.string.Personal_Contribution, "Nov 2025"));
                tvMonthYear.setText(getString(R.string.String, "Week"));
                headerMonthYear.setVisibility(View.VISIBLE);

                // Charts Visibility
                chartWeek.setVisibility(View.GONE);
                chartMonth.setVisibility(View.VISIBLE);
                chartYear.setVisibility(View.GONE);

                setupLineChart(); // Mock Data
                rvContributions.setAdapter(new MonthAdapter(getMockMonthData()));
                break;

            case MODE_YEAR:
                // TODO: Get Time, get week, month, year
                tvDateRange.setText(String.format(timeList[2], 2025));
                btnPrevDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO:Calculate Year
                    }
                });
                btnNextDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO:Calculate Year
                    }
                });
                constraintSet.connect(R.id.statsContainer, ConstraintSet.TOP, R.id.chartYear, ConstraintSet.BOTTOM);
                constraintSet.applyTo(impactConstraint);
                //TODO: Get Total value from database
                tvStatClaimedValue.setText(getString(R.string.Number, 2));
                tvStatClaimedDesc.setText(getString(R.string.Items_Claimed, "Year"));
                tvStatDonatedValue.setText(getString(R.string.Number, 5));
                tvStateDonatedDesc.setText(getString(R.string.Items_Donated, "Year"));
                var weightYear = 2.5;
                tvStatSavedValue.setText(getString(R.string.Food_Saved_Value, weightYear));
                tvContributionsHeader.setText(getString(R.string.Personal_Contribution, "24/11/2025-30/11/2025"));
                tvContributionsHeader.setText(getString(R.string.Personal_Contribution, "2025"));
                tvMonthYear.setText(getString(R.string.String, "Month"));
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

        //TODO: Replace with real data, Day, Food Saved(kg)
        entries.add(new BarEntry(1, 10)); // Mon, kg
        entries.add(new BarEntry(2, 5));  // Tue, kg
        entries.add(new BarEntry(3, 8));  // Wed, kg
        BarDataSet set = new BarDataSet(entries, "Items");
        //TODO: Maybe set the onValueSelect to primary color
        set.setColors(getResources().getColor(R.color.chart_unselected));
        BarData data = new BarData(set);
        chartWeek.setData(data);
        chartWeek.invalidate(); // Refresh
    }

    private void setupLineChart() {
        List<Entry> entries = new ArrayList<>();

        //TODO: Replace with real data, Week, Food Saved(kg)
        entries.add(new Entry(1, 20)); // Week 1, kg
        entries.add(new Entry(2, 45)); // Week 2, kg
        entries.add(new Entry(3, 30)); // Week 3, kg
        LineDataSet set = new LineDataSet(entries, "Kg Saved");
        set.setColor(Color.BLUE);
        LineData data = new LineData(set);
        chartMonth.setData(data);
        chartMonth.invalidate();
    }

    private void setupPieChart() {
        List<PieEntry> entries = new ArrayList<>();

        //TODO: Replace with real data
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
        //TODO: Data put in here
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
        public WeekAdapter(List<String> data) {
            this.data = data;
        }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_week_contribution, parent, false);
            return new ViewHolder(v);
        }
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            //TODO: Data go here
            holder.ivItemImage.setImageResource(R.drawable.ic_launcher_background);
            holder.tvItemName.setText(data.get(position));
            holder.tvItemDate.setText(getString(R.string.Contribute_Time, "Mon", 2, "PM"));
            holder.tvStatus.setText(getString(R.string.String, "Claimed"));
            var contributionWeight = 0.2;
            holder.tvWeight.setText(getString(R.string.Contribution_Amount, contributionWeight));
        }
        @Override public int getItemCount() {
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
            //TODO: Data go here
            holder.tvWeekTitle.setText(data.get(position));
            holder.tvDateRange.setText(getString(R.string.Contribute_Week, "3/11/2025", " 9/11/2025"));
            holder.tvClaimedCount.setText(getString(R.string.Contribution_Quantity_Type, 5));
            holder.tvDonatedCount.setText(getString(R.string.Contribution_Quantity_Type, 2));
            holder.tvSavedAmount.setText(getString(R.string.Contribution_Amount, 10.0));
        }
        @Override public int getItemCount() { return data.size(); }

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
        List<String> data;
        public YearAdapter(List<String> data) { this.data = data; }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_year_contribution, parent, false);
            return new ViewHolder(v);
        }
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            //TODO: Data go here
            holder.tvMonthName.setText(data.get(position));
            holder.tvYearClaimed.setText(getString(R.string.Contribution_Quantity_Type, 5));
            holder.tvYearDonated.setText(getString(R.string.Contribution_Quantity_Type, 5));
            holder.tvYearSaved.setText(getString(R.string.Contribution_Amount, 5.0));
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