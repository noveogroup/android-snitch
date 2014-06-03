package com.noveogroup.screen_shot_report.widgets;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.Scroller;

public class ImageMoveResize implements OnTouchListener, MarkView.OnComputeScrollListener {

    private static final String LOG_TAG = ImageMoveResize.class.getName();

    private static final float MAX_ZOOM = 2f;
    private static final float MIN_ZOOM = 1f;
    private static final float ZOOM_OUT = 0.9f;
    private static final float ZOOM_IN = 1.1f;
    private static final float ZOOM_INFELICITY = 0.02f;
    private boolean isPainting;

    public static interface OnZoomListener {
        public void onZoom();
    }

    // These matrices will be used to move and zoom image
    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();

    // We can be in one of these 3 states
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;

    private int mode = NONE;

    // Remember some things for zooming
    private PointF start = new PointF();
    private PointF mid = new PointF();
    private float oldDist = 1f;

    private ImageView view;

    private VelocityTracker mVelocityTracker;
    private Scroller mScroller;

    private float mLastMotionX, mLastMotionY;
    private int mMinimumVelocity;
    private float[] tempValues = new float[9];
    private float initialScale = 1f;
    private float currentScale;
    private float maxScale;
    private float minScale;
    private OnZoomListener onZoomListener;

    public void setOnZoomListener(final OnZoomListener onZoomListener) {
        this.onZoomListener = onZoomListener;
    }

    public ImageMoveResize(final ImageView view) {
        view.setOnTouchListener(this);
        matrix.setTranslate(1f, 1f);
        view.setImageMatrix(matrix);
        mScroller = new Scroller(view.getContext());
        final ViewConfiguration configuration = ViewConfiguration.get(view.getContext());
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        this.view = view;
    }

    @Override
    public void onComputeScroll() {
        if (mScroller.computeScrollOffset()) {
            matrix.getValues(tempValues);

            float curX = -tempValues[Matrix.MTRANS_X];
            float curY = -tempValues[Matrix.MTRANS_Y];
            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();

            matrix.postTranslate(curX - x, curY - y);
            //tryMove(view, curX - x, curY - y);

            view.setImageMatrix(matrix);
            view.invalidate();
        }
    }

    public boolean isPainting() {
        return isPainting;
    }

    public void setPainting(boolean isPainting) {
        this.isPainting = isPainting;
    }

    public boolean onTouch(final View view, final MotionEvent ev) {
        Log.d(LOG_TAG, "onTouch");
        int action = ev.getAction();

        if (isPainting) {
            return false;
        }

        if ((action == MotionEvent.ACTION_DOWN) || (action == MotionEvent.ACTION_MOVE)) {
            ScrollSynchronizer.getInstance().tryLock(view);
        } else if ((action == MotionEvent.ACTION_UP) || (action == MotionEvent.ACTION_OUTSIDE)) {
            ScrollSynchronizer.getInstance().tryUnlock(view);
        } else if (ev.getAction() == MotionEvent.ACTION_CANCEL) {
            ScrollSynchronizer.getInstance().tryUnlock(view);
        }

        if (!ScrollSynchronizer.getInstance().amILocker(view)) {
            return false;
        }

        if (ev.getAction() == MotionEvent.ACTION_DOWN && ev.getEdgeFlags() != 0) {
            return false;
        }

        final WrapMotionEvent event = WrapMotionEvent.wrap(ev);

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }

        mVelocityTracker.addMovement(ev);

