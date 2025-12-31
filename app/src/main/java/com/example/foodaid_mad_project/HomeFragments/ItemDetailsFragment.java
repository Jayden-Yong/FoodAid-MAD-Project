package com.example.foodaid_mad_project.HomeFragments;

import android.app.AlertDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.example.foodaid_mad_project.Model.FoodItem;
import com.example.foodaid_mad_project.R;
import com.example.foodaid_mad_project.Utils.ImageUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * ItemDetailsFragment
 *
 * Displays the full details of a donated FoodItem.
 * Allows users to:
 * - View Item ID, Title, Image, Quantity, Weight, and Pickup Times.
 * - See the donor's name and location.
 * - Claim the item (full or partial quantity).
 */
public class ItemDetailsFragment extends Fragment {

    private FoodItem foodItem;
    private ListenerRegistration itemListener;

    // UI Elements
    private TextView tvProductTitle, tvPickupTime, tvQuantity, tvLocationLabel, tvPostedBy, tvPickupMethod;
    private ImageView ivProductImage;

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

        // 1. Initialize Views
        initializeViews(view);

        // 2. Bind Data
        bindDataToViews();

        // 3. Setup Actions (Back, Claim)
        setupActions(view);

        // 5. Load the Map for this specific item
        loadItemMap();

        // 6. Check donor privacy
        checkDonorPrivacy();

