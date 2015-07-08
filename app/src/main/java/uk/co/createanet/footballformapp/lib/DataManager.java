package uk.co.createanet.footballformapp.lib;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;

import uk.co.createanet.footballformapp.data.FFDatabase;

/**
 * Created by matt on 07/08/2014.
 */
public class DataManager {

    private static final String PREFS_NAME = "ff_db";
    private static final String DATABASE = "ff_db_name";

    private static final int NO_OPENS = 3;

    public static String getDatabasePath(Context c) {
        SharedPreferences settings = c.getSharedPreferences(PREFS_NAME, 0);
        return settings.getString(DATABASE, null);
    }


    public static void setDatabasePath(Context c, String path) {
        SharedPreferences settings = c.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString(DATABASE, path);
        editor.commit();
    }

    public static void storeFavouriteTeams(final Context context, FFDatabase db) {

        db.getFavouriteIds(new FFDatabase.QueryListener() {
            @Override
            public void onQueryComplete(Cursor c) {
                SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();

                ArrayList<String> out = new ArrayList<String>();
                if(c.getCount() > 0) {
                    do {
                        out.add(c.getString(c.getColumnIndex("_id")) + "_" + c.getString(c.getColumnIndex("league_id")));
                    } while (c.moveToNext());
                }

                editor.putString("favors", TextUtils.join("+", out));
                editor.commit();
            }
        });

    }

    public static void restoreFavouriteTeams(final Context context, FFDatabase db) {

        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        String items = settings.getString("favors", null);

        if(items != null){
            String[] favs = items.split("\\+");

            for(int i = 0, j = favs.length; i < j; i++){
                String[] parts = favs[i].split("_");

                if(parts.length == 2) {
                    String id = parts[0];
                    String leagueId = parts[1];

                    String[] params = new String[]{id, leagueId};

                    SQLiteDatabase db2 = db.getReadableDatabase();
                    Cursor c = db2.rawQuery("UPDATE teams" +
                            " SET is_favourite = 'Y'" +
                            " WHERE _id = ? AND league_id = ?", params);

                    c.moveToFirst();
                }

            }
        }

    }

    public static boolean shouldShowPurchasePrompt(Context c){
        SharedPreferences settings = c.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        int currentOpen = settings.getInt("no_opens", 0);

        currentOpen = currentOpen + 1;

        boolean shouldShow = false;
        if(currentOpen > NO_OPENS){
            currentOpen = 0;
            shouldShow = true;
        }

        editor.putInt("no_opens", currentOpen);
        editor.commit();

        Log.d("FF", "No opens: " + currentOpen);

        return shouldShow;
    }

    public static String formatNumberSuffix(int number){
        String numberOut;

        String[] suffixes = new String[] { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th" };
        if ((number % 100) >= 11 && (number % 100) <= 13) {
            numberOut = number + "th";
        } else {
            numberOut = number + suffixes[number % 10];
        }

        return numberOut;
    }
}
