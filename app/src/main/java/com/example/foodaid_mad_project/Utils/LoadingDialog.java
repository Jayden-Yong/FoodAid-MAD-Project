package com.example.foodaid_mad_project.Utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import com.example.foodaid_mad_project.R;

/**
 * <h1>LoadingDialog</h1>
 * <p>
 * A custom dialog to show a loading indicator during long-running operations.
 * Displays a transparent background with a centered layout (usually a
 * ProgressBar).
 * </p>
 */
public class LoadingDialog {
    private Dialog dialog;

    public LoadingDialog(Context context) {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_loading);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.setCancelable(false); // User cannot dismiss by tapping outside
    }

    /**
     * Shows the loading dialog. Safe to call multiple times.
     */
    public void show() {
        try {
            if (!dialog.isShowing())
                dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Dismisses the loading dialog.
     */
    public void dismiss() {
        try {
            if (dialog.isShowing())
                dialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
