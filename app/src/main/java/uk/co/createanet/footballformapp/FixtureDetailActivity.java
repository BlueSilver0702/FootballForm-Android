package uk.co.createanet.footballformapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.HashMap;

import uk.co.createanet.footballformapp.data.FFDatabase;
import uk.co.createanet.footballformapp.fragments.tab_containers.tabs.ScrollViewFragment;
import uk.co.createanet.footballformapp.lib.DataManager;
import uk.co.createanet.footballformapp.models.StoreItem;
import uk.co.createanet.footballformapp.models.StoreManager;
import uk.co.createanet.footballformapp.util.Purchase;

public class FixtureDetailActivity extends AdvertActivity implements AdapterView.OnItemClickListener {

    public static final String KEY_FIXTURE_ID = "fixture_id";
    public static final String KEY_TEAM_HOME_ID = "home_id";
    public static final String KEY_TEAM_AWAY_ID = "away_id";

    public static final String KEY_LEAGUE_ID = "league_id";

    private int home_team_id, away_team_id;
    private int no_games = 3;
    private int fixture_id;

    private TextView team_1;
    private RadioButton seg_all_left;
    private RadioButton seg_home_left;
    private RadioButton seg_away_left;
    private TextView team_2;
    private RadioButton seg_all_right;
    private RadioButton seg_home_right;
    private RadioButton seg_away_right;
    private ListView list_left;
    private ListView list_right;

    private TextView total_points_left;
    private TextView total_points_right;

    private TextView total_goals_left;
    private TextView total_goals_right;

    private TextView pos_league_left;
    private TextView pos_league_right;

    private ScrollViewFragment.FILTER_TYPE filter_left = ScrollViewFragment.FILTER_TYPE.HOME;
    private ScrollViewFragment.FILTER_TYPE filter_right = ScrollViewFragment.FILTER_TYPE.AWAY;

    private FFDatabase db;

    private int leftTeamScore = 0, rightTeamScore = 0;
    private int totalGoalsLeft = 0, totalGoalsRight = 0;

    private StoreManager storeManager;
    private StoreItem gamesStoreItem;

    private int leagueId;
    private FixtureDetailActivity fixtureDetailActivity;

//    private Button button_betting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fixture_detail);

        setTitle("Momentum Stats");

        fixtureDetailActivity = this;

        Bundle b = getIntent().getExtras();
        if(b != null){
            fixture_id = b.getInt(KEY_FIXTURE_ID);
            leagueId = b.getInt(KEY_LEAGUE_ID);
        }

        storeManager = new StoreManager(FixtureDetailActivity.this);
        storeManager.initialiseInAppPurchase(FixtureDetailActivity.this, new StoreManager.PurchaseStoreReady() {
            @Override
            public void storeReady() {

                storeManager.checkPurchasedItems(new StoreManager.PurchaseUpdateListener() {
                    @Override
                    public void updateSuccess() {
                        gamesStoreItem = StoreManager.getById(FixtureDetailActivity.this, StoreManager.ID_GAMES);
                    }

                    @Override
                    public void updateFailed() {
//                        Toast.makeText(PlayerVsDetailActivity.this, "We were unable to communicate with the Google Store. If you're missing purchased items, please check back shortly", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void storeFailed() {

            }
        });

        db = new FFDatabase(this);

        team_1 = (TextView) findViewById(R.id.team_1);
        seg_all_left = (RadioButton) findViewById(R.id.seg_all_left);
        seg_home_left = (RadioButton) findViewById(R.id.seg_home_left);
        seg_away_left = (RadioButton) findViewById(R.id.seg_away_left);
        team_2 = (TextView) findViewById(R.id.team_2);
        seg_all_right = (RadioButton) findViewById(R.id.seg_all_right);
        seg_home_right = (RadioButton) findViewById(R.id.seg_home_right);
        seg_away_right = (RadioButton) findViewById(R.id.seg_away_right);
        list_left = (ListView) findViewById(R.id.list_left);
        list_right = (ListView) findViewById(R.id.list_right);
        total_points_left = (TextView) findViewById(R.id.total_points_left);
        total_points_right = (TextView) findViewById(R.id.total_points_right);

        total_goals_left = (TextView) findViewById(R.id.total_goals_left);
        total_goals_right = (TextView) findViewById(R.id.total_goals_right);

        pos_league_left = (TextView) findViewById(R.id.pos_league_left);
        pos_league_right = (TextView) findViewById(R.id.pos_league_right);

//        button_betting = (Button) findViewById(R.id.button_betting);

        seg_home_left.setChecked(true);
        seg_away_right.setChecked(true);

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v == seg_all_left){
                    filter_left = ScrollViewFragment.FILTER_TYPE.ALL;
                    getGames(home_team_id, list_left);
                } else if (v == seg_all_right){
                    filter_right = ScrollViewFragment.FILTER_TYPE.ALL;
                    getGames(away_team_id, list_right);
                } else if (v == seg_home_left){
                    filter_left = ScrollViewFragment.FILTER_TYPE.HOME;
                    getGames(home_team_id, list_left);
                } else if (v == seg_home_right){
                    filter_right = ScrollViewFragment.FILTER_TYPE.HOME;
                    getGames(away_team_id, list_right);
                } else if (v == seg_away_left){
                    filter_left = ScrollViewFragment.FILTER_TYPE.AWAY;
                    getGames(home_team_id, list_left);
                } else if (v == seg_away_right){
                    filter_right = ScrollViewFragment.FILTER_TYPE.AWAY;
                    getGames(away_team_id, list_right);
                }
            }
        };

