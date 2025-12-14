package com.example.foodaid_mad_project.HomeFragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodaid_mad_project.Model.FoodBank;
import com.example.foodaid_mad_project.R;

import java.util.ArrayList;
import java.util.List;

public class FoodBankAdapter extends RecyclerView.Adapter<FoodBankAdapter.ViewHolder> {

    private List<FoodBank> foodBanks = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(FoodBank foodBank);
    }

    public FoodBankAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setFoodBanks(List<FoodBank> foodBanks) {
        this.foodBanks = foodBanks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_foodbank, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodBank foodBank = foodBanks.get(position);
        holder.bind(foodBank, listener);
    }

    @Override
    public int getItemCount() {
        return foodBanks.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvType;
        private final TextView tvLocation;
        private final TextView tvRating;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvFoodBankName);
            tvType = itemView.findViewById(R.id.tvFoodBankType);
            tvLocation = itemView.findViewById(R.id.tvFoodBankLocation);
            tvRating = itemView.findViewById(R.id.tvRating);
        }

        public void bind(FoodBank foodBank, OnItemClickListener listener) {
            tvName.setText(foodBank.getName());
            tvType.setText(foodBank.getType());
            tvLocation.setText(foodBank.getAddress());
            tvRating.setText(String.format("⭐ %.1f", foodBank.getRating()));

            itemView.setOnClickListener(v -> listener.onItemClick(foodBank));
        }
    }
}
