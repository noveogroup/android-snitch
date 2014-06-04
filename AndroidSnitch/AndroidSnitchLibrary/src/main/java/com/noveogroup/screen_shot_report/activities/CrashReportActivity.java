package com.noveogroup.screen_shot_report.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.noveogroup.screen_shot_report.R;
import com.noveogroup.screen_shot_report.ReportData;
import com.noveogroup.screen_shot_report.fragments.LogEditFragment;
import com.noveogroup.screen_shot_report.utils.IOUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Created by oisupov on 4/8/14.
 */
public class CrashReportActivity extends ActionBarActivity {

    private Logger logger = LoggerFactory.getLogger(CrashReportActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(getString(R.string.report_crash));

        logger.debug("Crash report activity started");

        setContentView(R.layout.activity_crash_report);

        final ActionBar supportActionBar = getSupportActionBar();
        supportActionBar.setHomeButtonEnabled(true);
        supportActionBar.setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            final Bundle arguments = new Bundle();
            arguments.putString(LogEditFragment.ARGUMENT_LOGS_URI, getIntent().getStringExtra(LogEditFragment.ARGUMENT_LOGS_URI));
            LogEditFragment logEditFragment = (LogEditFragment) Fragment.instantiate(this, LogEditFragment.class.getName(), arguments);
            getSupportFragmentManager().beginTransaction().replace(R.id.content, logEditFragment, "logEditFragment").commit();
        }
    }

    private void share() {
        try {
            String log = ((LogEditFragment) getSupportFragmentManager().findFragmentByTag("logEditFragment")).getResult();
            File file = IOUtils.writeLogs(getApplicationContext(), log, IOUtils.DEFAULT_LOG_PATH);

            ReportData.getInstance().erase();
            ReportData.getInstance().setLogPath(file.getPath());

            final Intent intent = new Intent(this, ComposeMessageActivity.class);
            startActivity(intent);
        } catch (IOException e) {
            logger.trace("share fail", e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
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
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

}
