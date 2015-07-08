package uk.co.createanet.footballformapp.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by matt on 03/07/2014.
 */
public class Card extends BaseModel {

    public Card(JSONObject object) throws JSONException, IllegalAccessException {
        super(object);
    }

    public Card() {}

    public String player_name;
    public String time_player_got_card;
    public String home_or_away;
    public String card_type;

    public boolean isHomeTeam(){
        return home_or_away != null && home_or_away.compareToIgnoreCase("HOME") == 0;
    }

}
