package com.noveogroup.screen_shot_report.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.noveogroup.screen_shot_report.R;
import com.taskadapter.redmineapi.bean.Project;

/**
 * Created by oisupov on 5/13/2014.
 */
public class ProjectAdapter extends ArrayAdapter<Project> {

    public ProjectAdapter(Context context) {
        super(context, R.layout.item_redmine_spinner);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.item_redmine_spinner, null);
        }

        ((TextView)convertView).setText(getItem(position).getName());
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}
