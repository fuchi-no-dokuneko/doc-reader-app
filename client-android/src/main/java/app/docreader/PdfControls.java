package app.docreader;

import android.app.AlertDialog;
import android.text.InputType;
import android.widget.*;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

final class PdfControls {
    final LinearLayout view;
    private final Button previous, next, label;
    PdfControls(MainActivity activity, ZoomPage image, IntConsumer navigate, IntSupplier page, IntSupplier count) {
        Ui ui = activity.ui;
        view = ui.column(); view.setPadding(ui.dp(12), ui.dp(6), ui.dp(12), ui.dp(10));
        LinearLayout zoom = ui.row();
        zoom.addView(ui.button("−", false, () -> image.zoom(.7f)));
        zoom.addView(ui.button(R.string.fit_page, false, () -> image.zoom(0)), new LinearLayout.LayoutParams(0, -2, 1));
        zoom.addView(ui.button("+", false, () -> image.zoom(1.5f)));
        zoom.getChildAt(0).setContentDescription(activity.getString(R.string.zoom_out));
        zoom.getChildAt(2).setContentDescription(activity.getString(R.string.zoom_in));
        view.addView(zoom); ui.gap(view, 6);
        LinearLayout row = ui.row();
        previous = ui.button("‹", false, () -> navigate.accept(page.getAsInt()-1));
        previous.setContentDescription(activity.getString(R.string.previous));
        next = ui.button("›", false, () -> navigate.accept(page.getAsInt()+1));
        next.setContentDescription(activity.getString(R.string.next));
        label = ui.button("…", false, () -> jump(activity, count.getAsInt(), navigate));
        row.addView(previous);
        row.addView(label, new LinearLayout.LayoutParams(0, -2, 1)); row.addView(next);
        view.addView(row); busy(true);
    }
    void busy(boolean busy) { previous.setEnabled(!busy); next.setEnabled(!busy); label.setEnabled(!busy); }
    void update(int page, int count) {
        label.setText(view.getContext().getString(R.string.page_count, page+1, count));
        label.setEnabled(true); previous.setEnabled(page > 0); next.setEnabled(page < count-1);
    }
    private void jump(MainActivity activity, int count, IntConsumer navigate) {
        EditText input = new EditText(activity);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint(activity.getString(R.string.page_hint, count));
        AlertDialog dialog = new AlertDialog.Builder(activity).setTitle(R.string.jump_page).setView(input)
            .setNegativeButton(R.string.cancel, null).setPositiveButton(R.string.go, null).create();
        dialog.setOnShowListener(d -> dialog.getButton(-1).setOnClickListener(v -> {
            int page = PagePosition.parse(input.getText().toString(), count);
            if (page < 0) input.setError(activity.getString(R.string.page_hint, count));
            else { dialog.dismiss(); navigate.accept(page); }
        }));
        dialog.show();
    }
}
