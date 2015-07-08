package uk.co.createanet.footballformapp.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by matt on 03/07/2014.
 */
public class LiveScore extends BaseModel {

    public LiveScore(JSONObject object) throws JSONException, IllegalAccessException {
        super(object);
    }

    public int id;
    public String team_home_name;
    public String team_away_name;
    public String start_time;
    public String score;
    public String game_status;
    public String league_name;

    public boolean isFullTime(){
        return game_status != null && game_status.equals("Fin");
    }

}
