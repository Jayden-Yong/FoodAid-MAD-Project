package com.example.foodaid_mad_project.Utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.foodaid_mad_project.MainActivity;
import com.example.foodaid_mad_project.R;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.concurrent.ExecutionException;

/**
 * <h1>NotificationWorker</h1>
 * <p>
 * Background worker for checking new notifications periodically.
 * Useful for polling global notifications (e.g., new donations) even when the
 * app is in the background.
 * Uses Firestore queries to find messages created after the last check time.
 * </p>
 */
public class NotificationWorker extends Worker {

    private static final String CHANNEL_ID = "FoodAid_Global";
    private static final String PREFS_NAME = "FoodAidPrefs";
    private static final String LAST_CHECK_KEY = "lastNotificationCheck";

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d("NotificationWorker", "Checking for new notifications...");

        if (FirebaseApp.getApps(getApplicationContext()).isEmpty()) {
            FirebaseApp.initializeApp(getApplicationContext());
        }

        SharedPreferences prefs = getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long lastCheckTime = prefs.getLong(LAST_CHECK_KEY, System.currentTimeMillis() - 15 * 60 * 1000);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        try {
            // Synchronously fetch notifications created > lastCheckTime
            QuerySnapshot querySnapshot = Tasks.await(db.collection("notifications")
                    .whereEqualTo("userId", "ALL") // Check for Global notifications
                    .whereGreaterThan("timestamp", lastCheckTime)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(1) // Only need one to trigger alert
                    .get());

            if (!querySnapshot.isEmpty()) {
                String title = "New Donation Available!";
                String message = "Someone nearby has posted a donation.";

                if (querySnapshot.getDocuments().get(0).contains("message")) {
                    message = querySnapshot.getDocuments().get(0).getString("message");
                }

                showSystemNotification(title, message);
            } else {
                Log.d("NotificationWorker", "No new notifications found.");
            }

            // Update local time to now
            prefs.edit().putLong(LAST_CHECK_KEY, System.currentTimeMillis()).apply();

        } catch (ExecutionException | InterruptedException e) {
            Log.e("NotificationWorker", "Error fetching notifications", e);
            return Result.retry();
        }

        return Result.success();
    }

    private void showSystemNotification(String title, String message) {
        Context context = getApplicationContext();
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "New Donations",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Notifications for new food donations");
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(1001, builder.build());
    }
}
