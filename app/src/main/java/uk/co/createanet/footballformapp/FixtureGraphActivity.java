package uk.co.createanet.footballformapp;

import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

import java.util.Date;
import java.util.HashMap;

import uk.co.createanet.footballformapp.data.FFDatabase;
import uk.co.createanet.footballformapp.fragments.tab_containers.tabs.ScrollViewFragment;

public class FixtureGraphActivity extends AdvertActivity {

    public static final String KEY_NO_GAMES = "no_games";

    public static final String KEY_HOME_AWAY_LEFT = "home_away_left";
    public static final String KEY_HOME_AWAY_RIGHT = "home_away_right";

    private int home_team_id, away_team_id;
    private int num_games;

    private ScrollViewFragment.FILTER_TYPE filter_left;
    private ScrollViewFragment.FILTER_TYPE filter_right;

    private TextView team_1;
    private TextView point_type_left;
    private TextView team_2;
    private TextView point_type_right;
    private TextView no_games;

    private FFDatabase db;
    private int[] leftPoints, rightPoints;

    private LineGraphView graphView;
    private int leagueId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fixture_graph);

        setTitle("Momentum Graph");

        Bundle b = getIntent().getExtras();
        if (b != null) {
            home_team_id = b.getInt(FixtureDetailActivity.KEY_TEAM_HOME_ID);
            away_team_id = b.getInt(FixtureDetailActivity.KEY_TEAM_AWAY_ID);
            leagueId = b.getInt(FixtureDetailActivity.KEY_LEAGUE_ID);

            num_games = b.getInt(KEY_NO_GAMES);

            filter_left = getFilterType(b.getInt(KEY_HOME_AWAY_LEFT));
            filter_right = getFilterType(b.getInt(KEY_HOME_AWAY_RIGHT));
        }

        db = new FFDatabase(this);

        team_1 = (TextView) findViewById(R.id.team_1);
        point_type_left = (TextView) findViewById(R.id.point_type_left);
        team_2 = (TextView) findViewById(R.id.team_2);
        point_type_right = (TextView) findViewById(R.id.point_type_right);
        no_games = (TextView) findViewById(R.id.no_games);

        no_games.setText(String.format("Last %d games", num_games));

        graphView = new LineGraphView(this, "");
        graphView.setDrawDataPoints(true);
        graphView.setDataPointsRadius(20);

        LinearLayout layout = (LinearLayout) findViewById(R.id.graph_container);
        layout.addView(graphView);

        db.getTeamDetails(home_team_id, leagueId, new FFDatabase.QueryListener() {
            @Override
            public void onQueryComplete(Cursor c) {
            }

            @Override
            public void onQueryComplete(HashMap<String, String> map) {
                team_1.setText(map.get("name"));
            }
        });

        db.getTeamDetails(away_team_id, leagueId, new FFDatabase.QueryListener() {
            @Override
            public void onQueryComplete(Cursor c) {
            }

            @Override
            public void onQueryComplete(HashMap<String, String> map) {
                team_2.setText(map.get("name"));
            }
        });

        getPreviousGames(home_team_id, filter_left);
        getPreviousGames(away_team_id, filter_right);

        setFilterType(filter_left, point_type_left);
        setFilterType(filter_right, point_type_right);

    }

    public void setFilterType(ScrollViewFragment.FILTER_TYPE filterType, TextView tv) {
        String text = "All Points";
        if (filterType == ScrollViewFragment.FILTER_TYPE.AWAY) {
            text = "Away Points";
        } else if (filterType == ScrollViewFragment.FILTER_TYPE.HOME) {
            text = "Home Points";
        }

        tv.setText(text);
    }

    private void getPreviousGames(final int teamId, ScrollViewFragment.FILTER_TYPE filterType) {
        db.getPreviousGamesForTeam(leagueId, teamId, new Date(), num_games, filterType, new FFDatabase.QueryListener() {
            @Override
            public void onQueryComplete(Cursor c) {
                int[] totalPoints = new int[c.getCount()];

                if(totalPoints.length > 0) {
                    c.moveToLast();

                    int i = 0;
                    int currentScore = 0;
                    do {
                        currentScore += c.getInt(c.getColumnIndex("points"));
                        totalPoints[i++] = currentScore;
                    } while (c.moveToPrevious());
                }

                if (teamId == home_team_id) {
                    leftPoints = totalPoints;
                } else {
                    rightPoints = totalPoints;
                }

                if(leftPoints != null && rightPoints != null && leftPoints.length == 0 && rightPoints.length == 0){
                    Toast.makeText(FixtureGraphActivity.this, "No results to plot", Toast.LENGTH_SHORT).show();
                    finish();
                }

                rebuildGraph();
            }
        });
    }

    private void rebuildGraph() {
        graphView.removeAllSeries();

        if (leftPoints != null && leftPoints.length > 0) {
            addSeries(leftPoints, "", R.color.orange);
        }

        if (rightPoints != null && rightPoints.length > 0) {
            addSeries(rightPoints, "", R.color.blue_button);
        }

        if (leftPoints != null && rightPoints != null && leftPoints.length > 0 && rightPoints.length > 0) {
            Log.d("Graph", "Done points");

            int[] totalLeft = getTotals(leftPoints);
            int[] totalRight = getTotals(rightPoints);

            graphView.getGraphViewStyle().setVerticalLabelsColor(getResources().getColor(R.color.black));
            graphView.getGraphViewStyle().setHorizontalLabelsColor(getResources().getColor(R.color.black));

            // Feel like I'm having to hack this but that's just how I roll
            int noHorizontalLabels = Math.max(totalLeft[1], totalRight[1]);
            int noVerticalLabels = Math.max(totalLeft[0], totalRight[0]);

            graphView.getGraphViewStyle().setNumHorizontalLabels(noHorizontalLabels);
            graphView.getGraphViewStyle().setNumVerticalLabels(noVerticalLabels);

            graphView.setManualYAxisBounds(noVerticalLabels, 0);

            // graphView.setManualYAxisBounds(noVerticalLabels, 0);

            String[] horizontalLabels = new String[noHorizontalLabels];
            for (int i = 1; i <= noHorizontalLabels; i++) {
                horizontalLabels[i - 1] = "Game " + i;
            }

            String[] verticalLabels = new String[noVerticalLabels + 1];
            for (int i = 0; i <= noVerticalLabels; i++) {
                verticalLabels[noVerticalLabels - i] = String.valueOf(i);
            }

            Resources r = getResources();
            float fontSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, r.getDisplayMetrics());
            float labelWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, r.getDisplayMetrics());

            graphView.getGraphViewStyle().setTextSize(fontSize);
            graphView.getGraphViewStyle().setVerticalLabelsWidth((int)labelWidth);

            graphView.setHorizontalLabels(horizontalLabels);
            graphView.setVerticalLabels(verticalLabels);

        }
    }

    private void addSeries(int[] dataPoints, String title, int color) {
        GraphView.GraphViewData[] data = new GraphView.GraphViewData[dataPoints.length];
        for (int i = 0, j = dataPoints.length; i < j; i++) {
            data[i] = new GraphView.GraphViewData(i, dataPoints[i]);
        }

        GraphViewSeries.GraphViewSeriesStyle ss = new GraphViewSeries.GraphViewSeriesStyle();
        ss.color = getResources().getColor(color);
        ss.thickness = 10;

        GraphViewSeries exampleSeries = new GraphViewSeries(title, ss, data);
        graphView.addSeries(exampleSeries);

    }

    private int[] getTotals(int[] dataPoints) {
        int totalPoints = dataPoints == null ? 0 : dataPoints[dataPoints.length - 1];
        int noPoints = dataPoints == null ? 0 : dataPoints.length;

        /*
        totalPoints = 0;
        if(dataPoints != null) {
            for (int i = 0, j = dataPoints.length; i < j; i++) {
                totalPoints += dataPoints[i];
            }
        }
        */

        return new int[]{totalPoints, noPoints};
    }

    private static ScrollViewFragment.FILTER_TYPE getFilterType(int id) {
        if (id == ScrollViewFragment.FILTER_TYPE.HOME.ordinal()) {
            return ScrollViewFragment.FILTER_TYPE.HOME;
        } else if (id == ScrollViewFragment.FILTER_TYPE.AWAY.ordinal()) {
            return ScrollViewFragment.FILTER_TYPE.AWAY;
        }

        return ScrollViewFragment.FILTER_TYPE.ALL;
    }

}
