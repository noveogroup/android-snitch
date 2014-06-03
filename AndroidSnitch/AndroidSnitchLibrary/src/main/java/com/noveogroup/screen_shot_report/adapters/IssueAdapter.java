package com.noveogroup.screen_shot_report.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.noveogroup.screen_shot_report.R;
import com.taskadapter.redmineapi.bean.Issue;

import java.util.ArrayList;

/**
 * Created by oisupov on 4/11/14.
 */
public class IssueAdapter extends ArrayAdapter<Issue> {

    public IssueAdapter(Context context) {
        super(context, R.layout.item_redmine_issue);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.item_redmine_issue, null);
        }

        Issue item = getItem(position);
        ((TextView)convertView).setText(item.getId() + " : " + item.getSubject());

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}
