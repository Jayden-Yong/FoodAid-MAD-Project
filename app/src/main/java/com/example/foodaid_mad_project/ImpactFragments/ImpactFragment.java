package com.example.foodaid_mad_project.ImpactFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.foodaid_mad_project.R;

/**
 * <h1>ImpactFragment</h1>
 * <p>
 * The parent fragment for the Impact screen.
 * Handles the top-level View Mode switching (Week / Month / Year) using a
 * RadioGroup.
 * Delegates the content display to {@link ImpactBodyFragment}.
 * </p>
 */
public class ImpactFragment extends Fragment {

    private ImpactBodyFragment bodyFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_impact, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Child Fragment Management
        if (savedInstanceState == null) {
            bodyFragment = new ImpactBodyFragment();
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.impact_body_fragment_container, bodyFragment)
                    .commitNow();
        } else {
            bodyFragment = (ImpactBodyFragment) getChildFragmentManager()
                    .findFragmentById(R.id.impact_body_fragment_container);
        }

        // Time Range Selection Listener
        RadioGroup timeRangeGroup = view.findViewById(R.id.impactTimeRange);
        timeRangeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (bodyFragment == null)
                return;

            if (checkedId == R.id.rbImpactWeek) {
                bodyFragment.updateViewMode(ImpactBodyFragment.MODE_WEEK);
            } else if (checkedId == R.id.rbImpactMonth) {
                bodyFragment.updateViewMode(ImpactBodyFragment.MODE_MONTH);
            } else if (checkedId == R.id.rbImpactYear) {
                bodyFragment.updateViewMode(ImpactBodyFragment.MODE_YEAR);
            }
        });

        // Set default view mode
        bodyFragment.updateViewMode(ImpactBodyFragment.MODE_WEEK);
    }
}
