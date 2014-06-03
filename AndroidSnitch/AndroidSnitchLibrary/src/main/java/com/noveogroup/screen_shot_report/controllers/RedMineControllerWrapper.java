package com.noveogroup.screen_shot_report.controllers;

import android.os.AsyncTask;

import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.bean.Attachment;
import com.taskadapter.redmineapi.bean.Changeset;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueStatus;
import com.taskadapter.redmineapi.bean.Membership;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.User;

import java.io.File;
import java.util.List;

/**
 * Created by oisupov on 4/10/14.
 */
public class RedMineControllerWrapper {

    public static interface DataListener<T> {
        public void onSuccess(final T t);

        public void onFail(final Exception e);
    }

    public static class DataListenerDefaultImplementation<T> implements DataListener<T> {

        @Override
        public void onSuccess(T t) {

        }

        @Override
        public void onFail(Exception e) {

        }
    }

    public static class GetIssuesListener extends DataListenerDefaultImplementation<List<Issue>> {
    }

    public static class GetProjectsListener extends DataListenerDefaultImplementation<List<Project>> {
    }

    public static class EditIssueListener extends DataListenerDefaultImplementation<Issue> {
    }

    public static class GetMembershipsListener extends DataListenerDefaultImplementation<List<Membership>> {
    }

    public static class GetStatusesListener extends DataListenerDefaultImplementation<List<IssueStatus>> {
    }

    public abstract static class RedmineManagerTask<T> extends AsyncTask<Void, T, T> {

        private Exception e;
        private DataListener<T> dataListener;

        protected RedmineManagerTask(DataListener<T> dataListener) {
            this.dataListener = dataListener;
        }

        public void setDataListener(DataListener<T> dataListener) {
            this.dataListener = dataListener;
        }

        @Override
        protected T doInBackground(java.lang.Void... params) {
            try {
                T t = getData();
                return t;
            } catch (Exception e) {
                this.e = e;
                return null;
            }
        }

        protected abstract T getData() throws Exception;

        @Override
        protected void onPostExecute(T t) {
            super.onPostExecute(t);
            if (dataListener != null) {
                if (t != null) {
                    dataListener.onSuccess(t);
                } else {
                    dataListener.onFail(e);
                }
            } else {
                throw new RuntimeException("dataListener == null");
            }
        }
    }

    private String login;
    private String password;
    private String server;

    private RedmineManager redmineManager;

    public RedMineControllerWrapper(String login, String password, String server) {
        this.login = login;
        this.password = password;
        this.server = server;
        createManager();
    }

    private void createManager() {
        redmineManager = new RedmineManager(server);
        redmineManager.setLogin(login);
        redmineManager.setPassword(password);
    }

    public void getListOfProjects(final GetProjectsListener getProjectsListener) {
        new RedmineManagerTask<List<Project>>(getProjectsListener) {
            @Override
            protected List<Project> getData() throws Exception {
                return redmineManager.getProjects();
            }
        }.execute();
    }

    public void getListOfIssues(final Project project, final GetIssuesListener getIssuesListener) {
        new RedmineManagerTask<List<Issue>>(getIssuesListener) {
            @Override
            protected List<Issue> getData() throws Exception {
                return redmineManager.getIssues(String.valueOf(project.getId()), null);
            }
        }.execute();
    }

    public void postNewIssue(final Project project, final User assignee, final String statusName,
                             final String title, final String message,
                             final String pictureFilename, final String logsFilename,
                             final EditIssueListener editIssueListener) {
        final Issue issue = new Issue();
        issue.setProject(project);
        issue.setSubject(title);
        issue.setDescription(message);

        if (assignee != null) {
            issue.setAssignee(assignee);
        }

        if (statusName != null) {
            issue.setStatusName(statusName);
        }

        createIssue(project, editIssueListener, issue, pictureFilename, logsFilename);
    }

    private void createIssue(final Project project, final EditIssueListener editIssueListener, final Issue issue, final String pictureFilename, final String logsFilename) {
        new RedmineManagerTask<Issue>(editIssueListener) {
            @Override
            protected Issue getData() throws Exception {
                if (pictureFilename != null) {
                    Attachment attachment = redmineManager.uploadAttachment("image/jpeg", new File(pictureFilename));
                    issue.getAttachments().add(attachment);
                }
                if (logsFilename != null) {
                    Attachment attachment = redmineManager.uploadAttachment("text", new File(logsFilename));
                    issue.getAttachments().add(attachment);
                }
                return redmineManager.createIssue(String.valueOf(project.getId()), issue);
            }
        }.execute();
    }

    public void getMemberships(final Project project, final GetMembershipsListener getMembershipsListener) {
        new RedmineManagerTask<List<Membership>>(getMembershipsListener) {
            @Override
            protected List<Membership> getData() throws Exception {
                return redmineManager.getMemberships(project);
            }
        }.execute();
    }

    public void getStatuses(final GetStatusesListener getStatusesListener) {
        new RedmineManagerTask<List<IssueStatus>>(getStatusesListener) {
            @Override
            protected List<IssueStatus> getData() throws Exception {
                return redmineManager.getStatuses();
            }
        }.execute();
    }

    public void postCommentToTicket(final Issue issue, final User assignee, final String statusName,
                                    final String title, final String message,
                                    final String pictureFilename, final String logsFilename,
                                    final EditIssueListener editIssueListener) {
        List<Changeset> changesets = issue.getChangesets();
        Changeset changeset = new Changeset();
        changeset.setComments(title + "\n" + message);
        changesets.add(changeset);
        issue.setChangesets(changesets);

        if (assignee != null) {
            issue.setAssignee(assignee);
        }

        if (statusName != null) {
            issue.setStatusName(statusName);
        }

        createIssue(issue.getProject(), editIssueListener, issue, pictureFilename, logsFilename);
    }
}
