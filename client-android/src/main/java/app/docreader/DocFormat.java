package app.docreader;

import java.util.Locale;

public enum DocFormat {
    PDF, TEXT, MARKDOWN;

    public static DocFormat detect(String name, String mime) {
        String lower = name == null ? "" : name.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".pdf")) return PDF;
        if (lower.endsWith(".md") || lower.endsWith(".markdown")) return MARKDOWN;
        if (lower.endsWith(".txt")) return TEXT;
        String type = mime == null ? "" : mime.split(";", 2)[0].trim();
        if (type.equalsIgnoreCase("application/pdf")) return PDF;
        if (type.equalsIgnoreCase("text/plain")) return TEXT;
        if (type.equalsIgnoreCase("text/markdown") || type.equalsIgnoreCase("text/x-markdown")) return MARKDOWN;
        throw new IllegalArgumentException("Unsupported document");
    }

    public String label() { return this == TEXT ? "TXT" : this == MARKDOWN ? "MD" : "PDF"; }
    public long limit() { return (this == PDF ? 50L : 4L) * 1024 * 1024; }
}
