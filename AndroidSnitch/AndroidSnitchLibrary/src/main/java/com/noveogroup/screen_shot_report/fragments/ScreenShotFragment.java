package com.noveogroup.screen_shot_report.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.noveogroup.screen_shot_report.R;
import com.noveogroup.screen_shot_report.widgets.ImageMoveResize;
import com.noveogroup.screen_shot_report.widgets.Mark;
import com.noveogroup.screen_shot_report.widgets.MarkView;

import java.util.ArrayList;

/**
 * Created by oisupov on 4/5/14.
 */
public class ScreenShotFragment extends Fragment {

    public static final String ARGUMENT_SCREEN_SHOT_URI = "ARGUMENT_SCREEN_SHOT_URI";
    public static final String KEY_MARKS = "MARKS";

    private View navigate;
    private View edit;
    private MarkView markView;

    private ImageMoveResize imageMoveResize;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return View.inflate(getActivity(), R.layout.fragment_screen_shot, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final String data = getArguments().getString(ARGUMENT_SCREEN_SHOT_URI);
        final Bitmap bitmap = BitmapFactory.decodeFile(data);

        markView = (MarkView) view.findViewById(R.id.img);
        markView.setImageBitmap(bitmap);

        if (savedInstanceState != null) {
            savedInstanceState.setClassLoader(getActivity().getClassLoader());
            ArrayList<Mark> marks = savedInstanceState.<Mark>getParcelableArrayList(KEY_MARKS);
            markView.setMarks(marks);
        }

        imageMoveResize = new ImageMoveResize(markView);

        markView.setOnSizeChangedListener(new MarkView.OnSizeChangedListener() {
            @Override
            public void onSizeChanged(int w, int h, int oldw, int oldh) {
                if (imageMoveResize != null) {
                    imageMoveResize.resetDrawable(markView, w, h);
                }
            }
        });

        markView.setOnComputeScrollListener(imageMoveResize);

        view.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markView.removeAllMarks();
            }
        });

        view.findViewById(R.id.revert).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markView.removeLastMark();
            }
        });

        navigate = view.findViewById(R.id.navigate);
        edit = view.findViewById(R.id.edit);

        navigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markView.setPainting(false);
                imageMoveResize.setPainting(false);
                updateButtons();
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markView.setPainting(true);
                imageMoveResize.setPainting(true);
                updateButtons();
            }
        });

        updateButtons();
    }

    private void updateButtons() {
        if (markView.isPainting()) {
            navigate.setPressed(false);
            edit.setPressed(true);
        } else {
            navigate.setPressed(true);
            edit.setPressed(false);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.setClassLoader(getActivity().getClassLoader());
        outState.putParcelableArrayList(KEY_MARKS, markView.getMarks());
    }

    public Bitmap saveResult() {
        return markView.createPicture();
    }
}
