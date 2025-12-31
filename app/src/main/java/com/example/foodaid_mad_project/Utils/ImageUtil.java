package com.example.foodaid_mad_project.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * ImageUtil
 *
 * Utility class for handling image processing tasks.
 * Primarily used for:
 * - Compressing large images to avoid OutOfMemory errors.
 * - Converting Bitmaps/URIs to Base64 strings for Firestore storage.
 * - Decoding Base64 strings back to Bitmaps for display.
 */
public class ImageUtil {

    private static final int MAX_DIMENSION = 800; // Max width/height in pixels
    private static final int COMPRESSION_QUALITY = 70; // JPEG quality 0-100

    /**
     * Converts a Uri to a compressed Base64 String.
     * Safely loads and resizes the image to avoid OOM.
     *
     * @param context  Context to access ContentResolver
     * @param imageUri Uri of the image to process
     * @return Base64 encoded string of the compressed image
     * @throws IOException If image loading fails
     */
    public static String uriToBase64(Context context, Uri imageUri) throws IOException {
        Bitmap bitmap = decodeSampledBitmapFromUri(context, imageUri, MAX_DIMENSION, MAX_DIMENSION);
        if (bitmap == null)
            return null;
        return bitmapToBase64(bitmap);
    }

    /**
     * Converts a Bitmap to a compressed Base64 String.
     *
     * @param bitmap Source Bitmap
     * @return Base64 encoded string
     */
    public static String bitmapToBase64(Bitmap bitmap) {
        if (bitmap == null)
            return null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    /**
     * Decodes a Base64 string into a Bitmap.
     *
     * @param base64Str Base64 encoded string
     * @return Bitmap or null if decoding fails
     */
    public static Bitmap base64ToBitmap(String base64Str) {
        if (base64Str == null || base64Str.isEmpty())
            return null;
        try {
            byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (IllegalArgumentException e) {
            Log.e("ImageUtil", "Base64 decode error", e);
            return null;
        }
    }

    /**
     * Decodes a Base64 string into a byte array (for Glide loading).
     *
     * @param base64Str Base64 encoded string
     * @return Byte array
     */
    public static byte[] base64ToBytes(String base64Str) {
        if (base64Str == null || base64Str.isEmpty())
            return new byte[0];
        try {
            return Base64.decode(base64Str, Base64.DEFAULT);
        } catch (IllegalArgumentException e) {
            return new byte[0];
        }
    }

    /**
     * Decodes a bitmap from a URI, scaling it down to the requested dimensions.
     */
    private static Bitmap decodeSampledBitmapFromUri(Context context, Uri uri, int reqWidth, int reqHeight)
            throws IOException {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        try (InputStream stream = context.getContentResolver().openInputStream(uri)) {
            BitmapFactory.decodeStream(stream, null, options);
        }

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        try (InputStream stream = context.getContentResolver().openInputStream(uri)) {
            return BitmapFactory.decodeStream(stream, null, options);
        }
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
