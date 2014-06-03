package com.noveogroup.screen_shot_report.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.noveogroup.screen_shot_report.controllers.RedMineControllerWrapper;
import com.noveogroup.screen_shot_report.utils.OptionsManager;
import com.noveogroup.screen_shot_report.utils.DialogUtils;
import com.noveogroup.screen_shot_report.controllers.PreferencesController;
import com.noveogroup.screen_shot_report.R;
import com.noveogroup.screen_shot_report.ReportData;
import com.noveogroup.screen_shot_report.adapters.AssigneeAdapter;
import com.noveogroup.screen_shot_report.adapters.IssueAdapter;
import com.noveogroup.screen_shot_report.adapters.StatusAdapter;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueStatus;
import com.taskadapter.redmineapi.bean.Membership;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by oisupov on 4/4/14.
 */
public class RedMineActivity extends ActionBarActivity {

    private EditText issue;
    private ListView issues;

    private Spinner status;
    private Spinner assignee;

    private RedMineControllerWrapper redMineControllerWrapper;

    private Project project;
    private CheckBox newTicket;
    private ReportData reportData;
    private Issue selectedIssue;
    private ProgressDialog progressDialog;

    private Logger logger = LoggerFactory.getLogger(RedMineActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        setContentView(R.layout.acitivity_redmine);

        if (PreferencesController.getPreferencesController(this).getServer() == null) {
            OptionsManager.setupOptions(this, new OptionsManager.OptionsSetupListener() {
                @Override
                public void onOptionsSatUp() {
                    loadProject();
                }

                @Override
                public void onCancel() {
                    finish();
                }

                @Override
                public void onError(Exception e) {
                    finish();
                }
            });
        } else {
            loadIssues();
        }
    }

