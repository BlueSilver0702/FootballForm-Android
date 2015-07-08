package uk.co.createanet.footballformapp.lib;

import android.content.Context;
import android.content.SharedPreferences;

import uk.co.createanet.footballformapp.data.FFDatabase;

/**
 * Created by matt on 28/07/2014.
 */
public class CountrySelector {

    private static final String PREFS_NAME = "ff_country";
    private static final int DEFAULT_COUNTRY = 40011;

    public static int getLastCountry(Context c){
        SharedPreferences settings = c.getSharedPreferences(PREFS_NAME, 0);
        return settings.getInt("country", DEFAULT_COUNTRY);
    }

    public static String getLastContinentName(Context c, FFDatabase db){
        int country = getLastCountry(c);
        return db.getContinent(country, "continent");
    }

    public static String getLastCountryName(Context c, FFDatabase db){
        int country = getLastCountry(c);
        return db.getContinent(country, "name");
    }

    public static void setCountryId(Context c, int countryId){
        SharedPreferences settings = c.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putInt("country", countryId);
        editor.commit();
    }

}
