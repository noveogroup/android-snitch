package com.noveogroup.screen_shot_report.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.content.FileProvider;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.noveogroup.screen_shot_report.controllers.PreferencesController;
import com.noveogroup.screen_shot_report.R;
import com.noveogroup.screen_shot_report.ReportData;
import com.noveogroup.screen_shot_report.listeners.DefaultRecognitionListener;
import com.noveogroup.screen_shot_report.utils.IOUtils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by oisupov on 4/4/14.
 */
public class ComposeMessageActivity extends ActionBarActivity {

    private Logger logger = LoggerFactory.getLogger(ComposeMessageActivity.class);
    private Dialog recognizerDialog;
    private Menu menu;

    private class EditTextRecognitionListener extends DefaultRecognitionListener {

        private EditText editText;

        @Override
        public void onReadyForSpeech(Bundle params) {
            super.onReadyForSpeech(params);
            recognizerDialog.show();
        }

        @Override
        public void onEndOfSpeech() {
            super.onEndOfSpeech();
            recognizerDialog.dismiss();
        }

        private EditTextRecognitionListener(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void onError(int error) {
            logger.error("EditTextRecognitionListener : onError {}", error);
        }

        @Override
        public void onResults(Bundle results) {
            final ArrayList<String> result = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (result.size() > 0) {
                editText.setText(result.get(0));
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(getString(R.string.compose));

        recognizerDialog = new AlertDialog.Builder(this).setTitle(R.string.speech_recognizer_dialog_speak_label).create();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        setContentView(R.layout.activity_compose_message);

        final EditText titleEditText = (EditText) findViewById(R.id.title);
        final EditText messageEditText = (EditText) findViewById(R.id.message);
        final View recognizeTitle = findViewById(R.id.recognize_title);
        final View recognizeMessage = findViewById(R.id.recognize_message);
        final CheckBox attachLogs = (CheckBox) findViewById(R.id.attach_logs);
        attachLogs.setChecked(PreferencesController.getPreferencesController(this).shouldAttachLogs());
        final CheckBox attachScreenShot = (CheckBox) findViewById(R.id.attach_screen_shot);
        attachScreenShot.setChecked(PreferencesController.getPreferencesController(this).shouldAttachScreenShot());

        final SpeechRecognizer speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        final Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru")
                .putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getApplicationContext().getPackageName())
                .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);

        recognizeTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speechRecognizer.setRecognitionListener(new EditTextRecognitionListener(titleEditText));
                speechRecognizer.startListening(recognizerIntent);
            }
        });

        recognizeMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speechRecognizer.setRecognitionListener(new EditTextRecognitionListener(messageEditText));
                speechRecognizer.startListening(recognizerIntent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        this.menu.clear();
        this.getMenuInflater().inflate(R.menu.menu_compose_message, menu);

        final View view = MenuItemCompat.getActionView(menu.findItem(R.id.share_with));
        if (view instanceof Spinner) {
            final Spinner spinner = (Spinner) view;
            final ArrayAdapter<CharSequence> listAdapter = ArrayAdapter.createFromResource(this,
                    R.array.share_options,
                    R.layout.support_simple_spinner_dropdown_item);
            listAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
            spinner.setAdapter(listAdapter);
        }
        super.onCreateOptionsMenu(menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.send) {
            final Spinner view = (Spinner) MenuItemCompat.getActionView(menu.findItem(R.id.share_with));
            final int selectedItemPosition = view.getSelectedItemPosition();
            prepare();
            if (selectedItemPosition == 1) {
                shareRedMine();
            } else {
                shareEmail();
            }
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void prepare() {
        ReportData.getInstance().setTitle(((EditText) findViewById(R.id.title)).getText().toString());
        ReportData.getInstance().setMessage(((EditText) findViewById(R.id.message)).getText().toString());
        boolean attachLogs = ((CheckBox) findViewById(R.id.attach_logs)).isChecked();
        ReportData.getInstance().setIncludeLogs(attachLogs);
        boolean attachScreenShot = ((CheckBox) findViewById(R.id.attach_screen_shot)).isChecked();
        ReportData.getInstance().setIncludeScreenShot(attachScreenShot);

        PreferencesController.getPreferencesController(this).setAttachLogs(attachLogs);
        PreferencesController.getPreferencesController(this).setAttachScreenShot(attachScreenShot);
    }

    private void shareEmail() {
        Intent mailIntent = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);
        mailIntent.setType("text/plain");
        mailIntent.putExtra(Intent.EXTRA_SUBJECT, ReportData.getInstance().getTitle());
        mailIntent.putExtra(Intent.EXTRA_TEXT, ReportData.getInstance().getMessage());

        ReportData data = ReportData.getInstance();

        ArrayList<Uri> uris = new ArrayList<Uri>();
        if (data.getScreenShotPath() != null && data.isIncludeScreenShot()) {
            final File screenShotFile = new File(data.getScreenShotPath());
            Uri uri = FileProvider.getUriForFile(getApplicationContext(), IOUtils.AUTHORITY, screenShotFile);
            uris.add(uri);
        }
        if (data.getLogPath() != null && data.isIncludeLogs()) {
            final File logsFile = new File(data.getLogPath());
            Uri uri = FileProvider.getUriForFile(getApplicationContext(), IOUtils.AUTHORITY, logsFile);
            uris.add(uri);
        }
        mailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        mailIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        try {
            startActivity(Intent.createChooser(mailIntent, getString(R.string.email_client_chooser_title)));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, getString(R.string.no_email_clients_found), Toast.LENGTH_SHORT).show();
        }
    }

    private void shareRedMine() {
        startActivity(new Intent(this, RedMineActivity.class));
    }

}
