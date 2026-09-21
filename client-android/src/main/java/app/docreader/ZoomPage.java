package app.docreader;

import android.content.Context;
import android.graphics.*;
import android.view.*;
final class ZoomPage extends View {
    private Bitmap bitmap;
    private float zoom = 1, panX, panY;
    private final Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
    private final ScaleGestureDetector pinch;
    private final GestureDetector gestures;

    ZoomPage(Context context) {
        super(context);
        pinch = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override public boolean onScale(ScaleGestureDetector detector) {
                scale(zoom * detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY()); return true;
            }
        });
        gestures = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onDown(MotionEvent e) { return true; }
            @Override public boolean onDoubleTap(MotionEvent e) {
                scale(zoom > 1 ? 1 : 2.5f, e.getX(), e.getY()); return true;
            }
            @Override public boolean onScroll(MotionEvent a, MotionEvent b, float x, float y) {
                if (!pinch.isInProgress()) { panX -= x; panY -= y; constrain(); invalidate(); }
                return true;
            }
            @Override public boolean onSingleTapUp(MotionEvent e) { return performClick(); }
        });
    }
    void bitmap(Bitmap value) { bitmap = value; zoom = 1; panX = panY = 0; invalidate(); }
    void zoom(float factor) { scale(factor == 0 ? 1 : zoom * factor, getWidth()/2f, getHeight()/2f); }
    private float fit() {
        return bitmap == null ? 1 : Math.min((float)getWidth()/bitmap.getWidth(), (float)getHeight()/bitmap.getHeight());
    }
    private void scale(float value, float x, float y) {
        float next = Math.max(1, Math.min(5, value));
        panX = (x-getWidth()/2f) - (x-getWidth()/2f-panX)*next/zoom;
        panY = (y-getHeight()/2f) - (y-getHeight()/2f-panY)*next/zoom;
        zoom = next; constrain(); invalidate();
    }
    private void constrain() {
        if (bitmap == null) return;
        float x = Math.max(0, (bitmap.getWidth()*fit()*zoom-getWidth())/2);
        float y = Math.max(0, (bitmap.getHeight()*fit()*zoom-getHeight())/2);
        panX = Math.max(-x, Math.min(x, panX)); panY = Math.max(-y, Math.min(y, panY));
    }
    @Override protected void onDraw(Canvas canvas) {
        if (bitmap == null) return;
        float scale = fit()*zoom;
        canvas.save();
        canvas.translate((getWidth()-bitmap.getWidth()*scale)/2+panX, (getHeight()-bitmap.getHeight()*scale)/2+panY);
        canvas.scale(scale, scale); canvas.drawBitmap(bitmap, 0, 0, paint); canvas.restore();
    }
    @Override public boolean onTouchEvent(MotionEvent event) {
        pinch.onTouchEvent(event); gestures.onTouchEvent(event); return true;
    }
    @Override public boolean performClick() { super.performClick(); return true; }
}
