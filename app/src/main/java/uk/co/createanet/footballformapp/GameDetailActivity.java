package uk.co.createanet.footballformapp;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;

import uk.co.createanet.footballformapp.data.FFDatabase;
import uk.co.createanet.footballformapp.models.Card;
import uk.co.createanet.footballformapp.models.Scorer;
import uk.co.createanet.footballformapp.views.HorizontalBarGraph;

public class GameDetailActivity extends AdvertActivity {

    private FFDatabase db;

    private int fixtureId;

    private ListView listView;
    private GameDetailsAdapter gameDetailsAdapter;
    private HashMap<String, String> matchInfo;

    private ArrayList<Scorer> goalScorers;
    private ArrayList<Card> redCards;
    private ArrayList<Card> yellowCards;
    private ArrayList<Scorer> lineup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_detail);

        setTitle("Fixture");

        Bundle b = getIntent().getExtras();
        if(b != null){
            fixtureId = b.getInt(FixtureDetailActivity.KEY_FIXTURE_ID);
        }

        final TextView team_1 = (TextView) findViewById(R.id.team_1);
        final TextView current_position_left = (TextView) findViewById(R.id.current_position_left);
        final TextView text_score = (TextView) findViewById(R.id.text_score);
        final TextView team_2 = (TextView) findViewById(R.id.team_2);
        final TextView current_position_right = (TextView) findViewById(R.id.current_position_right);
        final TextView league_name = (TextView) findViewById(R.id.league_name);

        listView = (ListView)findViewById(R.id.list_view);
        gameDetailsAdapter = new GameDetailsAdapter(GameDetailActivity.this, R.layout.row_game_details);
        listView.setAdapter(gameDetailsAdapter);

        db = new FFDatabase(this);

        Thread t = new Thread(){
            @Override
            public void run() {

                goalScorers = new ArrayList<Scorer>();
                yellowCards = new ArrayList<Card>();
                redCards = new ArrayList<Card>();
                lineup = new ArrayList<Scorer>();

                // get the header info
                matchInfo = db.getFixtureDetailsFull(fixtureId);

                // get the other details
                final Cursor cursorGoalsScored = db.getGoalsScored(fixtureId);
                final Cursor cursorYellowCards = db.getYellowCards(fixtureId);
                final Cursor cursorRedCards = db.getRedCards(fixtureId);
                final Cursor cursorLineups = db.getLineups(fixtureId);

                GameDetailActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        while(cursorGoalsScored.moveToNext()){
                            Scorer s = new Scorer();
                            s.name = cursorGoalsScored.getString(cursorGoalsScored.getColumnIndex("player_name"));
                            s.time = cursorGoalsScored.getString(cursorGoalsScored.getColumnIndex("time_scored_in"));
                            s.home_or_away = cursorGoalsScored.getString(cursorGoalsScored.getColumnIndex("team_side"));

                            goalScorers.add(s);
                        }

                        while(cursorYellowCards.moveToNext()){
                            Card c = new Card();
                            c.card_type = "Yellow";
                            c.player_name = cursorYellowCards.getString(cursorYellowCards.getColumnIndex("player_name"));
                            c.time_player_got_card = cursorYellowCards.getString(cursorYellowCards.getColumnIndex("time_player_got_card"));
                            c.home_or_away = cursorYellowCards.getString(cursorYellowCards.getColumnIndex("recieved_at"));

                            yellowCards.add(c);
                        }

                        while(cursorRedCards.moveToNext()){
                            Card c = new Card();
                            c.card_type = "Red";
                            c.player_name = cursorRedCards.getString(cursorRedCards.getColumnIndex("player_name"));
                            c.time_player_got_card = cursorRedCards.getString(cursorRedCards.getColumnIndex("time_player_got_card"));
                            c.home_or_away = cursorRedCards.getString(cursorRedCards.getColumnIndex("recieved_at"));

                            redCards.add(c);
                        }

                        while(cursorLineups.moveToNext()){
                            Scorer s = new Scorer();
                            s.name = cursorLineups.getString(cursorLineups.getColumnIndex("playerName"));

                            int teamId = cursorLineups.getInt(cursorLineups.getColumnIndex("teamid"));
                            s.home_or_away = (teamId == Integer.parseInt(matchInfo.get("teams_home_id"))) ? "HOME" : "AWAY";

                            lineup.add(s);
                        }

                        cursorGoalsScored.close();
                        cursorYellowCards.close();
                        cursorRedCards.close();
                        cursorLineups.close();

                        team_1.setText(matchInfo.get("teams_home_name"));
                        team_2.setText(matchInfo.get("teams_away_name"));
                        text_score.setText(matchInfo.get("team_home_score") + "-" + matchInfo.get("team_away_score"));
                        current_position_left.setText("Current Position: " + matchInfo.get("home_position"));
                        current_position_right.setText("Current Position: " + matchInfo.get("away_position"));
                        league_name.setText(matchInfo.get("league_name"));

                        gameDetailsAdapter.notifyDataSetChanged();
                    }
                });
            }
        };
        t.start();

    }


    public enum RowType {
        ROW_NOTES, ROW_GOALS, ROW_YELLOW_CARDS, ROW_RED_CARDS, ROW_LINEUPS, ROW_SCORERS
    }

    public class GameDetailsAdapter extends ArrayAdapter<JSONArray> {

        public GameDetailsAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getViewTypeCount() {
            return RowType.values().length;
        }

        @Override
        public int getItemViewType(int pos) {

            int offset = 0;
            if(matchInfo.get("notes").length() > 0){
                if(pos == 0) {
                    return RowType.ROW_NOTES.ordinal();
                }

                offset++;
            }

            if(yellowCards.size() > 0 && pos < (offset + 1)){
                return RowType.ROW_YELLOW_CARDS.ordinal();
            }

            offset += (yellowCards.size() > 0 ? 1 : 0);

            if(redCards.size() > 0 && pos < (offset + 1)){
                return RowType.ROW_RED_CARDS.ordinal();
            }

            offset += (redCards.size() > 0 ? 1 : 0);

            if(goalScorers.size() > 0 && pos < (offset + 1)){
                return RowType.ROW_GOALS.ordinal();
            }

            offset += (goalScorers.size() > 0 ? 1 : 0);

            if(goalScorers.size() > 0 && pos < (offset + 1)){
                return RowType.ROW_SCORERS.ordinal();
            }

            offset += (goalScorers.size() > 0 ? 1 : 0);

            if(lineup.size() > 0 && pos < (offset + 1)){
                return RowType.ROW_LINEUPS.ordinal();
            }

            return RowType.ROW_NOTES.ordinal();
        }

        @Override
        public int getCount() {
            if(matchInfo == null){
                return 0;
            }

            int noRows = 0;

            if(matchInfo.get("notes") == null){
                matchInfo.put("notes", "");
            }

            if(matchInfo.get("notes").length() > 0){
                noRows++;
            }

            noRows += goalScorers.size() > 0 ? 2 : 0;
            noRows += yellowCards.size() > 0 ? 1 : 0;
            noRows += redCards.size() > 0 ? 1 : 0;
            noRows += lineup.size() > 0 ? 1 : 0;

            return noRows;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            int rowType = getItemViewType(position);

            if(rowType == RowType.ROW_NOTES.ordinal()) {
                if (convertView == null) {
                    convertView = View.inflate(getContext(), R.layout.row_match_details, null);
                }

                TextView tv = (TextView)convertView.findViewById(R.id.text);
                tv.setText(matchInfo.get("notes"));

            } else if(rowType == RowType.ROW_GOALS.ordinal()){
                if (convertView == null) {
                    convertView = View.inflate(getContext(), R.layout.row_graph, null);
                }

                ((ViewGroup)convertView).removeAllViews();

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                HorizontalBarGraph scoreGraph = new HorizontalBarGraph(GameDetailActivity.this, "Goals", Integer.parseInt(matchInfo.get("team_home_score")), Integer.parseInt(matchInfo.get("team_away_score")));
                scoreGraph.setLayoutParams(lp);

                ((ViewGroup)convertView).addView(scoreGraph);

            } else if(rowType == RowType.ROW_YELLOW_CARDS.ordinal() || rowType == RowType.ROW_RED_CARDS.ordinal()){
                if (convertView == null) {
                    convertView = View.inflate(getContext(), R.layout.row_scorer, null);
                }

                LinearLayout left = (LinearLayout)convertView.findViewById(R.id.list_left);
                LinearLayout right = (LinearLayout)convertView.findViewById(R.id.list_right);
                TextView header_text = (TextView)convertView.findViewById(R.id.header_text);

                left.removeAllViews();
                right.removeAllViews();

                header_text.setText(rowType == RowType.ROW_RED_CARDS.ordinal() ? "Red Cards" : "Yellow Cards");

                for(Card c : rowType == RowType.ROW_RED_CARDS.ordinal() ? redCards : yellowCards) {
                    TextView tv = new TextView(GameDetailActivity.this);
                    tv.setText(c.player_name);

                    if (c.isHomeTeam()) {
                        left.addView(tv);
                    } else {
                        right.addView(tv);
                    }
                }

            } else if(rowType == RowType.ROW_LINEUPS.ordinal() || rowType == RowType.ROW_SCORERS.ordinal()) {
                if (convertView == null) {
                    convertView = View.inflate(getContext(), R.layout.row_scorer, null);
                }

                LinearLayout left = (LinearLayout)convertView.findViewById(R.id.list_left);
                LinearLayout right = (LinearLayout)convertView.findViewById(R.id.list_right);
                TextView header_text = (TextView)convertView.findViewById(R.id.header_text);

                left.removeAllViews();
                right.removeAllViews();

                header_text.setText(rowType == RowType.ROW_LINEUPS.ordinal() ? "Lineup" : "Goal Scorers");

                for(Scorer s : rowType == RowType.ROW_LINEUPS.ordinal() ? lineup : goalScorers){
                    TextView tv = new TextView(GameDetailActivity.this);
                    tv.setText(s.name);

                    if (s.isHomeTeam()) {
                        left.addView(tv);
                    } else {
                        right.addView(tv);
                    }
                }

            }

            return convertView;

        }
    }

}
