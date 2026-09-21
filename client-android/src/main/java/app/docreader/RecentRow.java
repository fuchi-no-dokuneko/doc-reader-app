package app.docreader;

import android.app.AlertDialog;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.View;
import android.widget.*;

final class RecentRow {
    static View create(MainActivity activity, Document doc) {
        Ui ui = activity.ui;
        LinearLayout row = ui.row();
        row.setBackground(ui.shape(ui.surface, 18));
        row.setPadding(ui.dp(16), ui.dp(8), ui.dp(8), ui.dp(8));
        LinearLayout label = ui.column();
        label.setPadding(0, ui.dp(12), ui.dp(8), ui.dp(12));
        TextView name = ui.text(doc.name, 16, ui.ink);
        name.setMaxLines(2); name.setEllipsize(TextUtils.TruncateAt.END);
        label.addView(name);
        label.addView(ui.text(doc.format.label() + "  ·  " + Formatter.formatShortFileSize(activity, doc.size), 12, ui.muted));
        label.setOnClickListener(v -> activity.open(doc));
        label.setFocusable(true);
        label.setContentDescription(doc.name);
        row.addView(label, new LinearLayout.LayoutParams(0, -2, 1));
        Button remove = ui.button("×", false, () -> new AlertDialog.Builder(activity)
            .setTitle(R.string.remove_title).setMessage(R.string.remove_hint)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.remove, (dialog, which) -> {
                activity.library.remove(doc); activity.home();
            }).show());
        remove.setContentDescription(activity.getString(R.string.remove) + " " + doc.name);
        remove.setMinWidth(ui.dp(48)); remove.setMinimumWidth(ui.dp(48));
        row.addView(remove, new LinearLayout.LayoutParams(ui.dp(48), ui.dp(48)));
        return row;
    }
}
