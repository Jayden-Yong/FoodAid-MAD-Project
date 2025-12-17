package com.example.foodaid_mad_project.HomeFragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.foodaid_mad_project.DonateFragments.DonateNotifyFragment;
import com.example.foodaid_mad_project.Model.FoodItem;
import com.example.foodaid_mad_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.widget.Toast;

public class ItemDetailsFragment extends Fragment {

    private FoodItem foodItem;

    private TextView tvProductTitle, tvPickupTime, tvQuantity, tvLocationLabel, tvPostedBy;
    private ImageView ivProductImage;
    private RadioGroup radioGroupCategory;

    public ItemDetailsFragment() {
        // Required empty public constructor
    }

    public ItemDetailsFragment(FoodItem item) {
        this.foodItem = item;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_item_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (foodItem == null)
            return;

        tvProductTitle = view.findViewById(R.id.tvProductTitle);
        tvPickupTime = view.findViewById(R.id.tvPickupTime);
        tvQuantity = view.findViewById(R.id.tvQuantity);
        tvLocationLabel = view.findViewById(R.id.tvLocationLabel);
        tvPostedBy = view.findViewById(R.id.tvPostedBy);
        ivProductImage = view.findViewById(R.id.ivProductImage);
        radioGroupCategory = view.findViewById(R.id.radioGroupCategory);

        tvProductTitle.setText(getString(R.string.Food_Name, foodItem.getTitle()));

        // Convert timestamps to readable time if needed, or just show duration/range
        // For simplicity, showing a generic message or formatting dates.
        // Assuming user wants start/end time formatted.
        // Simple placeholder for now as per previous logic which used string array.
        // Let's format the start/end time.
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String startStr = sdf.format(new Date(foodItem.getStartTime()));
        String endStr = sdf.format(new Date(foodItem.getEndTime()));
        tvPickupTime.setText(getString(R.string.Pickup_Time, startStr, endStr));

        tvQuantity.setText("Weight: " + foodItem.getWeight() + " kg");
        tvLocationLabel.setText(getString(R.string.Food_Location, foodItem.getLocationName()));
        tvPostedBy.setText(getString(R.string.Food_Donator, foodItem.getDonatorName()));

        // Category check
        if (foodItem.getCategory() != null) {
            // Check based on string
            if (foodItem.getCategory().equalsIgnoreCase("GROCERIES")) {
                radioGroupCategory.check(R.id.radioGroceries);
            } else if (foodItem.getCategory().equalsIgnoreCase("MEALS")) {
                radioGroupCategory.check(R.id.radioMeals);
            }
        }

        if (foodItem.getImageUri() != null && !foodItem.getImageUri().isEmpty()) {
            String imageStr = foodItem.getImageUri();
            if (imageStr.startsWith("http")) {
                com.bumptech.glide.Glide.with(this).load(imageStr).into(ivProductImage);
            } else {
                try {
                    byte[] imageBytes = com.example.foodaid_mad_project.Utils.ImageUtil.base64ToBytes(imageStr);
                    com.bumptech.glide.Glide.with(this).load(imageBytes).into(ivProductImage);
                } catch (Exception e) {
                    ivProductImage.setImageResource(R.drawable.ic_launcher_background);
                }
            }
        }

        TextView toolBarTitle = view.findViewById(R.id.toolbarTitle);
        toolBarTitle.setText(getString(R.string.String, "Food Aid Details"));

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                            getParentFragmentManager().popBackStack("ItemDetail",
                                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
                            getParentFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        }
                    }
                });

        Toolbar toolbar = view.findViewById(R.id.Toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack("ItemDetail", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    getParentFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
            });
        }

        Button btnClaim = view.findViewById(R.id.btnClaim);
        if (btnClaim != null) {
            btnClaim.setOnClickListener(v -> claimDonation());
        }
    }

    private void claimDonation() {
        if (foodItem.getDonationId() == null) {
            Toast.makeText(getContext(), "Error: Invalid Item ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if quantity > 1 to show dialog
        if (foodItem.getQuantity() > 1) {
            showQuantityDialog();
        } else {
            performClaimTransaction(1);
        }
    }

    private void showQuantityDialog() {
        // Simple Alert Dialog with NumberPicker or Input
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle("Claim Quantity");

        final android.widget.EditText input = new android.widget.EditText(getContext());
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setHint("Enter quantity (Max: " + foodItem.getQuantity() + ")");
        builder.setView(input);

        builder.setPositiveButton("Claim", (dialog, which) -> {
            String qtyStr = input.getText().toString();
            if (!qtyStr.isEmpty()) {
                int qtyToClaim = Integer.parseInt(qtyStr);
                if (qtyToClaim > 0 && qtyToClaim <= foodItem.getQuantity()) {
                    performClaimTransaction(qtyToClaim);
                } else {
                    Toast.makeText(getContext(), "Invalid quantity", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void performClaimTransaction(int claimQty) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("donations").document(foodItem.getDonationId());

        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(docRef);
            String status = snapshot.getString("status");
            Long endTime = snapshot.getLong("endTime");
            Long currentQtyComp = snapshot.getLong("quantity");
            int currentQty = (currentQtyComp != null) ? currentQtyComp.intValue() : 1;

            long currentTime = System.currentTimeMillis();

            if (status != null && status.equals("AVAILABLE") &&
                    endTime != null && endTime > currentTime && currentQty >= claimQty) {

                int newQty = currentQty - claimQty;
                transaction.update(docRef, "quantity", newQty);

                if (newQty == 0) {
                    transaction.update(docRef, "status", "CLAIMED");
                    transaction.update(docRef, "claimedBy", FirebaseAuth.getInstance().getUid()); // Only marks as
                                                                                                  // claimed by LAST
                                                                                                  // person? Or logic
                                                                                                  // needs a
                                                                                                  // subcollection?
                    // For Phase 1 simplicity: The item is "Gone" when qty is 0.
                    // Tracking "who claimed what" might need a separate "claims" collection or
                    // array.
                    // Let's add to a "claims" collection for history tracking.
                    // But for this transaction, just decrement.
                }

                // Add to 'claims' collection for record (Atomic?)
                // Ideally yes, but firestore transactions on multiple docs work.
                // Let's keep it simple: Update donation doc. Context: "User X claimed N items".
                // Maybe update an array field "claimants"?
                // transaction.update(docRef, "claimants", FieldValue.arrayUnion(uid + ":" +
                // claimQty));

                return null; // Success
            } else {
                throw new FirebaseFirestoreException("Item unavailable or insufficient quantity",
                        FirebaseFirestoreException.Code.ABORTED);
            }
        }).addOnSuccessListener(result -> {
            // Navigate to Success
            FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.ItemDetailsFragmentContainer, new ClaimNotifyFragment())
                    .addToBackStack("ClaimSuccess")
                    .commit();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Claim failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
