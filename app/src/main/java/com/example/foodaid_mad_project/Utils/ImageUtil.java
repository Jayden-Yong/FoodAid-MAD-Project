package com.example.foodaid_mad_project.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageUtil {

    private static final int MAX_DIMENSION = 500;
    private static final int COMPRESSION_QUALITY = 50; // 0-100

    /**
     * Converts a Uri to a compressed Base64 String.
     */
    public static String uriToBase64(Context context, Uri imageUri) throws IOException {
        Bitmap bitmap = getBitmapFromUri(context, imageUri);
        return bitmapToBase64(bitmap);
    }

    /**
     * Converts a Bitmap to a compressed Base64 String.
     * Resizes the bitmap if it's too large.
     */
    public static String bitmapToBase64(Bitmap bitmap) {
        if (bitmap == null)
            return null;

        // 1. Resize
        bitmap = resizeBitmap(bitmap);

        // 2. Compress to Bytes
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        // 3. Encode to Base64
        return Base64.encodeToString(byteArray, Base64.DEFAULT); // Default = includes newlines, might want NO_WRAP?
        // Glide likes DEFAULT usually, but NO_WRAP is safer for transport. Let's use
        // DEFAULT for now as it handles newlines.
        // Actually, NO_WRAP is safer for Firestore strings to avoid weird parsing
        // issues if manual.
        // Let's stick to standard `encodeToString` which defaults to standard Base64.
    }

    /**
     * Decodes a Base64 String back to a Bitmap.
     */
    public static Bitmap base64ToBitmap(String base64Str) {
        try {
            byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Helper to get byte[] for Glide to load easily.
     */
    public static byte[] base64ToBytes(String base64Str) {
        try {
            return Base64.decode(base64Str, Base64.DEFAULT);
        } catch (IllegalArgumentException e) {
            return new byte[0];
        }
    }

    private static Bitmap getBitmapFromUri(Context context, Uri uri) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.getContentResolver(), uri),
                    (decoder, info, source) -> {
                        decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE); // Flexible for mutable copies if needed
                    });
        } else {
            return MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
        }
    }

    private static Bitmap resizeBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = MAX_DIMENSION;
            height = (int) (width / bitmapRatio);
        } else {
            height = MAX_DIMENSION;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }
}
