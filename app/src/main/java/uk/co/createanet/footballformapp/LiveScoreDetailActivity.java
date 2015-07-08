package uk.co.createanet.footballformapp;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.co.createanet.footballformapp.lib.CreateaResponseHandler;
import uk.co.createanet.footballformapp.lib.RestClient;
import uk.co.createanet.footballformapp.models.Card;
import uk.co.createanet.footballformapp.models.LiveScorers;
import uk.co.createanet.footballformapp.models.Scorer;

public class LiveScoreDetailActivity extends AdvertActivity {

    public static final String KEY_LIVE_SCORE_ID = "live_score_id";
    private int liveScoreId;

    private TextView team1TextView, team2TextView, text_score;

    private ListView listView;
    private LiveScoresAdapter mAdapter;

    private LiveScorers matchInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_scores_detail);

        Bundle b = getIntent().getExtras();
        if(b != null){
            liveScoreId = b.getInt(KEY_LIVE_SCORE_ID);
        }

        setTitle("Live Score");

        team1TextView = (TextView)findViewById(R.id.team_1);
        team2TextView = (TextView)findViewById(R.id.team_2);
        text_score = (TextView)findViewById(R.id.text_score);

        listView = (ListView)findViewById(R.id.list_view);

        mAdapter = new LiveScoresAdapter(LiveScoreDetailActivity.this, R.layout.row_live_score_detail);
        listView.setAdapter(mAdapter);

        getLiveScorers();
    }

    public void getLiveScorers(){

        // TODO: Use the country API that isn't hacked to always give results
        RequestParams params = new RequestParams();
        params.add("match_id", String.valueOf(liveScoreId));
        params.add("type", "GET_GAME_DATA");

        RestClient.get(LiveScoreDetailActivity.this, "live_scores_api_country_id.php", params, new CreateaResponseHandler(LiveScoreDetailActivity.this) {

            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);

                try {
                    matchInfo = new LiveScorers(jsonObject.getJSONObject("data"));

                    team1TextView.setText(matchInfo.matchData.team_home_name);
                    team2TextView.setText(matchInfo.matchData.team_away_name);
                    text_score.setText(matchInfo.matchData.score);

                    mAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                if(matchInfo == null || matchInfo.matchData == null){
                    Toast.makeText(LiveScoreDetailActivity.this, "No match data is available at this time", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

        });

    }

    public enum RowType {
        ROW_SCORER, ROW_RED_CARD
    }

    public class LiveScoresAdapter extends ArrayAdapter<JSONArray> {

        public LiveScoresAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getViewTypeCount() {
            return RowType.values().length;
        }

        @Override
        public int getItemViewType(int pos) {

            if(pos == 0 && matchInfo.scorers.size() > 0){
                return RowType.ROW_SCORER.ordinal();
            }

            return RowType.ROW_RED_CARD.ordinal();
        }

        @Override
        public int getCount() {
            if(matchInfo == null) return 0;

            int rowCount = 0;
            if(matchInfo.scorers.size() > 0){
                rowCount++;
            }

            if(matchInfo.cards.size() > 0){
                rowCount++;
            }

            return rowCount;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            int rowType = getItemViewType(position);

            if(rowType == RowType.ROW_SCORER.ordinal()){
                if (convertView == null) {
                    convertView = View.inflate(getContext(), R.layout.row_scorer, null);
                }

                LinearLayout left = (LinearLayout)convertView.findViewById(R.id.list_left);
                LinearLayout right = (LinearLayout)convertView.findViewById(R.id.list_right);
                TextView header_text = (TextView)convertView.findViewById(R.id.header_text);

                left.removeAllViews();
                right.removeAllViews();

                header_text.setText("Goal Scorers");

                for(Scorer s : matchInfo.scorers){
                    TextView tv = new TextView(LiveScoreDetailActivity.this);
                    tv.setText(s.name + " (" + s.time + ")");

                    tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_football, 0, 0, 0);
                    tv.setCompoundDrawablePadding(10);

                    if (s.isHomeTeam()) {
                        left.addView(tv);
                    } else {
                        right.addView(tv);
                    }
                }

            } else if(rowType == RowType.ROW_RED_CARD.ordinal()) {
                if (convertView == null) {
                    convertView = View.inflate(getContext(), R.layout.row_live_scorer, null);
                }

                LinearLayout left = (LinearLayout)convertView.findViewById(R.id.list_left);
                LinearLayout right = (LinearLayout)convertView.findViewById(R.id.list_right);
                TextView header_text = (TextView)convertView.findViewById(R.id.header_text);

                left.removeAllViews();
                right.removeAllViews();

                header_text.setText("Cards");

                for(Card c : matchInfo.cards) {
                    TextView tv = new TextView(LiveScoreDetailActivity.this);
                    tv.setText(c.player_name + " (" + c.time_player_got_card + ")");

                    Drawable d = getResources().getDrawable(c.card_type.equals("Red") ? R.drawable.red_rounded : R.drawable.yellow_rounded);
                    d.setBounds(0, 0, 40, 50);

                    tv.setCompoundDrawables(d, null, null, null);
                    tv.setCompoundDrawablePadding(10);

                    if (c.isHomeTeam()) {
                        left.addView(tv);
                    } else {
                        right.addView(tv);
                    }

                }

            }

            return convertView;

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_refresh, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            getLiveScorers();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
