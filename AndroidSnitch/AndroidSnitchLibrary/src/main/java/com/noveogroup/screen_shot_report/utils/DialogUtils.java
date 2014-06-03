package com.noveogroup.screen_shot_report.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.noveogroup.screen_shot_report.controllers.PreferencesController;
import com.noveogroup.screen_shot_report.R;
import com.noveogroup.screen_shot_report.adapters.ProjectAdapter;
import com.taskadapter.redmineapi.bean.Project;

import java.util.List;

/**
 * Created by oisupov on 4/10/14.
 */
public final class DialogUtils {

    private DialogUtils() {
        throw new UnsupportedOperationException();
    }

    public static void showCredentialsDialog(final Activity activity, final CredentialsListener credentialsListener) {
        final AlertDialog dialog = getCredentialsDialog(activity, credentialsListener);
        dialog.show();
    }

    public static interface CredentialsListener {
        public void onCredentialsObtained(final String server, final String login, final String password);
        public void onCancel();
    }

    private static AlertDialog getCredentialsDialog(Activity activity, final CredentialsListener credentialsListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();

        View inflate = inflater.inflate(R.layout.dialog_signin, null);
        final EditText server = (EditText) inflate.findViewById(R.id.server);
        final EditText login = (EditText) inflate.findViewById(R.id.login);
        final EditText password = (EditText) inflate.findViewById(R.id.password);

        final PreferencesController preferencesController = PreferencesController.getPreferencesController(activity);
        server.setText(preferencesController.getServer());
        login.setText(preferencesController.getLogin());
        password.setText(preferencesController.getPassword());

        builder.setView(inflate)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (credentialsListener != null) {
                            credentialsListener.onCredentialsObtained(server.getText().toString(), login.getText().toString(), password.getText().toString());
                        }
                        dialog.dismiss();
                    }
                }).setTitle(activity.getString(R.string.credentials)).setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                credentialsListener.onCancel();
            }
        });
        return builder.create();
    }

    public static ProgressDialog showProgressDialog(Activity activity) {
        ProgressDialog progressDialog = ProgressDialog.show(activity, "Подождите",
                "Проверка", true);
        progressDialog.setCancelable(false);
        return progressDialog;
    }

    public static interface ProjectSelectListener {
        public void onProjectSelected(final Project project);
        public void onCancel();
    }

    public static void showSelectProjectDialog(final Activity activity, final List<Project> projects, final String projectName, final ProjectSelectListener onProjectSelected) {
        AlertDialog alertDialog = getShowSelectProjectDialog(activity, projects, projectName, onProjectSelected);

        alertDialog.show();
    }

    private static AlertDialog getShowSelectProjectDialog(Activity activity, List<Project> projects, final String projectName, final ProjectSelectListener onProjectSelected) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();

        final View inflate = inflater.inflate(R.layout.dialog_select_project, null);
        final ArrayAdapter<Project> adapter = new ProjectAdapter(activity);

        adapter.addAll(projects);

        final Spinner spinner = (Spinner) inflate.findViewById(R.id.spinner_select_project);
        spinner.setAdapter(adapter);

        if (projectName != null) {
            for (int i = 0; i < projects.size(); i++) {
                if (TextUtils.equals(projects.get(i).getName(), projectName)) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }

        return builder.setView(inflate)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (onProjectSelected != null) {
                            onProjectSelected.onProjectSelected((Project) spinner.getSelectedItem());
                        }
                        dialog.dismiss();
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        onProjectSelected.onCancel();
                    }
                }).setTitle("Выберите проект").create();
    }


}
