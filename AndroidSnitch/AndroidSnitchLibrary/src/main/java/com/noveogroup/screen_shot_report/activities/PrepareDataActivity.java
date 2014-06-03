package com.noveogroup.screen_shot_report.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.noveogroup.screen_shot_report.R;
import com.noveogroup.screen_shot_report.ReportData;
import com.noveogroup.screen_shot_report.fragments.LogEditFragment;
import com.noveogroup.screen_shot_report.fragments.ScreenShotFragment;
import com.noveogroup.screen_shot_report.utils.IOUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;


/**
 * Created by oisupov on 4/1/14.
 */
public class PrepareDataActivity extends ActionBarActivity implements ActionBar.TabListener {

    private Logger logger = LoggerFactory.getLogger(PrepareDataActivity.class);

    public static final String SAVED_STATE_SCREEN_SHOT_FRAGMENT = "SAVED_STATE_SCREEN_SHOT_FRAGMENT";
    public static final String SAVED_STATE_LOG_EDIT_FRAGMENT = "SAVED_STATE_LOG_EDIT_FRAGMENT";

    private ScreenShotFragment screenShotFragment;
    private LogEditFragment logEditFragment;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_content_activity);

        final ActionBar supportActionBar = getSupportActionBar();

        supportActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        supportActionBar.setHomeButtonEnabled(true);

        final Bundle screenShotFragmentArguments = new Bundle();
        final Bundle logFragmentArguments = new Bundle();

        screenShotFragmentArguments.putString(ScreenShotFragment.ARGUMENT_SCREEN_SHOT_URI, getIntent().getStringExtra(ScreenShotFragment.ARGUMENT_SCREEN_SHOT_URI));
        logFragmentArguments.putString(LogEditFragment.ARGUMENT_LOGS_URI, getIntent().getStringExtra(LogEditFragment.ARGUMENT_LOGS_URI));

        screenShotFragment = (ScreenShotFragment) Fragment.instantiate(this, ScreenShotFragment.class.getName(), screenShotFragmentArguments);
        logEditFragment = (LogEditFragment) Fragment.instantiate(this, LogEditFragment.class.getName(), logFragmentArguments);

        getSupportFragmentManager()
                .beginTransaction()
                .add(android.R.id.content, screenShotFragment, "screenShotFragment")
                .add(android.R.id.content, logEditFragment, "logEditFragment")
                .commit();

        if (savedInstanceState != null) {
            screenShotFragment.setInitialSavedState((Fragment.SavedState) savedInstanceState.getParcelable(SAVED_STATE_SCREEN_SHOT_FRAGMENT));
            logEditFragment.setInitialSavedState((Fragment.SavedState) savedInstanceState.getParcelable(SAVED_STATE_LOG_EDIT_FRAGMENT));
        }

        supportActionBar.setDisplayHomeAsUpEnabled(true);

        supportActionBar.addTab(supportActionBar.newTab().setText(getString(R.string.screen_shot_tab)).setTag("screen_shot")
                .setTabListener(this));

        supportActionBar.addTab(supportActionBar.newTab().setText(getString(R.string.log_tab)).setTag("log")
                .setTabListener(this));

        supportActionBar.getTabAt(0).select();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        final Fragment.SavedState screenShotFragmentState = getSupportFragmentManager().saveFragmentInstanceState(screenShotFragment);
        final Fragment.SavedState logEditFragmentState = getSupportFragmentManager().saveFragmentInstanceState(logEditFragment);

        outState.putParcelable(SAVED_STATE_SCREEN_SHOT_FRAGMENT, screenShotFragmentState);
        outState.putParcelable(SAVED_STATE_LOG_EDIT_FRAGMENT, logEditFragmentState);
    }

    private void share() {
        final Bitmap picture = screenShotFragment.saveResult();
        try {
            File pictureFile = IOUtils.writeBitmap(getApplicationContext(), picture, IOUtils.DEFAULT_SCREEN_SHOT_PATH);
            File logFile = IOUtils.writeLogs(getApplicationContext(), logEditFragment.getResult(), IOUtils.DEFAULT_LOG_PATH);

            ReportData.getInstance().erase();
            ReportData.getInstance().setScreenShotPath(pictureFile.getPath());
            ReportData.getInstance().setLogPath(logFile.getPath());

            final Intent intent = new Intent(this, ComposeMessageActivity.class);
            startActivity(intent);
        } catch (IOException e) {
            logger.trace("Share failed", e);
        }
    }

    private void showLogFragment(FragmentTransaction fragmentTransaction) {
        fragmentTransaction.hide(screenShotFragment).show(logEditFragment);
    }

    private void showScreenShotFragment(FragmentTransaction fragmentTransaction) {
        fragmentTransaction.hide(logEditFragment).show(screenShotFragment);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        this.menu = menu;
        this.menu.clear();
        this.getMenuInflater().inflate(R.menu.menu_after_screen_shot, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.share) {
            share();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        if (screenShotFragment == null || logEditFragment == null) {
            return;
        }

        if (tab.getPosition() == 0) {
            showScreenShotFragment(fragmentTransaction);
        } else {
            showLogFragment(fragmentTransaction);
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        //Nothing
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        //Nothing
    }
}
