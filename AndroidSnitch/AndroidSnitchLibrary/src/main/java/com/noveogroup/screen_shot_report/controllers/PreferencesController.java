package com.noveogroup.screen_shot_report.controllers;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by oisupov on 4/10/14.
 */
public class PreferencesController {

    public static final String CURRENT_PROJECT = "CURRENT_PROJECT";
    public static final String PREFERENCES = PreferencesController.class.getName();
    public static final String LAST_TICKET = "LAST_TICKET";
    public static final String ATTACH_SCREEN_SHOT = "ATTACH_SCREEN_SHOT";
    public static final String ATTACH_LOGS = "ATTACH_LOGS";
    public static final String LOGIN = "LOGIN";
    public static final String SERVER = "SERVER";
    public static final String PASSWORD = "PASSWORD";
    private Context context;

    public PreferencesController(Context context) {
        this.context = context;
    }

    private static volatile PreferencesController preferencesController = null;

    public static PreferencesController getPreferencesController(final Context context) {
        if (preferencesController == null) {
            synchronized (PreferencesController.class) {
                preferencesController = new PreferencesController(context);
            }
        }
        return preferencesController;
    }

    public void setCurrentProject(final String project) {
        getSharedPreferences().edit().putString(CURRENT_PROJECT, project).commit();
    }

    public String getCurrentProject() {
        return getSharedPreferences().getString(CURRENT_PROJECT, null);
    }

    public int getLastTicket() {
       return getSharedPreferences().getInt(LAST_TICKET, -1);
    }

    public void setLastTicket(final int ticket) {
        getSharedPreferences().edit().putInt(LAST_TICKET, ticket).clear();
    }

    public void setAttachScreenShot(final boolean attachScreenShot) {
        getSharedPreferences().edit().putBoolean(ATTACH_SCREEN_SHOT, attachScreenShot).commit();
    }

    public void setAttachLogs(final boolean attachLogs) {
        getSharedPreferences().edit().putBoolean(ATTACH_LOGS, attachLogs).commit();
    }

    private SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    public boolean shouldAttachScreenShot() {
        return getSharedPreferences().getBoolean(ATTACH_SCREEN_SHOT, false);
    }

    public boolean shouldAttachLogs() {
        return getSharedPreferences().getBoolean(ATTACH_LOGS, false);
    }

    public void setLogin(final String login) {
        getSharedPreferences().edit().putString(LOGIN, login).commit();
    }

    public String getLogin() {
        return getSharedPreferences().getString(LOGIN, null);
    }

    public void setPassword(final String password) {
        getSharedPreferences().edit().putString(PASSWORD, password).commit();
    }

    public String getPassword() {
        return getSharedPreferences().getString(PASSWORD, null);
    }

    public void setServer(final String server) {
        getSharedPreferences().edit().putString(SERVER, server).commit();
    }

    public String getServer() {
        return getSharedPreferences().getString(SERVER, null);
    }


}
