package app.docreader;

import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.*;
import java.util.regex.*;

final class Markdown {
    static CharSequence render(String source) {
        SpannableStringBuilder result = new SpannableStringBuilder();
        boolean code = false;
        for (String raw : source.split("\n", -1)) {
            String line = raw.endsWith("\r") ? raw.substring(0, raw.length()-1) : raw;
            if (line.startsWith("```")) { code = !code; result.append('\n'); continue; }
            int level = 0;
            while (level < line.length() && level < 6 && line.charAt(level) == '#') level++;
            boolean heading = !code && level > 0 && line.length() > level && line.charAt(level) == ' ';
            if (heading) line = line.substring(level+1);
            if (!code && (line.startsWith("- ") || line.startsWith("* "))) line = "• " + line.substring(2);
            boolean quote = !code && line.startsWith("> ");
            if (quote) line = line.substring(2);
            int start = result.length();
            result.append(code ? line : inline(line)).append('\n');
            int end = result.length();
            if (heading) {
                span(result, new StyleSpan(Typeface.BOLD), start, end);
                span(result, new RelativeSizeSpan(level <= 2 ? 1.45f : 1.15f), start, end);
            }
            if (code) span(result, new TypefaceSpan("monospace"), start, end);
            if (quote) span(result, new QuoteSpan(0xff608777), start, end);
        }
        return result;
    }
    private static CharSequence inline(String source) {
        SpannableStringBuilder result = new SpannableStringBuilder();
        Matcher match = Pattern.compile("\\*\\*(.+?)\\*\\*|`([^`]+)`|\\*([^*]+)\\*").matcher(source);
        int cursor = 0;
        while (match.find()) {
            result.append(source, cursor, match.start());
            int start = result.length();
            String bold = match.group(1), code = match.group(2);
            result.append(bold != null ? bold : code != null ? code : match.group(3));
            span(result, code != null ? new TypefaceSpan("monospace")
                : new StyleSpan(bold != null ? Typeface.BOLD : Typeface.ITALIC), start, result.length());
            cursor = match.end();
        }
        return result.append(source, cursor, source.length());
    }
    private static void span(SpannableStringBuilder text, Object style, int start, int end) {
        text.setSpan(style, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
    private Markdown() {}
}
