package app.docreader;

import android.view.Gravity;
import android.view.View;
import android.widget.*;

final class StatusScreen {
    static LinearLayout panel(MainActivity activity) {
        LinearLayout body = activity.ui.column();
        body.setGravity(Gravity.CENTER);
        int padding = activity.ui.dp(32);
        body.setPadding(padding, padding, padding, padding);
        return body;
    }
    static View loading(MainActivity activity) {
        Ui ui = activity.ui;
        LinearLayout body = panel(activity);
        body.addView(new ProgressBar(activity)); ui.gap(body, 24);
        body.addView(ui.text(R.string.loading, 18, ui.ink)); ui.gap(body, 24);
        body.addView(ui.button(R.string.cancel, false, activity::home));
        return body;
    }
    static View error(MainActivity activity, int message) {
        Ui ui = activity.ui;
        LinearLayout body = panel(activity);
        body.addView(ui.text(R.string.open_failed, 24, ui.ink)); ui.gap(body, 12);
        body.addView(ui.text(message, 16, ui.muted)); ui.gap(body, 24);
        body.addView(ui.button(R.string.choose_another, true, activity::pick));
        return body;
    }
}
