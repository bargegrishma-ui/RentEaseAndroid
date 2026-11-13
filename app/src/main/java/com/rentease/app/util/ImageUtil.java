package com.rentease.app.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;

import com.rentease.app.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageUtil {

    private static final ExecutorService executor = Executors.newFixedThreadPool(2);

    public static int getPresetDrawableId(String presetName) {
        if (presetName == null) return R.drawable.prop_real_2bhk;
        String lower = presetName.trim().toLowerCase();
        if (lower.contains("studio")) {
            return R.drawable.prop_real_studio;
        } else if (lower.contains("gala") || lower.contains("shop") || lower.contains("commercial")) {
            return R.drawable.prop_real_gala;
        } else if (lower.contains("villa")) {
            return R.drawable.prop_real_villa;
        } else if (lower.contains("bungalow")) {
            return R.drawable.prop_real_bungalow;
        } else if (lower.contains("4bhk") || lower.contains("penthouse")) {
            return R.drawable.prop_real_4bhk;
        } else if (lower.contains("3bhk")) {
            return R.drawable.prop_real_3bhk;
        } else if (lower.contains("1bhk") || lower.contains("bedroom") || lower.contains("suite")) {
            return R.drawable.prop_real_1bhk;
        } else if (lower.contains("2bhk") || lower.contains("flat") || lower.contains("apartment")) {
            return R.drawable.prop_real_2bhk;
        }
        return R.drawable.prop_real_2bhk;
    }

    public static String saveImageToInternalStorage(Context context, Uri sourceUri) {
        try {
            File dir = new File(context.getFilesDir(), "property_images");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String fileName = "prop_" + System.currentTimeMillis() + ".jpg";
            File destFile = new File(dir, fileName);

            InputStream is = context.getContentResolver().openInputStream(sourceUri);
            if (is == null) return null;

            FileOutputStream fos = new FileOutputStream(destFile);
            byte[] buffer = new byte[8192];
            int length;
            while ((length = is.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
            fos.flush();
            fos.close();
            is.close();

            return destFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void loadImage(ImageView imageView, String imagePath) {
        if (imageView == null) return;

        if (imagePath == null || imagePath.trim().isEmpty()) {
            imageView.setImageResource(R.drawable.prop_real_living);
            return;
        }

        final String path = imagePath.trim();

        // 1. Preset Drawables
        int presetId = getPresetDrawableId(path);
        if (presetId != 0 && !path.startsWith("http://") && !path.startsWith("https://") && !path.startsWith("content://") && !path.startsWith("/")) {
            imageView.setTag(null);
            imageView.setImageResource(presetId);
            return;
        }

        // 2. HTTP / HTTPS Web URLs
        if (path.startsWith("http://") || path.startsWith("https://")) {
            imageView.setTag(path);
            imageView.setImageResource(getPresetDrawableId(path));

            final Context context = imageView.getContext().getApplicationContext();
            executor.execute(() -> {
                try {
                    File cacheDir = new File(context.getCacheDir(), "img_cache");
                    if (!cacheDir.exists()) cacheDir.mkdirs();
                    String cacheKey = "cache_" + Math.abs(path.hashCode()) + ".jpg";
                    File cacheFile = new File(cacheDir, cacheKey);

                    if (!cacheFile.exists()) {
                        URL url = new URL(path);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setConnectTimeout(6000);
                        conn.setReadTimeout(6000);
                        conn.setDoInput(true);
                        conn.connect();

                        InputStream input = conn.getInputStream();
                        FileOutputStream fos = new FileOutputStream(cacheFile);
                        byte[] buf = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = input.read(buf)) != -1) {
                            fos.write(buf, 0, bytesRead);
                        }
                        fos.flush();
                        fos.close();
                        input.close();
                        conn.disconnect();
                    }

                    if (cacheFile.exists()) {
                        Bitmap bitmap = decodeSampledBitmap(cacheFile.getAbsolutePath(), 600, 600);
                        if (bitmap != null) {
                            imageView.post(() -> {
                                if (path.equals(imageView.getTag())) {
                                    imageView.setImageBitmap(bitmap);
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            return;
        }

        // 3. Content URIs (e.g. content://...)
        if (path.startsWith("content://")) {
            imageView.setTag(null);
            try {
                Uri uri = Uri.parse(path);
                InputStream is = imageView.getContext().getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                if (is != null) is.close();
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 4. Local File Path
        File file = new File(path);
        if (file.exists()) {
            imageView.setTag(null);
            try {
                Bitmap bitmap = decodeSampledBitmap(path, 600, 600);
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Fallback
        imageView.setImageResource(getPresetDrawableId(path));
    }

    private static Bitmap decodeSampledBitmap(String filePath, int reqWidth, int reqHeight) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, options);

            int scale = 1;
            while ((options.outWidth / scale / 2) >= reqWidth && (options.outHeight / scale / 2) >= reqHeight) {
                scale *= 2;
            }

            options.inJustDecodeBounds = false;
            options.inSampleSize = scale;
            return BitmapFactory.decodeFile(filePath, options);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
