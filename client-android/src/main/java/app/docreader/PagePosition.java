package app.docreader;

public final class PagePosition {
    public static int clamp(int requested, int count) {
        return Math.max(0, Math.min(requested, count - 1));
    }
    public static int parse(String input, int count) {
        try {
            int page = Integer.parseInt(input.trim());
            return page >= 1 && page <= count ? page - 1 : -1;
        } catch (NumberFormatException error) { return -1; }
    }
    private PagePosition() {}
}
