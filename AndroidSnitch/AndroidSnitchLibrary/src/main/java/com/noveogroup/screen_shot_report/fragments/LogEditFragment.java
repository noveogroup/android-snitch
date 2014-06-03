package com.noveogroup.screen_shot_report.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.noveogroup.screen_shot_report.R;
import com.noveogroup.screen_shot_report.utils.IOUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by oisupov on 4/5/14.
 */
public class LogEditFragment extends Fragment {

    private Logger logger = LoggerFactory.getLogger(LogEditFragment.class);

    public static final String ARGUMENT_LOGS_URI = "ARGUMENT_LOGS_URI";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return View.inflate(getActivity(), R.layout.fragment_log_edit, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String logs;
        try {
            logs = IOUtils.readLogs(getActivity(), getArguments().getString(ARGUMENT_LOGS_URI));
        } catch (IOException e) {
            logs = "Error reading logs file";
            logger.trace("reading logs error", e);
        }

        final EditText editText = (EditText) getView().findViewById(R.id.edit);
        editText.setText(logs);

        getView().findViewById(R.id.cut_before).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.getText().delete(0, editText.getSelectionStart());
            }
        });

        getView().findViewById(R.id.cut_after).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.getText().delete(editText.getSelectionStart(), editText.getText().length() - 1);
            }
        });
    }

    public String getResult() {
        return ((EditText) getView().findViewById(R.id.edit)).getText().toString();
    }

}


