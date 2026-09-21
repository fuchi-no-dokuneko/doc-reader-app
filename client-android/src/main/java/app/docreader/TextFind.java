package app.docreader;

import android.app.AlertDialog;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.widget.*;

final class TextFind {
    private final MainActivity activity;
    private final TextView text;
    private final ScrollView scroll;
    private String query = "";
    private int found = -1;
    private BackgroundColorSpan background;
    private ForegroundColorSpan foreground;

    TextFind(MainActivity activity, TextView text, ScrollView scroll) {
        this.activity = activity; this.text = text; this.scroll = scroll;
    }
    void show() {
        EditText input = new EditText(activity);
        input.setSingleLine(true); input.setHint(R.string.find_hint); input.setText(query);
        AlertDialog dialog = new AlertDialog.Builder(activity).setTitle(R.string.find)
            .setView(input).setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.next, null).create();
        dialog.setOnShowListener(d -> dialog.getButton(-1).setOnClickListener(v -> {
            String value = input.getText().toString();
            if (!value.equals(query)) found = -1;
            query = value;
            found = TextSearch.next(text.getText().toString(), query, found+1);
            if (found < 0) input.setError(activity.getString(R.string.no_match));
            else { highlight(); dialog.dismiss(); }
        }));
        dialog.show();
    }
    private void highlight() {
        Spannable spans = (Spannable) text.getText();
        if (background != null) { spans.removeSpan(background); spans.removeSpan(foreground); }
        background = new BackgroundColorSpan(0xfff7d878);
        foreground = new ForegroundColorSpan(0xff252b20);
        spans.setSpan(background, found, found+query.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spans.setSpan(foreground, found, found+query.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.post(() -> {
            if (text.getLayout() != null) scroll.smoothScrollTo(0,
                text.getLayout().getLineTop(text.getLayout().getLineForOffset(found)) + text.getPaddingTop());
        });
    }
}
