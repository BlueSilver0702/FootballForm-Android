package uk.co.createanet.footballformapp.lib;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by matt on 06/08/2014.
 */
public class PushMessaging {

    public static final String PREFS_NAME = "ff_token";

    public static void saveToken(Context c, String token) {
        SharedPreferences settings = c.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("token", token);

        editor.commit();
    }

    public static void clearToken(Context c) {
        SharedPreferences settings = c.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove("token");

        editor.commit();
    }

    public static String getToken(Context c) {
        SharedPreferences settings = c.getSharedPreferences(PREFS_NAME, 0);
        return settings.getString("token", null);
    }

}
