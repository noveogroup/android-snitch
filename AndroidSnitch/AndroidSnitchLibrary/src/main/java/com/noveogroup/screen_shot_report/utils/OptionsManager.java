package com.noveogroup.screen_shot_report.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.widget.Toast;

import com.noveogroup.screen_shot_report.controllers.PreferencesController;
import com.noveogroup.screen_shot_report.controllers.RedMineControllerWrapper;
import com.taskadapter.redmineapi.bean.Project;

import java.util.List;

import rx.Subscriber;

/**
 * Created by oisupov on 5/13/2014.
 */
public final class OptionsManager {

    private OptionsManager() {
        throw new UnsupportedOperationException();
    }

    public static interface OptionsSetupListener {
        public void onOptionsSatUp();

        public void onCancel();

        public void onError(Throwable e);
    }

    public static void setupOptions(final Activity activity, final OptionsSetupListener optionsSetupListener) {
        DialogUtils.showCredentialsDialog(activity, new DialogUtils.CredentialsListener() {
            @Override
            public void onCredentialsObtained(String server, String login, String password) {
                getProjectList(activity, server, login, password, optionsSetupListener);
            }

            @Override
            public void onCancel() {
                optionsSetupListener.onCancel();
            }
        });
    }

    private static void getProjectList(final Activity activity, final String server, final String login, final String password, final OptionsSetupListener optionsSetupListener) {
        RedMineControllerWrapper redMineControllerWrapper = new RedMineControllerWrapper(login, password, server);

        final ProgressDialog progressDialog = DialogUtils.showProgressDialog(activity);

        redMineControllerWrapper.getListOfProjects().subscribe(new Subscriber<List<Project>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                progressDialog.dismiss();
                Toast.makeText(activity, e.getMessage(), 3000).show();
                optionsSetupListener.onError(e);
            }

            @Override
            public void onNext(List<Project> projects) {
                progressDialog.dismiss();
                String currentProject = PreferencesController.getPreferencesController(activity).getCurrentProject();
                DialogUtils.showSelectProjectDialog(activity, projects, currentProject, new DialogUtils.ProjectSelectListener() {
                    @Override
                    public void onProjectSelected(Project project) {
                        PreferencesController preferencesController = PreferencesController.getPreferencesController(activity);
                        preferencesController.setCurrentProject(project.getName());
                        preferencesController.setLogin(login);
                        preferencesController.setServer(server);
                        preferencesController.setPassword(password);
                        optionsSetupListener.onOptionsSatUp();
                    }

                    @Override
                    public void onCancel() {
                        optionsSetupListener.onCancel();
                    }
                });
            }
        });
    }

}
