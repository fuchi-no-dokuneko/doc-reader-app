package app.docreader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.Configuration;

final class Appearance {
    static boolean dark(Activity activity) {
        int mode = activity.getPreferences(0).getInt("theme", 0);
        return mode == 2 || (mode == 0 && (activity.getResources().getConfiguration().uiMode
            & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES);
    }
    static void apply(Activity activity) {
        activity.setTheme(dark(activity) ? R.style.ReaderDark : R.style.ReaderLight);
    }
    static void choose(Activity activity) {
        String[] items = {activity.getString(R.string.system_theme), activity.getString(R.string.light_theme),
            activity.getString(R.string.dark_theme)};
        new AlertDialog.Builder(activity).setTitle(R.string.appearance)
            .setSingleChoiceItems(items, activity.getPreferences(0).getInt("theme", 0), (dialog, index) -> {
                activity.getPreferences(0).edit().putInt("theme", index).apply();
                dialog.dismiss(); activity.recreate();
            }).setNegativeButton(R.string.cancel, null).show();
    }
    private Appearance() {}
}
