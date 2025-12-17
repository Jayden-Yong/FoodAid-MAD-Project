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

    private TextView tvProductTitle, tvPickupTime, tvQuantity, tvLocationLabel, tvPostedBy, tvPickupMethod;
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
        tvPickupMethod = view.findViewById(R.id.tvPickupMethod);
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

        tvQuantity.setText("Quantity: " + foodItem.getQuantity() + " (" + foodItem.getWeight() + " kg Total)");

        String pickupMethod = foodItem.getPickupMethod();
        if (pickupMethod == null || pickupMethod.isEmpty())
            pickupMethod = "Meet Up";
        tvPickupMethod.setText("Pickup Method: " + pickupMethod);

        tvLocationLabel.setText(getString(R.string.Food_Location, foodItem.getLocationName()));
        tvPostedBy.setText(getString(R.string.Food_Donator, foodItem.getDonatorName()));

        // Category check
        if (foodItem.getCategory() != null) {
            String cat = foodItem.getCategory();
            if (cat.equalsIgnoreCase("GROCERIES") || cat.equalsIgnoreCase("Pantry")) {
                radioGroupCategory.check(R.id.radioGroceries);
            } else if (cat.equalsIgnoreCase("MEALS") || cat.equalsIgnoreCase("Leftover")) {
                radioGroupCategory.check(R.id.radioMeals);
            }
        }

        if (foodItem.getImageUri() != null && !foodItem.getImageUri().isEmpty()) {
            String imageStr = foodItem.getImageUri();
            if (imageStr.startsWith("http")) {
                com.bumptech.glide.Glide.with(this)
                        .load(imageStr)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(ivProductImage);
            } else {
                try {
                    byte[] imageBytes = com.example.foodaid_mad_project.Utils.ImageUtil.base64ToBytes(imageStr);
                    com.bumptech.glide.Glide.with(this)
                            .asBitmap()
                            .load(imageBytes)
                            .placeholder(R.drawable.ic_launcher_background)
                            .error(R.drawable.ic_launcher_background)
                            .into(ivProductImage);
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
        if (foodItem == null || foodItem.getDonationId() == null) {
            Toast.makeText(getContext(), "Error: Invalid Item ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Always show dialog if logic requires user to confirm quantity, even for 1.
        // But per original logic: if quantity > 1, show dialog.
        // If quantity == 1, maybe confirm?
        // Let's stick to: if > 1, show dialog. Else auto-claim 1 (but maybe add
        // confirmation?)
        // For better UX, let's just claim directly if Qty=1 but with a "Confirm"
        // dialog?
        // To stick to request "choose how many", if Qty=1, choice is trivial.

        if (foodItem.getQuantity() > 1) {
            showQuantityDialog();
        } else {
            // Confirm single claim? Or just claim.
            // Let's just claim 1 for simplicity consistent with previous flow
            performClaimTransaction(1);
        }
    }

    private void showQuantityDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        // Inflate the custom layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_claim_quantity, null);
        builder.setView(dialogView);

        android.app.AlertDialog dialog = builder.create();
        // Transparent background for rounded corners
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        TextView tvTitle = dialogView.findViewById(R.id.tvClaimDialogTitle);
        TextView tvMax = dialogView.findViewById(R.id.tvMaxQuantity);
        android.widget.EditText etQuantity = dialogView.findViewById(R.id.etClaimQuantity);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelClaim);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirmClaim);

        tvMax.setText(getString(R.string.String, "Available: " + foodItem.getQuantity()));
        etQuantity.setHint("Max " + foodItem.getQuantity());

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String qtyStr = etQuantity.getText().toString().trim();
            if (!qtyStr.isEmpty()) {
                try {
                    int qtyToClaim = Integer.parseInt(qtyStr);
                    if (qtyToClaim > 0 && qtyToClaim <= foodItem.getQuantity()) {
                        performClaimTransaction(qtyToClaim);
                        dialog.dismiss();
                    } else {
                        Toast.makeText(getContext(), "Invalid quantity (1-" + foodItem.getQuantity() + ")",
                                Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Enter a number", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Enter quantity", Toast.LENGTH_SHORT).show();
            }
        });

        // Show
        dialog.show();
    }

    private void performClaimTransaction(int claimQty) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("donations").document(foodItem.getDonationId());

        // Get user info
        String uid = FirebaseAuth.getInstance().getUid();
        String userName = "Anonymous";
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String name = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
            if (name != null && !name.isEmpty())
                userName = name;
        }
        final String finalUserName = userName;

        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(docRef);
            String status = snapshot.getString("status");
            Long endTime = snapshot.getLong("endTime");
            Long currentQtyComp = snapshot.getLong("quantity");
            int currentQty = (currentQtyComp != null) ? currentQtyComp.intValue() : 0;

            Double currentWeight = snapshot.getDouble("weight");
            if (currentWeight == null)
                currentWeight = 0.0;

            long currentTime = System.currentTimeMillis();

            // Validation inside transaction
            if (status != null && status.equals("AVAILABLE") &&
                    (endTime == null || endTime > currentTime) &&
                    currentQty >= claimQty) {

                double unitWeight = (currentQty > 0) ? (currentWeight / currentQty) : 0.0;
                double claimedWeight = unitWeight * claimQty;

                int newQty = currentQty - claimQty;
                double newTotalWeight = currentWeight - claimedWeight;
                // Ensure non-negative
                if (newTotalWeight < 0)
                    newTotalWeight = 0.0;

                transaction.update(docRef, "quantity", newQty);
                transaction.update(docRef, "weight", newTotalWeight);

                if (newQty == 0) {
                    transaction.update(docRef, "status", "CLAIMED");
                    // Optimization: claimedBy could be the last person, or "Multiple"
                    transaction.update(docRef, "claimedBy", uid);
                }

                // --- NEW TRACKING LOGIC ---
                // Add to sub-collection: donations/{id}/claims/{auto-id}
                DocumentReference newClaimRef = docRef.collection("claims").document();
                java.util.Map<String, Object> claimData = new java.util.HashMap<>();
                claimData.put("claimerId", uid);
                claimData.put("claimerName", finalUserName);
                claimData.put("quantityClaimed", claimQty);
                claimData.put("timestamp", currentTime);

                // Redundant data for Impact Page efficiency
                claimData.put("foodTitle", foodItem.getTitle());
                // Only save image URI if it is a remote URL (http/https) OR Base64 (which is
                // reasonable size now 400x400)
                if (foodItem.getImageUri() != null) {
                    claimData.put("foodImage", foodItem.getImageUri());
                }
                claimData.put("location", foodItem.getLocationName());

                // Store correctly calculated claimed weight
                claimData.put("weight", claimedWeight);

                transaction.set(newClaimRef, claimData);
                // --------------------------

                return null; // Success
            } else {
                throw new FirebaseFirestoreException("Item unavailable or insufficient quantity",
                        FirebaseFirestoreException.Code.ABORTED);
            }
        }).addOnSuccessListener(result -> {
            // Navigate to Success
            FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.coveringFragment, new ClaimNotifyFragment())
                    .addToBackStack("ClaimSuccess")
                    .commit();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Claim failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }
}
