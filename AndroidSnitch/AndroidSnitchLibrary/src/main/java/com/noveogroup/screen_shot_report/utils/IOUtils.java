package com.noveogroup.screen_shot_report.utils;

import android.content.Context;
import android.graphics.Bitmap;

import org.apache.http.util.ByteArrayBuffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Created by oisupov on 4/5/14.
 */
public final class IOUtils {

    public static final String DEFAULT_DIR = "/report_data";
    public static final String DEFAULT_LOG_PATH = "/report_data/report_logs.txt";
    public static final String DEFAULT_SCREEN_SHOT_PATH = "/report_data/report_screen_shot.png";
    public static final String AUTHORITY = "com.noveogroup.screen_shot_report.provider";

    private IOUtils() {
        throw new UnsupportedOperationException();
    }

    private static void checkDir(final Context context) {
        final File file = new File(context.getFilesDir(), DEFAULT_DIR);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public static File writeBitmap(final Context context, final Bitmap bitmap, final String path) throws IOException {
        final FileOutputStream fos;
        try {
            checkDir(context);
            final File file = new File(context.getFilesDir(), path);
            file.delete();
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            return file;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static File writeLogs(final Context context, final String logs, final String path) throws IOException {
        final FileOutputStream fos;
        try {
            checkDir(context);
            final File file = new File(context.getFilesDir(), path);
            file.delete();
            fos = new FileOutputStream(file);
            fos.write(logs.getBytes());
            fos.close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static String readLogs(final Context context, final String path) throws IOException {
        File file = new File(path);
        InputStream inputStream = new FileInputStream(file);
        final byte[] buffer = new byte[1024];
        ByteArrayBuffer byteBuffer = new ByteArrayBuffer(2048);
        int r = 0;
        while ((r = inputStream.read(buffer)) == 1024) {
            byteBuffer.append(buffer, 0, r);
        }
        byteBuffer.append(buffer, 0, r);
        return new String(byteBuffer.toByteArray());
    }

}
