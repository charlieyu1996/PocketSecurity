package pocketsecurity.agroupofstudents.com.pocketsecurity.services;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Daniel on 6/24/2019.
 */

public class Preferences {
    private static final String APP_SHARED_PREFS = "com.agroupofstudents.pocketsecurity";

    private static final String HASHED_PASSWORD = "HASHED_PASSWORD";
    private static final String PASSWORD_SALT = "PASSWORD_SALT";
    private static final String IS_APP_PASSWORD_PROTECTED = "IS_APP_PASSWORD_PROTECTED";
    private static final String DEVICE_KEY = "DEVICE_KEY";

    private SharedPreferences appSharedPrefs;
    private SharedPreferences.Editor prefsEditor;

    public Preferences (Context context) {
        this.appSharedPrefs = context.getSharedPreferences(APP_SHARED_PREFS, Activity.MODE_PRIVATE);
        this.prefsEditor = appSharedPrefs.edit();
    }

    public void setHashedPassword(String hashedPassword) {
        prefsEditor.putString(HASHED_PASSWORD, hashedPassword);
        prefsEditor.commit();
    }

    public String getHashedPassword() {
        return appSharedPrefs.getString(HASHED_PASSWORD, null);
    }

    public void setPasswordSalt(String salt) {
        prefsEditor.putString(PASSWORD_SALT, salt);
        prefsEditor.commit();
    }

    public String getPasswordSalt() {
        return appSharedPrefs.getString(PASSWORD_SALT, null);
    }

    public void setIsAppPasswordProtected(boolean isAppPasswordProtected) {
        prefsEditor.putBoolean(IS_APP_PASSWORD_PROTECTED, isAppPasswordProtected);
        prefsEditor.commit();
    }

    public boolean getIsAppPasswordProtected() {
        return appSharedPrefs.getBoolean(IS_APP_PASSWORD_PROTECTED, false);
    }

    public void setDeviceKey(String deviceKey) {
        prefsEditor.putString(DEVICE_KEY, deviceKey);
        prefsEditor.commit();
    }

    public String getDeviceKey() {
        return appSharedPrefs.getString(DEVICE_KEY, null);
    }
}
