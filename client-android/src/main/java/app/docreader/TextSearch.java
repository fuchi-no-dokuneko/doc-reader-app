package app.docreader;

public final class TextSearch {
    public static int next(String text, String query, int start) {
        if (query.isEmpty()) return -1;
        int from = Math.min(Math.max(0, start), text.length());
        for (int i = from; i <= text.length()-query.length(); i++)
            if (text.regionMatches(true, i, query, 0, query.length())) return i;
        for (int i = 0; i < from && i <= text.length()-query.length(); i++)
            if (text.regionMatches(true, i, query, 0, query.length())) return i;
        return -1;
    }
    private TextSearch() {}
}
