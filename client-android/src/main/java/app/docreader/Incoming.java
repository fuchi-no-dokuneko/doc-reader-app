package app.docreader;

import android.content.Intent;
import android.net.Uri;

final class Incoming {
    static Uri uri(Intent intent) {
        if (intent == null) return null;
        Uri uri = null;
        if (Intent.ACTION_VIEW.equals(intent.getAction())) uri = intent.getData();
        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        }
        if (uri == null && (Intent.ACTION_VIEW.equals(intent.getAction()) || Intent.ACTION_SEND.equals(intent.getAction()))
            && intent.getClipData() != null && intent.getClipData().getItemCount() > 0) {
            uri = intent.getClipData().getItemAt(0).getUri();
        }
        if (uri == null) return null;
        return "content".equals(uri.getScheme()) || "file".equals(uri.getScheme()) ? uri : null;
    }
    static Intent picker() {
        return new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE)
            .setType("*/*").putExtra(Intent.EXTRA_MIME_TYPES,
                new String[]{"application/pdf", "text/plain", "text/markdown", "text/x-markdown", "application/octet-stream"});
    }
    private Incoming() {}
}
