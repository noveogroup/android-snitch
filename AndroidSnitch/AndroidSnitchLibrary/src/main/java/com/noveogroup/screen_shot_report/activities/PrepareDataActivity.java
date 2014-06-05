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

    private ScreenShotFragment screenShotFragment;
    private LogEditFragment logEditFragment;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(getString(R.string.prepare));

        setContentView(R.layout.fragment_content_activity);

        final ActionBar supportActionBar = getSupportActionBar();

        supportActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        supportActionBar.setHomeButtonEnabled(true);

        if (savedInstanceState == null) {

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

        } else {
            screenShotFragment = (ScreenShotFragment) getSupportFragmentManager().findFragmentByTag("screenShotFragment");
            logEditFragment = (LogEditFragment) getSupportFragmentManager().findFragmentByTag( "logEditFragment");
        }

        supportActionBar.setDisplayHomeAsUpEnabled(true);

        supportActionBar.addTab(supportActionBar.newTab().setText(getString(R.string.screen_shot_tab)).setTag("screen_shot")
                .setTabListener(this));

        supportActionBar.addTab(supportActionBar.newTab().setText(getString(R.string.log_tab)).setTag("log")
                .setTabListener(this));

        supportActionBar.getTabAt(0).select();
    }

    private void share() {
        final Bitmap picture = screenShotFragment.saveResult();
        try {
            File pictureFile = IOUtils.writeBitmap(getApplicationContext(), picture, IOUtils.DEFAULT_DIR + "/" + "modified_picture.png");
            File logFile = IOUtils.writeLogs(getApplicationContext(), logEditFragment.getResult(), IOUtils.DEFAULT_DIR + "/" + "modified_logs.txt");

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
        fragmentTransaction.show(logEditFragment).hide(screenShotFragment);
    }

    private void showScreenShotFragment(FragmentTransaction fragmentTransaction) {
        fragmentTransaction.show(screenShotFragment).hide(logEditFragment);
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
