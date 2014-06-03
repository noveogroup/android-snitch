package com.noveogroup.screen_shot_report;

/**
 * Created by oisupov on 4/5/14.
 */
public class ReportData {

    private String screenShotPath;
    private String logPath;
    private String title;
    private String message;
    private boolean includeLogs;
    private boolean includeScreenShot;

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isIncludeLogs() {
        return includeLogs;
    }

    public void setIncludeLogs(boolean includeLogs) {
        this.includeLogs = includeLogs;
    }

    public boolean isIncludeScreenShot() {
        return includeScreenShot;
    }

    public void setIncludeScreenShot(boolean includeScreenShot) {
        this.includeScreenShot = includeScreenShot;
    }

    private static volatile ReportData instance;

    public void erase() {
        screenShotPath = null;
        logPath = null;
        title = null;
        message = null;
        includeLogs = false;
        includeScreenShot = false;
    }

    public static ReportData getInstance() {
        if (instance == null) {
            synchronized (ReportData.class) {
                instance = new ReportData();
            }
        }
        return instance;
    }

    public String getScreenShotPath() {
        return screenShotPath;
    }

    public void setScreenShotPath(String screenShotPath) {
        this.screenShotPath = screenShotPath;
    }
}
