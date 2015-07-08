package uk.co.createanet.footballformapp.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by matt on 03/07/2014.
 */
public class Scorer extends BaseModel {

    public Scorer(JSONObject object) throws JSONException, IllegalAccessException {
        super(object);
    }

    public String name;
    public String time;
    public String home_or_away;

    public Scorer(){

    }

    public boolean isHomeTeam(){
        return home_or_away != null && home_or_away.compareToIgnoreCase("HOME") == 0;
    }

}
