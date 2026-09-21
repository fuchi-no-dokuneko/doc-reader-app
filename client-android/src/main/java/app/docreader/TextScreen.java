package app.docreader;

import android.view.View;
import android.widget.*;

final class TextScreen implements ReaderScreen {
    private final MainActivity activity;
    private final Document document;
    private final LinearLayout root;
    private final ScrollView scroll;
    private final TextView text, sizeLabel;
    private final Button smaller, larger;
    private int size;

    TextScreen(MainActivity activity, Document doc, String source) {
        this.activity = activity; document = doc;
        Ui ui = activity.ui;
        root = ui.column();
        scroll = new ScrollView(activity); scroll.setFillViewport(true);
        text = ui.text("", 18, ui.ink);
        text.setPadding(ui.dp(24), ui.dp(20), ui.dp(24), ui.dp(48));
        text.setText(source.isEmpty() ? activity.getString(R.string.empty_file)
            : doc.format == DocFormat.MARKDOWN ? Markdown.render(source) : source, TextView.BufferType.SPANNABLE);
        text.setTextIsSelectable(true); text.setLineSpacing(ui.dp(5), 1.15f);
        scroll.addView(text);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        LinearLayout bar = ui.row(); bar.setPadding(ui.dp(12), ui.dp(6), ui.dp(12), ui.dp(10));
        smaller = ui.button("A−", false, () -> resize(-2));
        smaller.setContentDescription(activity.getString(R.string.font_smaller));
        larger = ui.button("A+", false, () -> resize(2));
        larger.setContentDescription(activity.getString(R.string.font_larger));
        sizeLabel = ui.text("", 14, ui.muted); sizeLabel.setGravity(android.view.Gravity.CENTER);
        bar.addView(smaller);
        bar.addView(sizeLabel, new LinearLayout.LayoutParams(0, -2, 1)); bar.addView(larger);
        TextFind find = new TextFind(activity, text, scroll);
        bar.addView(ui.button(R.string.find, false, find::show)); root.addView(bar);
        size = activity.library.prefs.getInt("textSize", 18); resize(0);
        scroll.post(() -> scroll.scrollTo(0, activity.library.position(doc, ".scroll")));
    }
    private void resize(int change) {
        size = Math.max(14, Math.min(32, size+change)); text.setTextSize(size);
        sizeLabel.setText(String.valueOf(size));
        sizeLabel.setContentDescription(activity.getString(R.string.text_size) + " " + size);
        smaller.setEnabled(size > 14); larger.setEnabled(size < 32);
        activity.library.prefs.edit().putInt("textSize", size).apply();
    }
    @Override public View view() { return root; }
    @Override public void save() { activity.library.position(document, ".scroll", scroll.getScrollY()); }
    @Override public void close() {}
}
