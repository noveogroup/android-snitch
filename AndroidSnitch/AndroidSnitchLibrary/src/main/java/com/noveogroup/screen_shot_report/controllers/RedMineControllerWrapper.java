package com.noveogroup.screen_shot_report.controllers;

import com.taskadapter.redmineapi.NotAuthorizedException;
import com.taskadapter.redmineapi.RedMineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.bean.Attachment;
import com.taskadapter.redmineapi.bean.Changeset;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.IssueStatus;
import com.taskadapter.redmineapi.bean.Membership;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by oisupov on 4/10/14.
 */
public class RedMineControllerWrapper {

    private Logger logger = LoggerFactory.getLogger(RedMineControllerWrapper.class);

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

    public Observable<List<Project>> getListOfProjects() {
        return updateObservable(new Observable.OnSubscribe<List<Project>>() {
            @Override
            public void call(Subscriber<? super List<Project>> subscriber) {
                try {
                    subscriber.onNext(redmineManager.getProjects());
                    subscriber.onCompleted();
                } catch (RedMineException e) {
                    logger.trace(e.getMessage(), e);
                    subscriber.onError(e);
                }
            }
        });
    }

    public Observable<List<Issue>> getListOfIssues(final Project project) {
        return updateObservable(new Observable.OnSubscribe<List<Issue>>() {
            @Override
            public void call(Subscriber<? super List<Issue>> subscriber) {
                try {
                    subscriber.onNext(redmineManager.getIssues(String.valueOf(project.getId()), null));
                    subscriber.onCompleted();
                } catch (RedMineException e) {
                    logger.trace(e.getMessage(), e);
                    subscriber.onError(e);
                }
            }
        });
    }

    public Observable<Issue> postNewIssue(final Project project, final User assignee, final String statusName,
                                          final String title, final String message,
                                          final String pictureFilename, final String logsFilename) {
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

        return createIssue(project, issue, pictureFilename, logsFilename);
    }

    private Observable<Issue> createIssue(final Project project, final Issue issue, final String pictureFilename, final String logsFilename) {
        return updateObservable(new Observable.OnSubscribe<Issue>() {
            @Override
            public void call(Subscriber<? super Issue> subscriber) {
                try {
                    uploadAttachments(pictureFilename, issue, logsFilename);

                    subscriber.onNext(redmineManager.createIssue(String.valueOf(project.getId()), issue));
                    subscriber.onCompleted();
                } catch (Exception e) {
                    logger.trace(e.getMessage(), e);
                    subscriber.onError(e);
                }
            }
        });
    }

    private void uploadAttachments(String pictureFilename, Issue issue, String logsFilename) throws RedMineException, IOException {
        if (pictureFilename != null) {
            Attachment attachment = redmineManager.uploadAttachment("image/jpeg", new File(pictureFilename));
            issue.getAttachments().add(attachment);
        }
        if (logsFilename != null) {
            Attachment attachment = redmineManager.uploadAttachment("text", new File(logsFilename));
            issue.getAttachments().add(attachment);
        }
    }

    private Observable<Void> updateIssue(final Issue issue, final String pictureFilename, final String logsFilename) {
        return updateObservable(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                try {
                    uploadAttachments(pictureFilename, issue, logsFilename);
                    redmineManager.update(issue);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    logger.trace(e.getMessage(), e);
                    subscriber.onError(e);
                }
            }
        });
    }

    public Observable<List<Membership>> getMemberships(final Project project) {
        return updateObservable(new Observable.OnSubscribe<List<Membership>>() {
            @Override
            public void call(Subscriber<? super List<Membership>> subscriber) {
                try {
                    subscriber.onNext(redmineManager.getMemberships(project));
                    subscriber.onCompleted();
                } catch (RedMineException e) {
                    logger.trace(e.getMessage(), e);
                    if (e instanceof NotAuthorizedException) {
                        // Because sometimes user does not has permissions to get memberships o0
                        subscriber.onNext(new ArrayList<Membership>());
                        subscriber.onCompleted();
                    } else {
                        subscriber.onError(e);
                    }
                }
            }
        });
    }

    public Observable<List<IssueStatus>> getStatuses() {
        return updateObservable(new Observable.OnSubscribe<List<IssueStatus>>() {
            @Override
            public void call(Subscriber<? super List<IssueStatus>> subscriber) {
                try {
                    subscriber.onNext(redmineManager.getStatuses());
                    subscriber.onCompleted();
                } catch (RedMineException e) {
                    logger.trace(e.getMessage(), e);
                    if (e instanceof NotAuthorizedException) {
                        // Because sometimes user does not has permissions to get statuses o0
                        subscriber.onNext(new ArrayList<IssueStatus>());
                        subscriber.onCompleted();
                    } else {
                        subscriber.onError(e);
                    }
                }
            }
        });
    }

    public Observable<Void> postCommentToTicket(final Issue issue, final User assignee, final String statusName,
                                                 final String title, final String message,
                                                 final String pictureFilename, final String logsFilename) {
        issue.setNotes(title + "\n" + message);

        if (assignee != null) {
            issue.setAssignee(assignee);
        }

        if (statusName != null) {
            issue.setStatusName(statusName);
        }

        return updateIssue(issue, pictureFilename, logsFilename);
    }

    private <T> Observable<T> updateObservable(Observable.OnSubscribe<T> onSubscribe) {
        return Observable.create(onSubscribe).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }
}
