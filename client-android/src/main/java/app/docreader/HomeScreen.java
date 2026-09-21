package app.docreader;

import android.graphics.Typeface;
import android.view.View;
import android.widget.*;
import java.util.List;

final class HomeScreen {
    static View create(MainActivity activity) {
        Ui ui = activity.ui;
        ScrollView scroll = new ScrollView(activity);
        scroll.setFillViewport(true);
        LinearLayout body = ui.column();
        body.setPadding(ui.dp(24), ui.dp(20), ui.dp(24), ui.dp(28));
        TextView title = ui.text(R.string.home_title, 34, ui.ink);
        title.setTypeface(Typeface.create("serif", Typeface.NORMAL));
        body.addView(title); ui.gap(body, 12);
        body.addView(ui.text(R.string.home_subtitle, 16, ui.muted));
        ui.gap(body, 24);
        LinearLayout card = ui.column();
        card.setPadding(ui.dp(20), ui.dp(24), ui.dp(20), ui.dp(20));
        card.setBackground(ui.shape(ui.surface, 24));
        ImageView icon = new ImageView(activity);
        icon.setImageResource(R.drawable.ic_reader);
        card.addView(icon, new LinearLayout.LayoutParams(ui.dp(48), ui.dp(48)));
        ui.gap(card, 20);
        card.addView(ui.button(R.string.open_document, true, activity::pick), new LinearLayout.LayoutParams(-1, ui.dp(56)));
        ui.gap(card, 16);
        TextView types = ui.text("PDF   ·   TXT   ·   MARKDOWN", 12, ui.muted);
        types.setLetterSpacing(.10f); card.addView(types);
        body.addView(card); ui.gap(body, 16);
        body.addView(ui.text(R.string.private_hint, 13, ui.muted));
        ui.gap(body, 32);
        TextView heading = ui.text(R.string.recent, 22, ui.ink);
        heading.setTypeface(null, Typeface.BOLD); body.addView(heading);
        ui.gap(body, 14);
        List<Document> documents = activity.library.list();
        if (documents.isEmpty()) {
            body.addView(ui.text(R.string.empty_title, 17, ui.ink)); ui.gap(body, 8);
            body.addView(ui.text(R.string.empty_hint, 15, ui.muted));
        } else {
            for (Document doc : documents) {
                body.addView(RecentRow.create(activity, doc)); ui.gap(body, 10);
            }
            body.addView(ui.text(R.string.retention, 12, ui.muted));
        }
        scroll.addView(body);
        return scroll;
    }
}
