package app.docreader;

import android.view.View;

interface ReaderScreen {
    View view();
    void save();
    void close();
}
