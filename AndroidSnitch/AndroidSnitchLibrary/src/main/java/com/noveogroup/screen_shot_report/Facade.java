package com.noveogroup.screen_shot_report;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.DisplayMetrics;
import android.view.KeyEvent;

import com.noveogroup.screen_shot_report.activities.CrashReportActivity;
import com.noveogroup.screen_shot_report.activities.PrepareDataActivity;
import com.noveogroup.screen_shot_report.fragments.LogEditFragment;
import com.noveogroup.screen_shot_report.fragments.ScreenShotFragment;
import com.noveogroup.screen_shot_report.utils.IOUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import static android.graphics.Bitmap.Config.ARGB_8888;

/**
 * Created by oisupov on 4/2/14.
 */
public class Facade {

    private static boolean uncaughtExceptionHandlerSatUp = false;

    public static boolean onKeyDown(final int keyCode, final KeyEvent event, final Activity activity) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                shoot(activity);
                return true;
        }
        return false;
    }

    private static void drawDecorViewToBitmap(final Activity activity, final Bitmap bitmap) {
        final Canvas canvas = new Canvas(bitmap);
        activity.getWindow().getDecorView().draw(canvas);
    }

    private static Bitmap takeScreenShot(final Activity activity) {
        DisplayMetrics dm = activity.getResources().getDisplayMetrics();
        final Bitmap bitmap = Bitmap.createBitmap(dm.widthPixels, dm.heightPixels, ARGB_8888);
        drawDecorViewToBitmap(activity, bitmap);
        return bitmap;
    }

    public static synchronized void setupUncaughtExceptionHandler(final Context context) {
        if (!uncaughtExceptionHandlerSatUp) {
            final Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler
                    = Thread.getDefaultUncaughtExceptionHandler();
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable ex) {
                    Facade.reportCrash(context.getApplicationContext(), ex);
                    defaultUncaughtExceptionHandler.uncaughtException(thread, ex);
                }
            });
        }
    }

    private static String getLogs(final Context context) {
        try {
            int counter = 0;
            final Process logcat = Runtime.getRuntime().exec(new String[]
                    {"logcat", "-d"});//, String.format("AndroidRuntime:E %s:V *:S",  con) });

            final BufferedReader reader = new BufferedReader(new InputStreamReader
                    (logcat.getInputStream()));

            StringBuilder log = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null && counter++ < 100) {
                log.append(line).append('\n');
            }
            return log.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void reportCrash(final Context context, final Throwable e) {
        try {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(out));
            final String logs = out.toString();
            out.close();

            File logsFile = IOUtils.writeLogs(context, logs, IOUtils.DEFAULT_LOG_PATH);

            context.startActivity(new Intent(context, CrashReportActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                    .putExtra(LogEditFragment.ARGUMENT_LOGS_URI, logsFile.getPath()));

        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public static void shoot(final Activity activity) {
        final Bitmap bitmap = takeScreenShot(activity);
        final String logs = getLogs(activity.getApplicationContext());

        try {
            final File screenShotFile = IOUtils.writeBitmap(activity, bitmap, IOUtils.DEFAULT_SCREEN_SHOT_PATH);
            final File logsFile = IOUtils.writeLogs(activity, logs, IOUtils.DEFAULT_LOG_PATH);
            activity.startActivity(new Intent(activity, PrepareDataActivity.class)
                    .putExtra(ScreenShotFragment.ARGUMENT_SCREEN_SHOT_URI, screenShotFile.getPath())
                    .putExtra(LogEditFragment.ARGUMENT_LOGS_URI, logsFile.getPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
