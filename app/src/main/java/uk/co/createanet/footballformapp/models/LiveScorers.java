package uk.co.createanet.footballformapp.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by matt on 03/07/2014.
 */
public class LiveScorers extends BaseModel {

    public LiveScorers(JSONObject object) throws JSONException, IllegalAccessException {
        JSONArray cardsData = object.getJSONArray("cards");
        JSONArray scorersData = object.getJSONArray("scorers");

        scorers = new ArrayList<Scorer>();
        cards = new ArrayList<Card>();

        matchData = new LiveScore(object.getJSONArray("match_data").getJSONObject(0));

        for(int i = 0, j = cardsData.length(); i < j; i++){
            cards.add(new Card(cardsData.getJSONObject(i)));
        }

        for(int i = 0, j = scorersData.length(); i < j; i++){
            scorers.add(new Scorer(scorersData.getJSONObject(i)));
        }
    }

    public LiveScore matchData;
    public ArrayList<Scorer> scorers;
    public ArrayList<Card> cards;

}
