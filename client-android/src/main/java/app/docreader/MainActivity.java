package app.docreader;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.window.OnBackInvokedDispatcher;

public final class MainActivity extends Activity {
    Ui ui;
    Library library;
    AppShell shell;
    OpenController controller;

    @Override public void onCreate(Bundle state) {
        Appearance.apply(this);
        super.onCreate(state);
        library = new Library(this); ui = new Ui(this);
        shell = new AppShell(this, ui); controller = new OpenController(this);
        if (Build.VERSION.SDK_INT >= 33) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT, this::back);
        }
        String saved = state == null ? null : state.getString("document");
        Document doc = saved == null ? null : library.find(saved);
        if (doc != null) open(doc);
        else if (state != null) home();
        else accept(getIntent());
    }
    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent); setIntent(intent); accept(intent);
    }
    private void accept(Intent intent) {
        Uri uri = Incoming.uri(intent);
        if (uri != null) controller.importUri(uri, intent.getType());
        else if (Intent.ACTION_SEND.equals(intent.getAction()) && intent.hasExtra(Intent.EXTRA_TEXT))
            controller.importText(intent.getStringExtra(Intent.EXTRA_TEXT));
        else home();
    }
    void home() {
        controller.clear();
        shell.display(getString(R.string.app_name), false, HomeScreen.create(this));
    }
    void pick() { startActivityForResult(Incoming.picker(), 10); }
    void open(Document doc) { controller.open(doc); }
    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request == 10 && result == RESULT_OK && data != null && data.getData() != null)
            controller.importUri(data.getData(), data.getType());
    }
    private void back() { if (controller.reading()) home(); else finish(); }
    // API 26–32 fallback; API 33+ uses the native dispatcher registered above.
    @android.annotation.SuppressLint("GestureBackNavigation")
    @Override public void onBackPressed() { back(); }
    @Override protected void onSaveInstanceState(Bundle state) {
        controller.save();
        if (controller.document != null) state.putString("document", controller.document.id);
        super.onSaveInstanceState(state);
    }
    @Override protected void onStop() { controller.save(); super.onStop(); }
    @Override protected void onDestroy() { controller.destroy(); super.onDestroy(); }
}