        // 4. Start Real-time Updates
        setupRealtimeUpdates();
    }

    private void initializeViews(View view) {
        tvProductTitle = view.findViewById(R.id.tvProductTitle);
        tvPickupTime = view.findViewById(R.id.tvPickupTime);
        tvQuantity = view.findViewById(R.id.tvQuantity);
        tvPickupMethod = view.findViewById(R.id.tvPickupMethod);
        tvLocationLabel = view.findViewById(R.id.tvLocationLabel);
        tvPostedBy = view.findViewById(R.id.tvPostedBy);
        ivProductImage = view.findViewById(R.id.ivProductImage);
    }

    private void bindDataToViews() {
        if (getContext() == null || foodItem == null)
            return;

        tvProductTitle.setText(getString(R.string.Food_Name, foodItem.getTitle()));

        // Format Dates
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String startStr = sdf.format(new Date(foodItem.getStartTime()));
        String endStr = sdf.format(new Date(foodItem.getEndTime()));
        tvPickupTime.setText(getString(R.string.Pickup_Time, startStr, endStr));

        // Quantity & Weight
        tvQuantity.setText("Quantity: " + foodItem.getQuantity() + " (" + foodItem.getWeight() + " kg Total)");

        // Pickup Method
        String pickupMethod = foodItem.getPickupMethod();
        if (pickupMethod == null || pickupMethod.isEmpty())
            pickupMethod = "Meet Up";
        tvPickupMethod.setText("Pickup Method: " + pickupMethod);

        // Location & Donor
        tvLocationLabel.setText(getString(R.string.Food_Location, foodItem.getLocationName()));
        tvPostedBy.setText(getString(R.string.Food_Donator, foodItem.getDonatorName()));

        // Category (if present)
        if (foodItem.getCategory() != null) {
            TextView tvCategory = getView().findViewById(R.id.tvCategoryValue);
            if (tvCategory != null)
                tvCategory.setText(foodItem.getCategory());
        }

        // Image Loading
        loadImage(foodItem.getImageUri());
    }

    private void loadImage(String imageStr) {
        if (imageStr != null && !imageStr.isEmpty()) {
            if (imageStr.startsWith("http")) {
                Glide.with(this)
                        .load(imageStr)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(ivProductImage);
            } else {
                try {
                    byte[] imageBytes = ImageUtil.base64ToBytes(imageStr);
                    Glide.with(this)
                            .asBitmap()
                            .load(imageBytes)
                            .placeholder(R.drawable.ic_launcher_background)
                            .error(R.drawable.ic_launcher_background)
                            .into(ivProductImage);
                } catch (Exception e) {
                    ivProductImage.setImageResource(R.drawable.ic_launcher_background);
                }
            }
        } else {
            ivProductImage.setImageResource(R.drawable.ic_launcher_background);
        }
    }

    private void setupActions(View view) {
        // Toolbar Title
        TextView toolBarTitle = view.findViewById(R.id.toolbarTitle);
        toolBarTitle.setText(getString(R.string.String, "Food Aid Details")); // "String" resource seems generic, verify
                                                                              // later. Using literal "Food Aid Details"
                                                                              // as safe fallback if String res is odd.

        // Back Navigation
        Toolbar toolbar = view.findViewById(R.id.Toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> navigateBack());
        }
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        navigateBack();
                    }
                });

        // Claim Button
        Button btnClaim = view.findViewById(R.id.btnClaim);
        if (btnClaim != null) {
            btnClaim.setOnClickListener(v -> initiateClaimProcess());
        }
    }

    private void navigateBack() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack("ItemDetail", FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getParentFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    private void initiateClaimProcess() {
        if (foodItem == null || foodItem.getDonationId() == null) {
            Toast.makeText(getContext(), "Error: Invalid Item ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // If multiple items, ask user how many to claim.
        // If only 1, simple confirmation or direct claim is possible.
        // We stick to direct claim for 1 to reduce friction, dialog for > 1.
        if (foodItem.getQuantity() > 1) {
            showQuantityDialog();
        } else {
            performClaimTransaction(1);
        }
    }

    private void showQuantityDialog() {
        if (getContext() == null)
            return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_claim_quantity, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        TextView tvMax = dialogView.findViewById(R.id.tvMaxQuantity);
        EditText etQuantity = dialogView.findViewById(R.id.etClaimQuantity);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelClaim);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirmClaim);

        tvMax.setText(getString(R.string.String, "Available: " + foodItem.getQuantity()));
        etQuantity.setHint("Max " + foodItem.getQuantity());

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String qtyStr = etQuantity.getText().toString().trim();
            if (!qtyStr.isEmpty()) {
                try {
                    android.util.Log.d("ClaimDebug", "Raw Input: '" + qtyStr + "'");
                    // Use BigDecimal for robust exactness
                    java.math.BigDecimal qtyBd = new java.math.BigDecimal(qtyStr);
                    android.util.Log.d("ClaimDebug", "Parsed BigDecimal: " + qtyBd.toString());

                    // Check if is integer (scale <= 0 or stripTrailingZeros scale <= 0)
                    // Simple check: remainder of division by 1 is 0
                    boolean isInteger = qtyBd.remainder(java.math.BigDecimal.ONE).compareTo(java.math.BigDecimal.ZERO) == 0;
                    android.util.Log.d("ClaimDebug", "isInteger: " + isInteger);

                    if (isInteger) {
                        int qtyToClaim = qtyBd.intValueExact();
                        android.util.Log.d("ClaimDebug", "qtyToClaim (int): " + qtyToClaim);
                        android.util.Log.d("ClaimDebug", "Available: " + foodItem.getQuantity());
                        
                        if (qtyToClaim > 0 && qtyToClaim <= foodItem.getQuantity()) {
                            performClaimTransaction(qtyToClaim);
                            dialog.dismiss();
                        } else {
                            android.util.Log.e("ClaimDebug", "Quantity out of bounds");
                            Toast.makeText(getContext(), "Invalid quantity (1-" + foodItem.getQuantity() + ")",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        android.util.Log.e("ClaimDebug", "Not a whole number");
                         Toast.makeText(getContext(), "Please enter a whole number", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    android.util.Log.e("ClaimDebug", "NumberFormatException: " + e.getMessage());
                    Toast.makeText(getContext(), "Invalid number format", Toast.LENGTH_SHORT).show();
                } catch (ArithmeticException e) {
                     android.util.Log.e("ClaimDebug", "ArithmeticException (Overflow?): " + e.getMessage());
                     Toast.makeText(getContext(), "Invalid quantity", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Enter quantity", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    /**
     * Executes the Firestore transaction to claim the donation item.
     * Updates "donations" quantity/status and creates a "claims" record.
     */
    private void performClaimTransaction(int claimQty) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("donations").document(foodItem.getDonationId());

        String uid = FirebaseAuth.getInstance().getUid();
        String currentUserName = "Anonymous";
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String name = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
            if (name != null && !name.isEmpty())
                currentUserName = name;
        }
        final String finalUserName = currentUserName;

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

            // Transactional Validation
            if (status != null && "AVAILABLE".equals(status) &&
                    (endTime == null || endTime > currentTime) &&
                    currentQty >= claimQty) {

                // Calculate proportional weight
                double unitWeight = (currentQty > 0) ? (currentWeight / currentQty) : 0.0;
                double mainClaimedWeight = unitWeight * claimQty;

                int newQty = currentQty - claimQty;
                double newTotalWeight = currentWeight - mainClaimedWeight;
                
                // Ensure no negative weight due to floating point variations
                if (newTotalWeight < 0)
                    newTotalWeight = 0.0;

                transaction.update(docRef, "quantity", newQty);
                transaction.update(docRef, "weight", newTotalWeight);

                if (newQty == 0) {
                    transaction.update(docRef, "status", "CLAIMED");
                    transaction.update(docRef, "claimedBy", uid);
                }

                // Create Claim Record
                DocumentReference newClaimRef = db.collection("claims").document();
                Map<String, Object> claimData = new HashMap<>();
                claimData.put("donationId", foodItem.getDonationId());
                claimData.put("claimerId", uid);
                claimData.put("claimerName", finalUserName);
                claimData.put("quantityClaimed", claimQty);
                claimData.put("timestamp", currentTime);

                // Redundant Data for Impact Report optimization
                claimData.put("foodTitle", foodItem.getTitle());
                if (foodItem.getImageUri() != null) {
                    claimData.put("foodImage", foodItem.getImageUri());
                }
                claimData.put("location", foodItem.getLocationName());
                claimData.put("weight", mainClaimedWeight);
                claimData.put("category", foodItem.getCategory());

                transaction.set(newClaimRef, claimData);

                return null;
            } else {
                throw new FirebaseFirestoreException("Item unavailable or insufficient quantity",
                        FirebaseFirestoreException.Code.ABORTED);
            }
        }).addOnSuccessListener(result -> {
            if (isAdded()) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.coveringFragment, new ClaimNotifyFragment())
                        .addToBackStack("ClaimSuccess")
                        .commit();
            }
        }).addOnFailureListener(e -> {
            if (isAdded() && getContext() != null) {
                Toast.makeText(getContext(), "Claim failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupRealtimeUpdates() {
        if (foodItem == null || foodItem.getDonationId() == null)
            return;

        itemListener = FirebaseFirestore.getInstance().collection("donations")
                .document(foodItem.getDonationId())
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null)
                        return;

                    if (snapshot != null && snapshot.exists()) {
                        FoodItem updatedItem = snapshot.toObject(FoodItem.class);
                        if (updatedItem != null) {
                            updatedItem.setDonationId(snapshot.getId());
                            this.foodItem = updatedItem;

                            // Refresh UI
                            tvQuantity.setText(
                                    "Quantity: " + foodItem.getQuantity() + " (" + foodItem.getWeight() + " kg Total)");

                            if (foodItem.getQuantity() <= 0) {
                                Button btnClaim = getView().findViewById(R.id.btnClaim);
                                if (btnClaim != null)
                                    btnClaim.setEnabled(false);
                                tvQuantity.setText("Status: CLAIMED");
                            }
                        }
                    } else {
                        // Document deleted
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Item no longer exists", Toast.LENGTH_SHORT).show();
                            navigateBack();
                        }
                    }
                });
    }

    private void loadItemMap() {
        // Create a new MapFragment in "Single Item Mode"
        MapFragment mapFragment = new MapFragment();
        mapFragment.setSingleItemMode(foodItem);

        // Load it into the container
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.MapFragment, mapFragment)
                .commit();
    }

    private void checkDonorPrivacy() {
        if (foodItem == null || foodItem.getDonatorId() == null) return;

        FirebaseFirestore.getInstance().collection("users")
                .document(foodItem.getDonatorId())
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!isAdded()) return;

                    boolean isPrivate = false;
                    if (snapshot.exists() && snapshot.contains("isPrivate")) {
                        isPrivate = Boolean.TRUE.equals(snapshot.getBoolean("isPrivate"));
                    }

                    if (isPrivate) {
                        // Privacy enabled: Force Anonymous
                        tvPostedBy.setText(getString(R.string.Food_Donator, "Anonymous Donor"));
                    } else {
                        // Privacy disabled: Show Name
                        // PRIORITY: Use the name from the User Profile (most up to date)
                        // FALLBACK: Use the name from the Food Item (snapshot from when it was posted)
                        String displayDateName = foodItem.getDonatorName();

                        if (snapshot.exists() && snapshot.getString("name") != null) {
                            displayDateName = snapshot.getString("name");
                        }

                        tvPostedBy.setText(getString(R.string.Food_Donator, displayDateName));
                    }
                })
                .addOnFailureListener(e -> {
                    // If fetch fails, fallback to what is on the item object
                    if (isAdded()) {
                        tvPostedBy.setText(getString(R.string.Food_Donator, foodItem.getDonatorName()));
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (itemListener != null) {
            itemListener.remove();
        }
    }
}
