package autoservisi.Autoservisi.MakinaIme.preferences;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    private SharedPreferences preferences;
    private static final String PREFERENCE_NAME = "myCarPrefs";
    private static final String IS_FIRST_LAUNCH = "isFirstLaunch";

    public PreferenceManager(Context context, int mode) {
        preferences = context.getSharedPreferences(PREFERENCE_NAME, mode);
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        preferences.edit().putBoolean(IS_FIRST_LAUNCH, isFirstTime).apply();
    }

    public boolean isFirstLaunch() {
        return preferences.getBoolean(IS_FIRST_LAUNCH, true);
    }
}
