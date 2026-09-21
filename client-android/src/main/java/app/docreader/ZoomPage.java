package app.docreader;

import android.content.Context;
import android.graphics.*;
import android.view.*;
final class ZoomPage extends View {
    private Bitmap bitmap;
    float zoom = 1;
    private float panX, panY;
    private final Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
    private final ZoomInput input;

    ZoomPage(Context context) {
        super(context); setFocusable(true);
        input = new ZoomInput(context, this);
    }
    void bitmap(Bitmap value) { bitmap = value; zoom = 1; panX = panY = 0; invalidate(); }
    void zoom(float factor) { scale(factor == 0 ? 1 : zoom * factor, getWidth()/2f, getHeight()/2f); }
    private float fit() {
        return bitmap == null ? 1 : Math.min((float)getWidth()/bitmap.getWidth(), (float)getHeight()/bitmap.getHeight());
    }
    void pan(float x, float y) { panX -= x; panY -= y; constrain(); invalidate(); }
    void scale(float value, float x, float y) {
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
    // GestureDetector calls performClick only for a confirmed single tap.
    @android.annotation.SuppressLint("ClickableViewAccessibility")
    @Override public boolean onTouchEvent(MotionEvent event) {
        return input.touch(event);
    }
    @Override public boolean performClick() { super.performClick(); return true; }
}