        seg_all_right.setOnClickListener(clickListener);
        seg_all_left.setOnClickListener(clickListener);
        seg_home_left.setOnClickListener(clickListener);
        seg_home_right.setOnClickListener(clickListener);
        seg_away_left.setOnClickListener(clickListener);
        seg_away_right.setOnClickListener(clickListener);

        db.getFixtureDetails(fixture_id, new FFDatabase.QueryListener() {
            @Override
            public void onQueryComplete(Cursor c) {
            }

            @Override
            public void onQueryComplete(HashMap<String, String> map) {
                home_team_id = Integer.parseInt(map.get("teams_home_id"));
                away_team_id = Integer.parseInt(map.get("teams_away_id"));

                getFixtureDetails();
            }
        });

        /*
        button_betting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://affiliatehub.skybet.com/processing/clickthrgh.asp?btag=a_15777b_1"));
                startActivity(intent);
            }
        });
        */
    }

    public void getFixtureDetails(){

        db.getTeamDetails(home_team_id, leagueId, new FFDatabase.QueryListener() {
            @Override
            public void onQueryComplete(Cursor c) {}

            @Override
            public void onQueryComplete(HashMap<String, String> map){
                team_1.setText(map.get("name"));
                pos_league_left.setText(DataManager.formatNumberSuffix(Integer.valueOf(map.get("position"))) + " in league table");
            }
        });

        db.getTeamDetails(away_team_id, leagueId, new FFDatabase.QueryListener() {
            @Override
            public void onQueryComplete(Cursor c) {}

            @Override
            public void onQueryComplete(HashMap<String, String> map){
                team_2.setText(map.get("name"));
                pos_league_right.setText(DataManager.formatNumberSuffix(Integer.valueOf(map.get("position"))) + " in league table");
            }

        });

        getGames(home_team_id, list_left);
        getGames(away_team_id, list_right);
    }

    public void getTotalPoints(int teamId, final TextView tv){
        db.getPointsForTeam(teamId, new Date(), no_games, tv == total_points_left ? filter_left : filter_right, new FFDatabase.QueryListener() {
            @Override
            public void onQueryComplete(Cursor c) {}

            @Override
            public void onQueryComplete(HashMap<String, String> map) {
                tv.setText("Total Points: " + map.get("total_points"));

                if(tv == total_points_left){
                    leftTeamScore = Integer.parseInt(map.get("total_points"));
                    totalGoalsLeft = Integer.parseInt(map.get("total_goals"));

                    total_goals_left.setText("Total Goals: " + String.valueOf(totalGoalsLeft));
                } else {
                    rightTeamScore = Integer.parseInt(map.get("total_points"));
                    totalGoalsRight = Integer.parseInt(map.get("total_goals"));

                    total_goals_right.setText("Total Goals: " + String.valueOf(totalGoalsRight));
                }

                if(leftTeamScore > rightTeamScore) {
                    total_points_left.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_star, 0, 0, 0);
                    total_points_right.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.star_placeholder, 0);
                } else if(rightTeamScore > leftTeamScore){
                    total_points_right.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_star, 0);
                    total_points_left.setCompoundDrawablesWithIntrinsicBounds(R.drawable.star_placeholder, 0, 0, 0);
                } else {
                    total_points_left.setCompoundDrawablesWithIntrinsicBounds(R.drawable.star_placeholder, 0, 0, 0);
                    total_points_right.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.star_placeholder, 0);
                }

                if(totalGoalsLeft > totalGoalsRight) {
                    total_goals_left.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_star, 0, 0, 0);
                    total_goals_right.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.star_placeholder, 0);
                } else if(totalGoalsRight > totalGoalsLeft){
                    total_goals_right.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_star, 0);
                    total_goals_left.setCompoundDrawablesWithIntrinsicBounds(R.drawable.star_placeholder, 0, 0, 0);
                } else {
                    total_goals_left.setCompoundDrawablesWithIntrinsicBounds(R.drawable.star_placeholder, 0, 0, 0);
                    total_goals_right.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.star_placeholder, 0);
                }

            }
        });
    }

    public void getGames(final int teamId, final ListView listView){
        db.getPreviousGamesForTeam(leagueId, teamId, new Date(), no_games, listView == list_left ? filter_left : filter_right,
                new FFDatabase.QueryListener() {
            @Override
            public void onQueryComplete(Cursor c) {
                ClientCursorAdapter fixtureAwayAdapter = new ClientCursorAdapter(FixtureDetailActivity.this, R.layout.row_fixture_detail, c, 0, teamId);

                getTotalPoints(home_team_id, total_points_left);
                getTotalPoints(away_team_id, total_points_right);

                listView.setAdapter(fixtureAwayAdapter);
                listView.setOnItemClickListener(fixtureDetailActivity);
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Intent i = new Intent(FixtureDetailActivity.this, GameDetailActivity.class);
        i.putExtra(FixtureDetailActivity.KEY_FIXTURE_ID, (int)id);
        startActivity(i);

    }

    public class ClientCursorAdapter extends ResourceCursorAdapter {

        private int myTeamId;

        public ClientCursorAdapter(Context context, int layout, Cursor c, int flags, int teamId) {
            super(context, layout, c, flags);

            myTeamId = teamId;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView text = (TextView)view.findViewById(R.id.text);

            int homeScore = cursor.getInt(cursor.getColumnIndex("team_home_score"));
            int awayScore = cursor.getInt(cursor.getColumnIndex("team_away_score"));

            String displayText = cursor.getString(cursor.getColumnIndex("teams_home_name")) + " " + cursor.getString(cursor.getColumnIndex("team_home_score"))
                    + " - " + cursor.getString(cursor.getColumnIndex("team_away_score")) + " " + cursor.getString(cursor.getColumnIndex("teams_away_name"));

            text.setText(displayText);

            int homeTeamId = cursor.getInt(cursor.getColumnIndex("teams_home_id"));
            boolean myTeamIsHome = homeTeamId == myTeamId;

            int textColor = R.color.orange;
            if((homeScore > awayScore && myTeamIsHome) || (awayScore > homeScore && !myTeamIsHome)){
                textColor = R.color.win;
            } else if((homeScore < awayScore && myTeamIsHome) || (awayScore < homeScore && !myTeamIsHome)){
                textColor = R.color.lost;
            }

            text.setTextColor(getResources().getColor(textColor));

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_no_games, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_no_games) {

            if(gamesStoreItem == null){

                Toast.makeText(FixtureDetailActivity.this, "We were unable to communicate with the Google Play Store. Please check back soon", Toast.LENGTH_SHORT).show();

            } else {
                if(gamesStoreItem.isPurchased){
                    showNoGamesPicker();
                } else {

                    storeManager.purchaseStoreItem(FixtureDetailActivity.this, gamesStoreItem, new StoreManager.PurchaseResultListener() {
                        @Override
                        public void purchaseSuccess(StoreItem item, Purchase purchase) {
                            showNoGamesPicker();
                        }

                        @Override
                        public void purchaseFailed(StoreItem item, Purchase purchase) {

                        }
                    });

                }
            }

            return true;
        } else if (id == R.id.action_graph) {

            Intent i = new Intent(FixtureDetailActivity.this, FixtureGraphActivity.class);
            i.putExtra(FixtureDetailActivity.KEY_TEAM_HOME_ID, home_team_id);
            i.putExtra(FixtureDetailActivity.KEY_TEAM_AWAY_ID, away_team_id);
            i.putExtra(FixtureGraphActivity.KEY_NO_GAMES, no_games);
            i.putExtra(FixtureGraphActivity.KEY_HOME_AWAY_LEFT, filter_left.ordinal());
            i.putExtra(FixtureGraphActivity.KEY_HOME_AWAY_RIGHT, filter_right.ordinal());
            i.putExtra(FixtureDetailActivity.KEY_LEAGUE_ID, leagueId);
            startActivity(i);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showNoGamesPicker() {
        final String[] items = new String[]{ "3", "5", "10" };

        final AlertDialog.Builder builder = new AlertDialog.Builder(FixtureDetailActivity.this, R.style.YourAlertDialogTheme);
        builder.setTitle("No Games")
                .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        no_games = Integer.parseInt(items[which]);

                        getGames(home_team_id, list_left);
                        getGames(away_team_id, list_right);
                    }
                });
        Dialog d = builder.create();

        d.show();
    }
}
