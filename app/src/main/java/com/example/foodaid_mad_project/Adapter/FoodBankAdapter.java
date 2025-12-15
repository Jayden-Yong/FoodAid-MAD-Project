package com.example.foodaid_mad_project.Adapter;

import android.content.Context;
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
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(FoodBank foodBank);
    }

    public FoodBankAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<FoodBank> foodBanks) {
        this.foodBanks = foodBanks;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
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
        private final TextView tvPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvFoodBankName);
            tvType = itemView.findViewById(R.id.tvFoodBankType);
            tvLocation = itemView.findViewById(R.id.tvFoodBankLocation);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }

        public void bind(FoodBank foodBank, OnItemClickListener listener) {
            tvName.setText(foodBank.getName());
            tvType.setText(foodBank.getCategory());
            // Use getLocation() for compatibility or getAddress()
            tvLocation.setText(foodBank.getAddress() != null ? foodBank.getAddress() : foodBank.getLocation());

            if (foodBank.getPrice() <= 0) {
                tvPrice.setText("Free");
            } else {
                tvPrice.setText(String.format("RM %.2f", foodBank.getPrice()));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(foodBank);
                }
            });
        }
    }
}
