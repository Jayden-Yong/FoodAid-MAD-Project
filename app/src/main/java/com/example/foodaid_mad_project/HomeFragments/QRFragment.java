package com.example.foodaid_mad_project.HomeFragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.foodaid_mad_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CompoundBarcodeView;

import java.util.List;

public class QRFragment extends Fragment {

    private TextView tvUserName, tvUserId, tvUserCode;
    private RadioButton btnTabMyQr, btnTabScanQr;
    private View containerMyQr, containerScanQr;
    private ImageView ivQrCode;
    private CompoundBarcodeView barcodeScanner;

    private boolean isScanning = false;
    private static final int CAMERA_PERMISSION_CODE = 101;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_qr, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Views
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserId = view.findViewById(R.id.tvUserId);
        tvUserCode = view.findViewById(R.id.tv_user_code);
        btnTabMyQr = view.findViewById(R.id.btn_tab_my_qr);
        btnTabScanQr = view.findViewById(R.id.btn_tab_scan_qr);
        containerMyQr = view.findViewById(R.id.container_my_qr);
        containerScanQr = view.findViewById(R.id.container_scan_qr);
        ivQrCode = view.findViewById(R.id.iv_qr_code);
        barcodeScanner = view.findViewById(R.id.barcode_scanner);

        TextView toolBarTitle = view.findViewById(R.id.toolbarTitle);
        toolBarTitle.setText(getString(R.string.String, "QR Page"));

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String username = (user != null) ? user.getEmail().substring(0, user.getEmail().indexOf("@")).toUpperCase() : "Anonymous";
        String userId = (user != null) ? user.getUid() : "Anonymous";
        tvUserName.setText(username);
        tvUserId.setText(userId);

        // Generate "My QR" immediately
        generateMyQRCode("USER-12345-AUTH-TOKEN"); // Replace with actual User ID or Data
        tvUserCode.setText("USER-12345-AUTH-TOKEN");

        // Set Tab Listeners
        btnTabMyQr.setOnClickListener(v -> switchTab(true));
        btnTabScanQr.setOnClickListener(v -> switchTab(false));

        // System back button
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Manually pop the Donate Fragment
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack("ItemDetail", FragmentManager.POP_BACK_STACK_INCLUSIVE);
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

        // Initialize Scanner Callback
        barcodeScanner.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result.getText() != null) {
                    // Logic when a QR code is scanned
                    barcodeScanner.pause(); // Pause to prevent multiple scans
                    Toast.makeText(getContext(), "Scanned: " + result.getText(), Toast.LENGTH_SHORT).show();

                    // TODO: Handle the scanned data (e.g., navigate to item detail or confirm claim)

                    // To resume scanning after handling:
                    // barcodeScanner.resume();
                }
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) { }
        });
    }

    private void switchTab(boolean showMyQr) {
        if (showMyQr) {
            // UI Styling for "My QR" Active
            containerMyQr.setVisibility(View.VISIBLE);
            containerScanQr.setVisibility(View.GONE);

            btnTabMyQr.setBackgroundResource(R.drawable.bg_donate_type_btn); // Active Drawable
            btnTabMyQr.setTextColor(Color.WHITE);

            btnTabScanQr.setBackgroundColor(Color.TRANSPARENT);
            btnTabScanQr.setTextColor(Color.parseColor("#808080"));

            pauseScanner();
        } else {
            // UI Styling for "Scan QR" Active
            containerMyQr.setVisibility(View.GONE);
            containerScanQr.setVisibility(View.VISIBLE);

            btnTabScanQr.setBackgroundResource(R.drawable.bg_donate_type_btn); // Active Drawable
            btnTabScanQr.setTextColor(Color.WHITE);

            btnTabMyQr.setBackgroundColor(Color.TRANSPARENT);
            btnTabMyQr.setTextColor(Color.parseColor("#808080"));

            checkPermissionAndStartScanner();
        }
    }

    private void generateMyQRCode(String data) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(data, BarcodeFormat.QR_CODE, 600, 600);
            ivQrCode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    private void checkPermissionAndStartScanner() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            resumeScanner();
        }
    }

    private void resumeScanner() {
        if (!isScanning) {
            barcodeScanner.resume();
            isScanning = true;
        }
    }

    private void pauseScanner() {
        if (isScanning) {
            barcodeScanner.pause();
            isScanning = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (containerScanQr.getVisibility() == View.VISIBLE) {
            resumeScanner();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        pauseScanner();
    }
}