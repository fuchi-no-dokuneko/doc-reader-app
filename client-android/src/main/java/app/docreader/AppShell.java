package app.docreader;

import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowInsets;
import android.widget.*;

final class AppShell {
    final LinearLayout root;
    final FrameLayout content;
    final TextView title;
    final Button back;

    AppShell(MainActivity activity, Ui ui) {
        root = ui.column(); root.setBackgroundColor(ui.background);
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets;
        });
        int flags = Appearance.dark(activity) ? 0 : View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        activity.getWindow().getDecorView().setSystemUiVisibility(flags);
        LinearLayout toolbar = ui.row(); toolbar.setPadding(ui.dp(12), ui.dp(8), ui.dp(12), ui.dp(8));
        back = ui.button(R.string.library, false, activity::home);
        toolbar.addView(back);
        title = ui.text(R.string.app_name, 18, ui.ink);
        title.setSingleLine(); title.setEllipsize(TextUtils.TruncateAt.END);
        title.setPadding(ui.dp(10), 0, ui.dp(10), 0);
        toolbar.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        Button theme = ui.button("◐", false, () -> Appearance.choose(activity));
        theme.setContentDescription(activity.getString(R.string.appearance));
        theme.setMinWidth(ui.dp(48)); theme.setMinimumWidth(ui.dp(48));
        toolbar.addView(theme, new LinearLayout.LayoutParams(ui.dp(48), ui.dp(48)));
        root.addView(toolbar);
        content = new FrameLayout(activity);
        root.addView(content, new LinearLayout.LayoutParams(-1, 0, 1));
        activity.setContentView(root);
        root.requestApplyInsets();
    }
    void display(String name, boolean reading, View view) {
        title.setText(name); back.setVisibility(reading ? View.VISIBLE : View.GONE);
        content.removeAllViews();
        content.addView(view, new FrameLayout.LayoutParams(-1, -1));
    }
}
