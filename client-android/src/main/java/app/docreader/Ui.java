package app.docreader;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.view.Gravity;
import android.widget.*;

final class Ui {
    final MainActivity activity;
    final int background, surface, ink, muted, accent, onAccent, border;
    Ui(MainActivity activity) {
        this.activity = activity;
        boolean dark = Appearance.dark(activity);
        background = dark ? 0xff151d1a : 0xfff5f4ee;
        surface = dark ? 0xff202c26 : 0xffffffff;
        ink = dark ? 0xffedf4ee : 0xff1e3028;
        muted = dark ? 0xffb5c7bb : 0xff58695f;
        accent = dark ? 0xffa8dcc6 : 0xff226d5d;
        onAccent = dark ? 0xff143a2c : 0xffffffff;
        border = dark ? 0xff3c5144 : 0xffdce3d9;
    }
    int dp(float value) { return Math.round(value * activity.getResources().getDisplayMetrics().density); }
    LinearLayout column() {
        LinearLayout view = new LinearLayout(activity);
        view.setOrientation(LinearLayout.VERTICAL);
        return view;
    }
    LinearLayout row() {
        LinearLayout view = new LinearLayout(activity);
        view.setGravity(Gravity.CENTER_VERTICAL);
        return view;
    }
    TextView text(String label, int size, int color) {
        TextView view = new TextView(activity);
        view.setText(label); view.setTextSize(size); view.setTextColor(color);
        view.setLineSpacing(dp(3), 1);
        return view;
    }
    TextView text(int label, int size, int color) { return text(activity.getString(label), size, color); }
    Button button(String label, boolean primary, Runnable action) {
        Button view = new Button(activity);
        view.setText(label); view.setAllCaps(false); view.setTextSize(14);
        view.setTextColor(primary ? onAccent : accent);
        view.setMinHeight(dp(48)); view.setMinimumHeight(dp(48));
        view.setPadding(dp(14), dp(6), dp(14), dp(6));
        view.setBackground(new RippleDrawable(ColorStateList.valueOf(0x33226d5d),
            shape(primary ? accent : surface, 14), null));
        view.setOnClickListener(v -> action.run());
        return view;
    }
    Button button(int label, boolean primary, Runnable action) {
        return button(activity.getString(label), primary, action);
    }
    GradientDrawable shape(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color); drawable.setCornerRadius(dp(radius));
        return drawable;
    }
    void gap(LinearLayout parent, int height) {
        parent.addView(new android.view.View(activity), new LinearLayout.LayoutParams(1, dp(height)));
    }
}
