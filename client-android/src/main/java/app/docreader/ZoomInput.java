package app.docreader;

import android.content.Context;
import android.view.*;

final class ZoomInput {
    private final ScaleGestureDetector pinch;
    private final GestureDetector gestures;
    ZoomInput(Context context, ZoomPage page) {
        pinch = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override public boolean onScale(ScaleGestureDetector detector) {
                page.scale(page.zoom * detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY());
                return true;
            }
        });
        gestures = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onDown(MotionEvent e) { return true; }
            @Override public boolean onDoubleTap(MotionEvent e) {
                page.scale(page.zoom > 1 ? 1 : 2.5f, e.getX(), e.getY()); return true;
            }
            @Override public boolean onScroll(MotionEvent a, MotionEvent b, float x, float y) {
                if (!pinch.isInProgress()) page.pan(x, y);
                return true;
            }
            @Override public boolean onSingleTapUp(MotionEvent e) { return page.performClick(); }
        });
    }
    boolean touch(MotionEvent event) {
        pinch.onTouchEvent(event); gestures.onTouchEvent(event); return true;
    }
}
