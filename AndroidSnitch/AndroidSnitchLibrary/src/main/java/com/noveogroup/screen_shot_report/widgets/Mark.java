package com.noveogroup.screen_shot_report.widgets;

import android.graphics.Path;
import android.graphics.Point;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Mark implements Parcelable {

    public transient Path path = new Path();

    public ArrayList<Point> points = new ArrayList<Point>();

    public void restorePathFromPoints() {
        path = new Path();
        boolean firstPoint = true;
        for (final Point point : points) {
            if (!firstPoint) {
                path.lineTo(point.x, point.y);
            } else {
                firstPoint = false;
                path.moveTo(point.x, point.y);
            }
            path.rMoveTo(0f, 0f);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(points);
    }

    public static Creator<Mark> CREATOR = new Creator<Mark>() {
        @Override
        public Mark createFromParcel(Parcel source) {
            final Mark mark = new Mark();
            mark.points = source.readArrayList(ClassLoader.getSystemClassLoader());
            mark.restorePathFromPoints();
            return mark;
        }

        @Override
        public Mark[] newArray(int size) {
            return new Mark[size];
        }
    };
}