        action = ev.getActionMasked();
        final float y = ev.getY();
        final float x = ev.getX();
        Log.d(LOG_TAG, "onTouch " + action);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                mLastMotionY = y;
                mLastMotionX = x;
                start.x = x;
                start.y = y;
                mode = DRAG;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    int deltaX = (int) (x - mLastMotionX);
                    int deltaY = (int) (y - mLastMotionY);
                    mLastMotionX = x;
                    mLastMotionY = y;
                    tryMove((ImageView) view, deltaX, deltaY);
                    break;
                } else if (mode == ZOOM) {
                    zoom(event);
                    break;
                }
            case MotionEvent.ACTION_UP:
                if (mode == DRAG) {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000);
                    int initialXVelocity = (int) velocityTracker.getXVelocity();
                    int initialYVelocity = (int) velocityTracker.getYVelocity();

                    if ((Math.abs(initialXVelocity) + Math.abs(initialYVelocity) > mMinimumVelocity)) {
                        fling((ImageView) view, -initialXVelocity, -initialYVelocity);
                    }

                    if (mVelocityTracker != null) {
                        mVelocityTracker.recycle();
                        mVelocityTracker = null;
                    }
                }
                mode = NONE;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (mode == ZOOM) {
                    checkBorders((ImageView) view);
                }
                mode = NONE;
                break;
        }

        this.view.setImageMatrix(matrix);
        return true;
    }


    /**
     * Determine the space between the first two fingers
     */
    private float spacing(WrapMotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

    /**
     * Calculate the mid point of the first two fingers
     */
    private void midPoint(PointF point, WrapMotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    public void checkBorders(final ImageView view) {
        Drawable drawable = view.getDrawable();
        matrix.getValues(tempValues);

        final float picWidth = drawable.getIntrinsicWidth() * tempValues[Matrix.MSCALE_X];
        final float picHeight = drawable.getIntrinsicHeight() * tempValues[Matrix.MSCALE_Y];

        final float curX = tempValues[Matrix.MTRANS_X];
        final float curY = tempValues[Matrix.MTRANS_Y];

        final int viewWidth = view.getWidth();
        final int viewHeight = view.getHeight();

        float deltaX = 0;
        float deltaY = 0;

        boolean shouldReturn = false;

        if (curX > 0) {
            shouldReturn = true;
            deltaX = -curX;
        }

        if (curX + picWidth < viewWidth) {
            shouldReturn = true;
            deltaX = viewWidth - curX - picWidth;
        }

        if (curY > 0) {
            shouldReturn = true;
            deltaY = -curY;
        }

        if (curY + picHeight < viewHeight) {
            shouldReturn = true;
            deltaY = viewHeight - picHeight - curY;
        }

        if (shouldReturn) {
            mScroller.startScroll(-(int) curX, -(int) curY, -(int) deltaX, -(int) deltaY, 700);
            view.invalidate();
        }
    }

    public void zoom(final float zoomFactor) {
        float scale = calculateOptimalScale(zoomFactor);
        matrix.postTranslate(-view.getWidth() / 2, -view.getHeight() / 2);
        matrix.postScale(scale, scale);
        matrix.postTranslate(view.getWidth() / 2, view.getHeight() / 2);
        tryMove(view, 0, 0);

        view.setImageMatrix(matrix);
    }

    public void zoomIn() {
        zoom(ZOOM_IN);
    }

    public void zoomOut() {
        zoom(ZOOM_OUT);
    }

    private float calculateOptimalScale(final float proposedScale) {
        matrix.getValues(tempValues);
        currentScale = tempValues[Matrix.MSCALE_X];
        float scale = proposedScale;
        if (proposedScale * currentScale > maxScale) {
            scale = maxScale / currentScale;
        } else if (proposedScale * currentScale < minScale) {
            scale = minScale / currentScale;
        }
        currentScale = currentScale * scale;
        return scale;
    }

    private void zoom(WrapMotionEvent event) {
        float newDist = spacing(event);
        Log.d(LOG_TAG, "newDist=" + newDist);
        if (newDist > 10f) {
            matrix.set(savedMatrix);
            float scale = calculateOptimalScale(newDist / oldDist);
            matrix.postScale(scale, scale, mid.x, mid.y);
//            onZoomListener.onZoom();
        }
    }

    public void resetDrawable(ImageView view) {
        resetDrawable(view, view.getWidth(), view.getHeight());
    }

    public void resetDrawable(ImageView view, int newViewWidth, int newViewHeight) {
        Drawable drawable = view.getDrawable();
        // workaround.
        matrix.setTranslate(1f, 1f);

        float scaleX = (float) newViewWidth / (float) drawable.getIntrinsicWidth();
        float scaleY = (float) newViewHeight / (float) drawable.getIntrinsicHeight();
        float newScale = this.initialScale = Math.max(scaleX, scaleY);
        currentScale = newScale;
        maxScale = newScale * MAX_ZOOM;
        minScale = newScale * MIN_ZOOM;

        matrix.setScale(newScale, newScale);

        int dx = (int) ((newViewWidth - drawable.getIntrinsicWidth() * newScale) / 2);
        int dy = (int) ((newViewHeight - drawable.getIntrinsicHeight() * newScale) / 2);
        matrix.postTranslate(dx, dy);

        view.setImageMatrix(matrix);
        view.postInvalidate();
        view.requestFocus();
    }

    private void tryMove(ImageView view, float deltaX, float deltaY) {
        final Drawable drawable = view.getDrawable();
        matrix.getValues(tempValues);

        final float picWidth = drawable.getIntrinsicWidth() * tempValues[Matrix.MSCALE_X];
        final float picHeight = drawable.getIntrinsicHeight() * tempValues[Matrix.MSCALE_Y];

        final float curX = tempValues[Matrix.MTRANS_X];
        final float curY = tempValues[Matrix.MTRANS_Y];

        final int viewWidth = view.getWidth();
        final int viewHeight = view.getHeight();

        if (picWidth <= viewWidth) {
            if (deltaX >= 0) {
                if (picWidth + curX + deltaX > viewWidth) {
                    deltaX = viewWidth - picWidth - curX;
                }
            }
            if (deltaX <= 0) {
                if (curX + deltaX < 0) {
                    deltaX = -curX;
                }
            }
        } else {
            // picWidth > viewWidth
            if (deltaX >= 0) {
                if (curX + deltaX > 0) {
                    deltaX = -curX;
                }
            }
            if (deltaX <= 0) {
                if (curX + picWidth + deltaX < viewWidth) {
                    deltaX = viewWidth - curX - picWidth;
                }

            }
        }

        if (picHeight <= viewHeight) {
            if (deltaY >= 0) {
                if (picHeight + curY + deltaY > viewHeight) {
                    deltaY = viewHeight - picHeight - curY;
                }
            }
            if (deltaY <= 0) {
                if (curY + deltaY < 0) {
                    deltaY = -curY;
                }
            }
        } else {
            // picHeight> viewHeight
            if (deltaY >= 0) {
                if (curY + deltaY > 0) {
                    deltaY = -curY;
                }
            }
            if (deltaY <= 0) {
                if (curY + deltaY + picHeight < viewHeight) {
                    deltaY = viewHeight - picHeight - curY;
                }
            }
        }

        matrix.postTranslate(deltaX, deltaY);
    }

    public void fling(final ImageView view, final int velocityX, final int velocityY) {
        matrix.getValues(tempValues);

        final float curX = -tempValues[Matrix.MTRANS_X];
        final float curY = -tempValues[Matrix.MTRANS_Y];

        final float picWidth = view.getDrawable().getIntrinsicWidth() * tempValues[Matrix.MSCALE_X];
        final float picHeight = view.getDrawable().getIntrinsicHeight() * tempValues[Matrix.MSCALE_Y];

        final int height = view.getHeight();
        final int width = view.getWidth();

        mScroller.fling((int) curX, (int) curY, velocityX, velocityY, 0, (int) (picWidth - width), 0, (int) (picHeight - height));

        view.invalidate();
    }

    public boolean canZoomOut() {
        return currentScale > minScale + ZOOM_INFELICITY;
    }

    public boolean canZoomIn() {
        return currentScale < maxScale - ZOOM_INFELICITY;
    }
}
