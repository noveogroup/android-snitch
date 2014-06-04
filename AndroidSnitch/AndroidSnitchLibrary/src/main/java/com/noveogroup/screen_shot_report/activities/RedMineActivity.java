package com.noveogroup.screen_shot_report.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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

import com.noveogroup.screen_shot_report.R;
import com.noveogroup.screen_shot_report.ReportData;
import com.noveogroup.screen_shot_report.adapters.AssigneeAdapter;
import com.noveogroup.screen_shot_report.adapters.IssueAdapter;
import com.noveogroup.screen_shot_report.adapters.StatusAdapter;
import com.noveogroup.screen_shot_report.controllers.PreferencesController;
import com.noveogroup.screen_shot_report.controllers.RedMineControllerWrapper;
import com.noveogroup.screen_shot_report.utils.DialogUtils;
import com.noveogroup.screen_shot_report.utils.OptionsManager;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueStatus;
import com.taskadapter.redmineapi.bean.Membership;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func3;

/**
 * Created by oisupov on 4/4/14.
 */
public class RedMineActivity extends ActionBarActivity {

    private List<Subscription> subscriptions = new Vector<Subscription>();

    private EditText issue;
    private ListView issuesView;

    private Spinner status;
    private Spinner assignee;

    private volatile RedMineControllerWrapper redMineControllerWrapper;

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
                public void onError(Throwable e) {
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

        issuesView = (ListView) findViewById(R.id.issues);
        issue = (EditText) findViewById(R.id.issue);

        issuesView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                issuesView.setSelection(position);
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
                issuesView.setFilterText(s.toString());
            }
        });

        setupManager();
        loadProject();

        ((TextView) findViewById(R.id.title)).setText(reportData.getTitle());
        ((TextView) findViewById(R.id.message)).setText(reportData.getMessage());
    }

    private void loadProject() {
        Subscription subscribe = redMineControllerWrapper.getListOfProjects().subscribe(new Observer<List<Project>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                showError();
            }

            @Override
            public void onNext(List<Project> projects) {
                afterLoadProjectList(projects);
            }
        });
    }

    private void afterLoadProjectList(final List<Project> projects) {
        project = findProject(projects, PreferencesController.getPreferencesController(this).getCurrentProject());

        Observable<List<Issue>> getListOfIssues = redMineControllerWrapper.getListOfIssues(project);
        Observable<List<Membership>> getMemberShips = redMineControllerWrapper.getMemberships(project);
        Observable<List<IssueStatus>> getStatuses = redMineControllerWrapper.getStatuses();

        Subscription subscribe = Observable.zip(getListOfIssues, getStatuses, getMemberShips, new Func3<List<Issue>, List<IssueStatus>, List<Membership>, List[]>() {
            @Override
            public List[] call(List<Issue> issues, List<IssueStatus> issueStatuses, List<Membership> memberships) {
                return new List[]{issues, issueStatuses, memberships};
            }
        }).subscribe(new Subscriber<List[]>() {
            @Override
            public void onCompleted() {
                progressDialog.dismiss();
            }

            @Override
            public void onError(Throwable e) {
                showError();
            }

            @Override
            public void onNext(List[] o) {
                logger.debug("combineLatest");

                //Issues
                IssueAdapter issueArrayAdapter = new IssueAdapter(RedMineActivity.this);
                issueArrayAdapter.addAll(o[0]);
                issuesView.setAdapter(issueArrayAdapter);

                //IssueStatuses
                StatusAdapter statusArrayAdapter = new StatusAdapter(RedMineActivity.this);
                statusArrayAdapter.addAll(o[1]);
                status.setAdapter(statusArrayAdapter);

                //MemberShips
                AssigneeAdapter memberShipAdapter = new AssigneeAdapter(RedMineActivity.this);
                memberShipAdapter.addAll(o[2]);
                assignee.setAdapter(memberShipAdapter);
            }
        });

        manageSubscription(subscribe);
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
                public void onError(Throwable e) {
                }
            });
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void share() {
        if (newTicket.isChecked()) {
            Subscription subscribe = redMineControllerWrapper.postNewIssue(project,
                    ((Membership) assignee.getSelectedItem()).getUser(),
                    ((IssueStatus) status.getSelectedItem()).getName(),
                    reportData.getTitle(),
                    reportData.getMessage(),
                    reportData.isIncludeScreenShot() ? reportData.getScreenShotPath() : null,
                    reportData.isIncludeLogs() ? reportData.getLogPath() : null
            ).subscribe(new Subscriber<Issue>() {
                @Override
                public void onCompleted() {
                    Toast.makeText(RedMineActivity.this, getString(R.string.redmine_success), 1000).show();
                }

                @Override
                public void onError(Throwable e) {
                    Toast.makeText(RedMineActivity.this, getString(R.string.redmine_fail), 1000).show();
                }

                @Override
                public void onNext(Issue issue) {

                }
            });

            manageSubscription(subscribe);
        } else if (selectedIssue != null) {
            Subscription subscribe = redMineControllerWrapper.postCommentToTicket(selectedIssue,
                    ((Membership) assignee.getSelectedItem()).getUser(),
                    ((IssueStatus) status.getSelectedItem()).getName(),
                    reportData.getTitle(),
                    reportData.getMessage(),
                    reportData.isIncludeScreenShot() ? reportData.getScreenShotPath() : null,
                    reportData.isIncludeLogs() ? reportData.getLogPath() : null).subscribe(new Subscriber<Issue>() {
                @Override
                public void onCompleted() {
                    Toast.makeText(RedMineActivity.this, getString(R.string.redmine_success), 1000).show();
                }

                @Override
                public void onError(Throwable e) {
                    Toast.makeText(RedMineActivity.this, getString(R.string.redmine_fail), 1000).show();
                }

                @Override
                public void onNext(Issue issue) {

                }
            });

            manageSubscription(subscribe);
        } else {
            Toast.makeText(RedMineActivity.this, getString(R.string.redmine_select_ticket), 1000).show();
        }
    }

    private void manageSubscription(final Subscription subscribe) {
        subscriptions.add(subscribe);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Iterator<Subscription> iterator = subscriptions.iterator();
        while (iterator.hasNext()) {
            Subscription next = iterator.next();
            next.unsubscribe();
            iterator.remove();
        }
    }
}


