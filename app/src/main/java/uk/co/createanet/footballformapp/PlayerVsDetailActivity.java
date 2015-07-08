package uk.co.createanet.footballformapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

import uk.co.createanet.footballformapp.data.FFDatabase;
import uk.co.createanet.footballformapp.models.StoreItem;
import uk.co.createanet.footballformapp.models.StoreManager;
import uk.co.createanet.footballformapp.util.Purchase;
import uk.co.createanet.footballformapp.views.HorizontalBarGraph;

public class PlayerVsDetailActivity extends AdvertActivity {

    private int playerId1, playerId2;
    private int leagueId1, leagueId2;

    private TextView team1TextView, team2TextView;

    private HorizontalBarGraph scoredGraph, averageScoreGraph, homeGoalsGrah, awayGoalsGraph,
                                yellowCardsGraph, redCardsGraph, averageCardTimeGraph,
                                totalScoredGraph, averageScoreTimeGraph, totalYellowCardsGraph,
                                totalRedCardsGraph, totalAverageCardTimeGraph;

    private FFDatabase db;
    private StoreManager storeManager;
    private StoreItem gamesStoreItem;

    private int noGames = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_vs_detail);

        setTitle("Player VS");

        Bundle b = getIntent().getExtras();
        if (b != null) {
            playerId1 = b.getInt(PlayerVsTeamsActivity.KEY_TEAM_1);
            playerId2 = b.getInt(PlayerVsTeamsActivity.KEY_TEAM_2);

            leagueId1 = b.getInt(PlayerVsTeamsActivity.KEY_LEAGUE_ID_1);
            leagueId2 = b.getInt(PlayerVsTeamsActivity.KEY_LEAGUE_ID_2);
        }

        team1TextView = (TextView) findViewById(R.id.team_1);
        team2TextView = (TextView) findViewById(R.id.team_2);

        storeManager = new StoreManager(PlayerVsDetailActivity.this);
        storeManager.initialiseInAppPurchase(PlayerVsDetailActivity.this, new StoreManager.PurchaseStoreReady() {
            @Override
            public void storeReady() {

                storeManager.checkPurchasedItems(new StoreManager.PurchaseUpdateListener() {
                    @Override
                    public void updateSuccess() {
                        gamesStoreItem = StoreManager.getById(PlayerVsDetailActivity.this, StoreManager.ID_GAMES);
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

        buildView();
    }

    private void buildView() {

        LinearLayout main_container = (LinearLayout) findViewById(R.id.main_container);
        main_container.removeAllViews();

        TextView season = new TextView(PlayerVsDetailActivity.this);
        season.setText("Last " + noGames + " Games");
        season.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        season.setTypeface(null, Typeface.BOLD);
        season.setGravity(Gravity.CENTER_HORIZONTAL);
        main_container.addView(season);

        scoredGraph = new HorizontalBarGraph(PlayerVsDetailActivity.this, "Scored", 0, 0);
        averageScoreGraph = new HorizontalBarGraph(PlayerVsDetailActivity.this, "Average Score Time", 0, 0, R.color.white, true);
        homeGoalsGrah = new HorizontalBarGraph(PlayerVsDetailActivity.this, "Home Goals", 0, 0, R.color.grey);
        awayGoalsGraph = new HorizontalBarGraph(PlayerVsDetailActivity.this, "Away Goals", 0, 0, R.color.grey);
        yellowCardsGraph = new HorizontalBarGraph(PlayerVsDetailActivity.this, "Yellow Cards", 0, 0);
        redCardsGraph = new HorizontalBarGraph(PlayerVsDetailActivity.this, "Red Cards", 0, 0);
        averageCardTimeGraph = new HorizontalBarGraph(PlayerVsDetailActivity.this, "Average Card Time", 0, 0, R.color.white, true);

        main_container.addView(scoredGraph);
        main_container.addView(averageScoreGraph);
        main_container.addView(homeGoalsGrah);
        main_container.addView(awayGoalsGraph);
        main_container.addView(yellowCardsGraph);
        main_container.addView(redCardsGraph);
        main_container.addView(averageCardTimeGraph);

        season = new TextView(PlayerVsDetailActivity.this);
        season.setText("Entire Season");
        season.setGravity(Gravity.CENTER_HORIZONTAL);
        season.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        season.setTypeface(null, Typeface.BOLD);

        main_container.addView(season);

        totalScoredGraph = new HorizontalBarGraph(PlayerVsDetailActivity.this, "Total Scored", 0, 0, R.color.grey);
        averageScoreTimeGraph = new HorizontalBarGraph(PlayerVsDetailActivity.this, "Average Score Time", 0, 0, R.color.grey, true);
        totalYellowCardsGraph = new HorizontalBarGraph(PlayerVsDetailActivity.this, "Total Yellow Cards", 0, 0);
        totalRedCardsGraph = new HorizontalBarGraph(PlayerVsDetailActivity.this, "Total Red Cards", 0, 0);
        totalAverageCardTimeGraph = new HorizontalBarGraph(PlayerVsDetailActivity.this, "Average Card Time", 0, 0, R.color.white, true);

        main_container.addView(totalScoredGraph);
        main_container.addView(averageScoreTimeGraph);
        main_container.addView(totalYellowCardsGraph);
        main_container.addView(totalRedCardsGraph);
        main_container.addView(totalAverageCardTimeGraph);

        Thread t = new Thread(){
            @Override
            public void run() {

                // get the last x games this player played in
                final Cursor getPlayer1LastGames = db.getPlayerLastGames(leagueId1, playerId1, noGames);
                final Cursor getPlayer2LastGames = db.getPlayerLastGames(leagueId2, playerId2, noGames);

                Integer[] ids1 = new Integer[getPlayer1LastGames.getCount()];
                int i = 0;
                while(getPlayer1LastGames.moveToNext()){
                    ids1[i++] = getPlayer1LastGames.getInt(0);
                }

                Integer[] ids2 = new Integer[getPlayer2LastGames.getCount()];
                i = 0;
                while(getPlayer2LastGames.moveToNext()){
                    ids2[i++] = getPlayer2LastGames.getInt(0);
                }

                final Cursor getPlayer1LastGamesTotal = db.getPlayerLastGames(leagueId1, playerId1, 0);
                final Cursor getPlayer2LastGamesTotal = db.getPlayerLastGames(leagueId2, playerId2, 0);

                Integer[] ids1Total = new Integer[getPlayer1LastGamesTotal.getCount()];
                i = 0;
                while(getPlayer1LastGamesTotal.moveToNext()){
                    ids1Total[i++] = getPlayer1LastGamesTotal.getInt(0);
                }

                Integer[] ids2Total = new Integer[getPlayer2LastGamesTotal.getCount()];
                i = 0;
                while(getPlayer2LastGamesTotal.moveToNext()){
                    ids2Total[i++] = getPlayer2LastGamesTotal.getInt(0);
                }

                // get values for last x games graphs
                final HashMap<String, String> player1Details = db.getPlayerDetails(playerId1);
                final HashMap<String, String> getPlayer1AvgTimeScoredIn = db.getPlayerAvgTimeScoredIn(playerId1, ids1);
                final HashMap<String, String> getPlayer1CardsYellow = db.getPlayerCards(playerId1, "Yellow", ids1);
                final HashMap<String, String> getPlayer1CardsRed = db.getPlayerCards(playerId1, "Red", ids1);
                final HashMap<String, String> getPlayer1TimeGotCard = db.getPlayerTimeGotCard(playerId1, ids1);
                final HashMap<String, String> getPlayer1Scored = db.getPlayerScored(playerId1, ids1);

                final HashMap<String, String> player2Details = db.getPlayerDetails(playerId2);
                final HashMap<String, String> getPlayer2AvgTimeScoredIn = db.getPlayerAvgTimeScoredIn(playerId2, ids2);
                final HashMap<String, String> getPlayer2CardsYellow = db.getPlayerCards(playerId2, "Yellow", ids2);
                final HashMap<String, String> getPlayer2CardsRed = db.getPlayerCards(playerId2, "Red", ids2);
                final HashMap<String, String> getPlayer2TimeGotCard = db.getPlayerTimeGotCard(playerId2, ids2);
                final HashMap<String, String> getPlayer2Scored = db.getPlayerScored(playerId2, ids2);


                // get values for total games graph
                final HashMap<String, String> totalPlayer1AvgTimeScoredIn = db.getPlayerAvgTimeScoredIn(playerId1, ids1Total);
                final HashMap<String, String> totalPlayer1CardsYellow = db.getPlayerCards(playerId1, "Yellow", ids1Total);
                final HashMap<String, String> totalPlayer1CardsRed = db.getPlayerCards(playerId1, "Red", ids1Total);
                final HashMap<String, String> totalPlayer1TimeGotCard = db.getPlayerTimeGotCard(playerId1, ids1Total);
                final HashMap<String, String> totalPlayer1Scored = db.getPlayerScored(playerId1, ids1Total);

                final HashMap<String, String> totalPlayer2AvgTimeScoredIn = db.getPlayerAvgTimeScoredIn(playerId2, ids2Total);
                final HashMap<String, String> totalPlayer2CardsYellow = db.getPlayerCards(playerId2, "Yellow", ids2Total);
                final HashMap<String, String> totalPlayer2CardsRed = db.getPlayerCards(playerId2, "Red", ids2Total);
                final HashMap<String, String> totalPlayer2TimeGotCard = db.getPlayerTimeGotCard(playerId2, ids2Total);
                final HashMap<String, String> totalPlayer2Scored = db.getPlayerScored(playerId2, ids2Total);

                PlayerVsDetailActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        team1TextView.setText(player1Details.get("name") + "\n(" + player1Details.get("team_name") + ")");
                        team2TextView.setText(player2Details.get("name") + "\n(" + player2Details.get("team_name") + ")");

                        setGraphValues(averageScoreGraph, "time_scored_in", getPlayer1AvgTimeScoredIn, getPlayer2AvgTimeScoredIn);
                        setGraphValues(yellowCardsGraph, "cards", getPlayer1CardsYellow, getPlayer2CardsYellow);
                        setGraphValues(redCardsGraph, "cards", getPlayer1CardsRed, getPlayer2CardsRed);
                        setGraphValues(averageCardTimeGraph, "time_player_got_card", getPlayer1TimeGotCard, getPlayer2TimeGotCard);
                        setGraphValues(scoredGraph, "scored", getPlayer1Scored, getPlayer2Scored);

                        setGraphValues(totalScoredGraph, "time_scored_in", totalPlayer1AvgTimeScoredIn, totalPlayer2AvgTimeScoredIn);
                        setGraphValues(totalYellowCardsGraph, "cards", totalPlayer1CardsYellow, totalPlayer2CardsYellow);
                        setGraphValues(totalRedCardsGraph, "cards", totalPlayer1CardsRed, totalPlayer2CardsRed);
                        setGraphValues(totalAverageCardTimeGraph, "time_player_got_card", totalPlayer1TimeGotCard, totalPlayer2TimeGotCard);
                        setGraphValues(totalScoredGraph, "scored", totalPlayer1Scored, totalPlayer2Scored);

                    }
                });
            }
        };
        t.start();

    }

    private void setGraphValues(HorizontalBarGraph bg, String valueKey, HashMap<String, String> valuesLeft, HashMap<String, String> valuesRight){
        if(valuesLeft.size() == 1 && valuesRight.size() == 1){
            int leftValue = 0;
            int rightValue = 0;

            if(valuesLeft.get(valueKey) != null){
                leftValue = (int) Math.round(Double.parseDouble(valuesLeft.get(valueKey)));
            }

            if(valuesRight.get(valueKey) != null) {
                rightValue = (int) Math.round(Double.parseDouble(valuesRight.get(valueKey)));
            }

            bg.setValues(leftValue, rightValue);
        } else {
            bg.setValues(0, 0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_games, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_no_games) {

            if(gamesStoreItem == null){

                Toast.makeText(PlayerVsDetailActivity.this, "We were unable to communicate with the Google Play Store. Please check back soon", Toast.LENGTH_SHORT).show();

            } else {
                if(gamesStoreItem.isPurchased){
                    showNoGamesPicker();
                } else {

                    storeManager.purchaseStoreItem(PlayerVsDetailActivity.this, gamesStoreItem, new StoreManager.PurchaseResultListener() {
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
        }

        return super.onOptionsItemSelected(item);
    }

    public void showNoGamesPicker() {
        final String[] items = new String[]{ "3", "5", "10" };

        final AlertDialog.Builder builder = new AlertDialog.Builder(PlayerVsDetailActivity.this, R.style.YourAlertDialogTheme);
        builder.setTitle("No Games")
                .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        noGames = Integer.parseInt(items[which]);
                        buildView();
                    }
                });
        Dialog d = builder.create();

        d.show();
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        finish();
    }
}