    private void loadIssues() {
        progressDialog = DialogUtils.showProgressDialog(this);
        reportData = ReportData.getInstance();

        newTicket = (CheckBox) findViewById(R.id.new_ticket);

        newTicket.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                findViewById(R.id.existing_ticket_container).setVisibility(isChecked ? View.GONE : View.VISIBLE);
            }
        });

        issues = (ListView) findViewById(R.id.issues);
        issue = (EditText) findViewById(R.id.issue);

        issues.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                issues.setSelection(position);
                onSelectIssue((Issue) parent.getItemAtPosition(position));
            }
        });

        status = (Spinner) findViewById(R.id.status);
        assignee = (Spinner) findViewById(R.id.assignee);

        assignee.setAdapter(new AssigneeAdapter(getApplicationContext()));
        status.setAdapter(new StatusAdapter(getApplicationContext()));

        issue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                issues.setFilterText(s.toString());
            }
        });

        setupManager();
        loadProject();

        ((TextView) findViewById(R.id.title)).setText(reportData.getTitle());
        ((TextView) findViewById(R.id.message)).setText(reportData.getMessage());
    }

    private void loadProject() {
        redMineControllerWrapper.getListOfProjects(new RedMineControllerWrapper.GetProjectsListener() {

            @Override
            public void onSuccess(List<Project> projects) {
                super.onSuccess(projects);
                afterLoadProjectList(projects);
            }

            @Override
            public void onFail(Exception e) {
                super.onFail(e);
                showError();
            }
        });
    }

    private void afterLoadProjectList(final List<Project> projects) {
        project = findProject(projects, PreferencesController.getPreferencesController(this).getCurrentProject());

        redMineControllerWrapper.getListOfIssues(project, new RedMineControllerWrapper.GetIssuesListener() {
            @Override
            public void onSuccess(List<Issue> issue) {
                super.onSuccess(issue);
                IssueAdapter issueArrayAdapter = new IssueAdapter(RedMineActivity.this);
                issueArrayAdapter.addAll(issue);
                issues.setAdapter(issueArrayAdapter);
            }

            @Override
            public void onFail(Exception e) {
                super.onFail(e);
                showError();
            }
        });

        redMineControllerWrapper.getMemberships(project, new RedMineControllerWrapper.GetMembershipsListener() {

            @Override
            public void onSuccess(List<Membership> memberships) {
                super.onSuccess(memberships);
                AssigneeAdapter memberShipAdapter = new AssigneeAdapter(RedMineActivity.this);
                memberShipAdapter.addAll(memberships);
                assignee.setAdapter(memberShipAdapter);
            }

            @Override
            public void onFail(Exception e) {
                super.onFail(e);
                showError();
            }
        });

        redMineControllerWrapper.getStatuses(new RedMineControllerWrapper.GetStatusesListener() {

            @Override
            public void onSuccess(List<IssueStatus> issueStatuses) {
                super.onSuccess(issueStatuses);
                StatusAdapter statusArrayAdapter = new StatusAdapter(RedMineActivity.this);
                statusArrayAdapter.addAll(issueStatuses);
                status.setAdapter(statusArrayAdapter);
                progressDialog.dismiss();
            }

            @Override
            public void onFail(Exception e) {
                super.onFail(e);
                showError();
            }
        });
    }

    private void checkIfLoaded() {

    }

    private void showError() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private void onSelectIssue(final Issue selectedIssue) {
        this.selectedIssue = selectedIssue;
        final User issueAssignee = selectedIssue.getAssignee();
        final int statusId = selectedIssue.getStatusId();

        SpinnerAdapter assigneeAdapter = assignee.getAdapter();
        for (int i = 0; i < assigneeAdapter.getCount(); i++) {
            Membership item = (Membership) assigneeAdapter.getItem(i);
            if (item.getUser().getId().equals(issueAssignee.getId())) {
                assignee.setSelection(i);
            }
        }

        SpinnerAdapter statusAdapter = status.getAdapter();
        for (int i = 0; i < statusAdapter.getCount(); i++) {
            IssueStatus item = (IssueStatus) statusAdapter.getItem(i);
            if (item.getId().equals(statusId)) {
                status.setSelection(i);
            }
        }

    }

    private Project findProject(final List<Project> projects, final int id) {
        for (final Project project : projects) {
            if (project.getId() == id) {
                return project;
            }
        }
        return null;
    }

    private Project findProject(final List<Project> projects, final String identifier) {
        for (final Project project : projects) {
            if (TextUtils.equals(project.getName(), identifier)) {
                return project;
            }
        }
        return null;
    }

    private void setupManager() {
        PreferencesController preferencesController = PreferencesController.getPreferencesController(this);
        String server = preferencesController.getServer();
        String login = preferencesController.getLogin();
        String password = preferencesController.getPassword();
        redMineControllerWrapper = new RedMineControllerWrapper(login, password, server);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_redmine, menu);
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
        } else if (item.getItemId() == R.id.options) {
            OptionsManager.setupOptions(this, new OptionsManager.OptionsSetupListener() {
                @Override
                public void onOptionsSatUp() {
                    loadIssues();
                }

                @Override
                public void onCancel() {
                }

                @Override
                public void onError(Exception e) {
                }
            });
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void share() {
        if (newTicket.isChecked()) {
            redMineControllerWrapper.postNewIssue(project,
                    ((Membership) assignee.getSelectedItem()).getUser(),
                    ((IssueStatus) status.getSelectedItem()).getName(),
                    reportData.getTitle(),
                    reportData.getMessage(),
                    reportData.isIncludeScreenShot() ? reportData.getScreenShotPath() : null,
                    reportData.isIncludeLogs() ? reportData.getLogPath() : null,
                    new RedMineControllerWrapper.EditIssueListener() {
                        @Override
                        public void onSuccess(Issue issue) {
                            super.onSuccess(issue);
                            Toast.makeText(RedMineActivity.this, getString(R.string.redmine_success), 1000).show();
                        }

                        @Override
                        public void onFail(Exception e) {
                            super.onFail(e);
                            logger.trace("share fail", e);
                            Toast.makeText(RedMineActivity.this, getString(R.string.redmine_fail), 1000).show();
                        }
                    }
            );
        } else if (selectedIssue != null) {
            redMineControllerWrapper.postCommentToTicket(selectedIssue,
                    ((Membership) assignee.getSelectedItem()).getUser(),
                    ((IssueStatus) status.getSelectedItem()).getName(),
                    reportData.getTitle(),
                    reportData.getMessage(),
                    reportData.isIncludeScreenShot() ? reportData.getScreenShotPath() : null,
                    reportData.isIncludeLogs() ? reportData.getLogPath() : null,
                    new RedMineControllerWrapper.EditIssueListener() {
                        @Override
                        public void onSuccess(Issue issue) {
                            super.onSuccess(issue);
                            Toast.makeText(RedMineActivity.this, getString(R.string.redmine_success), 1000).show();
                        }

                        @Override
                        public void onFail(Exception e) {
                            super.onFail(e);
                            Toast.makeText(RedMineActivity.this, getString(R.string.redmine_fail), 1000).show();
                        }
                    }
            );
        } else {
            Toast.makeText(RedMineActivity.this, getString(R.string.redmine_select_ticket), 1000).show();
        }
    }


}


