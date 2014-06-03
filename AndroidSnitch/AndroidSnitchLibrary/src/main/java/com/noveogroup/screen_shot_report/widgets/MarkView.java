package com.noveogroup.screen_shot_report.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import static android.graphics.Bitmap.Config.ARGB_8888;

public class MarkView extends ImageView {

    private static final String LOG_TAG = MarkView.class.getName();

    public static final Paint DEFAULT_PAINT = new Paint();

    static {
        DEFAULT_PAINT.setStrokeWidth(5f);
        DEFAULT_PAINT.setColor(Color.MAGENTA);
        DEFAULT_PAINT.setStyle(Paint.Style.STROKE);
        DEFAULT_PAINT.setAntiAlias(true);
    }

    public static final Paint SUCCESS_PATH_PAINT = new Paint(DEFAULT_PAINT);

    static {
        SUCCESS_PATH_PAINT.setColor(Color.GREEN);
    }

    public static final Paint ERROR_PATH_PAINT = new Paint(DEFAULT_PAINT);

    static {
        ERROR_PATH_PAINT.setColor(Color.RED);
    }

    private final Matrix eMatrix = new Matrix();    {
        eMatrix.setTranslate(0f, 0f);
    }

    private final float[] utilValues = new float[9];

    private Mark currentMark;
    private Paint currentMarkPaint = DEFAULT_PAINT;
    private boolean drawable = true;
    private ArrayList<Mark> marks = new ArrayList<Mark>();

    private OnSizeChangedListener onSizeChangedListener = new OnSizeChangedListener() {
        @Override
        public void onSizeChanged(int w, int h, int oldw, int oldh) {
        }
    };

    private OnComputeScrollListener onComputeScrollListener = new OnComputeScrollListener() {
        @Override
        public void onComputeScroll() {
        }
    };
    private boolean isPainting;

    public static interface OnSizeChangedListener {
        public void onSizeChanged(int w, int h, int oldw, int oldh);
    }

    public static interface OnComputeScrollListener {
        public void onComputeScroll();
    }

    public MarkView(Context context) {
        super(context);
    }

    public MarkView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MarkView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        onSizeChangedListener.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (ScrollSynchronizer.getInstance().amILocker(this)) {
            onComputeScrollListener.onComputeScroll();
        }
    }

    public void setOnComputeScrollListener(final OnComputeScrollListener onComputeScrollListener) {
        this.onComputeScrollListener = onComputeScrollListener;
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        //canvas.drawBitmap(border, this.eMatrix, null);
        super.onDraw(canvas);

        if (!drawable) {
            return;
        }

        final Matrix matrix = getImageMatrix();
        matrix.getValues(utilValues);
        canvas.translate(utilValues[Matrix.MTRANS_X], utilValues[Matrix.MTRANS_Y]);
        canvas.scale(utilValues[Matrix.MSCALE_X], utilValues[Matrix.MSCALE_Y]);

        if (currentMark != null) {
            canvas.drawPath(currentMark.path, currentMarkPaint);
        }

        for (final Mark mark : marks) {
            if (mark != null) {
                canvas.drawPath(mark.path, SUCCESS_PATH_PAINT);
            }
        }
    }

    public boolean isPainting() {
        return isPainting;
    }

    public void setPainting(boolean isPainting) {
        this.isPainting = isPainting;
    }

    @Override
    public boolean onTouchEvent(final MotionEvent ev) {
        Log.d(LOG_TAG, "onTouchEvent");
        final int action = ev.getAction();

        if ((action == MotionEvent.ACTION_DOWN) || (action == MotionEvent.ACTION_MOVE)) {
            ScrollSynchronizer.getInstance().tryLock(this);
        } else if ((action == MotionEvent.ACTION_UP) || (action == MotionEvent.ACTION_OUTSIDE)) {
            ScrollSynchronizer.getInstance().tryUnlock(this);
        } else if (ev.getAction() == MotionEvent.ACTION_CANCEL) {
            ScrollSynchronizer.getInstance().tryUnlock(this);
        }

        if (ScrollSynchronizer.getInstance().amILocker(this)) {
            if (drawable && isPainting) {
                final float eventX = ev.getX();
                final float eventY = ev.getY();
                final Matrix matrix = getImageMatrix();
                matrix.getValues(utilValues);
                final float pathX = (eventX - utilValues[Matrix.MTRANS_X]) / utilValues[Matrix.MSCALE_X];
                final float pathY = (eventY - utilValues[Matrix.MTRANS_Y]) / utilValues[Matrix.MSCALE_Y];
                if (action == MotionEvent.ACTION_DOWN) {
                    currentMark = new Mark();
                    currentMark.path.moveTo(pathX, pathY);
                    currentMark.points.add(new Point((int) pathX, (int) pathY));
                } else if (action == MotionEvent.ACTION_MOVE) {
                    if (currentMark != null) {
                        currentMark.path.lineTo(pathX, pathY);
                        currentMark.path.rMoveTo(0, 0);
                        currentMark.points.add(new Point((int) pathX, (int) pathY));
                    }
                } else if (action == MotionEvent.ACTION_UP) {
                    if (currentMark != null) {
                        marks.add(currentMark);
                        currentMark = null;
//                        checkCurrentMark();
                    }
                }
                invalidate();
                return true;
            } else {
                super.onTouchEvent(ev);
                return true;
            }
        }
        return false;
    }

    public void setOnSizeChangedListener(OnSizeChangedListener listener) {
        this.onSizeChangedListener = listener;
    }

    public void setCurrentMarkPaint(final Paint paint) {
        this.currentMarkPaint = paint;
    }

    public void removeLastMark() {
        if (marks != null && marks.size() > 0) {
            this.marks.remove(marks.size() - 1);
        }
        invalidate();
    }

    public boolean isDrawable() {
        return drawable;
    }

    public void setDrawable(final boolean drawable) {
        this.drawable = drawable;
    }

    public void removeAllMarks() {
        marks.clear();
        currentMark = null;
        invalidate();
    }

    public Bitmap createPicture() {
        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
        final Bitmap bitmap = Bitmap.createBitmap(dm.widthPixels, dm.heightPixels, ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);

        setImageMatrix(eMatrix);
        draw(canvas);

        return bitmap;
    }

    public ArrayList<Mark> getMarks() {
        return marks;
    }

    public void setMarks(ArrayList<Mark> marks) {
        this.marks = marks;
    }

}
