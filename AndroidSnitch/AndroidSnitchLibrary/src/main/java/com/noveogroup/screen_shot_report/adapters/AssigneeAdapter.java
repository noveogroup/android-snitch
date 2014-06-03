package com.noveogroup.screen_shot_report.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.noveogroup.screen_shot_report.R;
import com.taskadapter.redmineapi.bean.Membership;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by oisupov on 4/11/14.
 */
public class AssigneeAdapter extends ArrayAdapter<Membership> {

    public AssigneeAdapter(Context context) {
        super(context, R.layout.item_redmine_spinner);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.spinner_item_project, null);
        }

        ((TextView)convertView).setText(getItem(position).getUser().getFullName());

        return  convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}
