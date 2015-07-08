package uk.co.createanet.footballformapp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import uk.co.createanet.footballformapp.data.FFDatabase;
import uk.co.createanet.footballformapp.lib.SelectListView;

public class PlayerVsTeamsActivity extends AdvertActivity {

    public static final String KEY_TEAM_1 = "team_1";
    public static final String KEY_TEAM_2 = "team_2";

    public static final String KEY_LEAGUE_ID_1 = "league_id_1";
    public static final String KEY_LEAGUE_ID_2 = "league_id_2";

    private SelectListView selectListViewLeft;
    private SelectListView selectListViewRight;

    private EditText text_search_left, text_search_right;

    private int teamId1, teamId2;
    private int leagueId1, leagueId2;
    private FFDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_playervs);

        restore(getIntent().getExtras());
        restore(savedInstanceState);

        setTitle("Choose 2 Players");

        text_search_left = (EditText) findViewById(R.id.text_search_left);
        text_search_right = (EditText) findViewById(R.id.text_search_right);

        db = new FFDatabase(PlayerVsTeamsActivity.this);

        text_search_left.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                db.getPlayersInTeam(leagueId1, teamId1, v.getText().toString(), new FFDatabase.QueryListener() {
                    @Override
                    public void onQueryComplete(Cursor c) {
                        selectListViewLeft.setCursor(PlayerVsTeamsActivity.this, c);
                    }
                });

                return true;
            }
        });

        text_search_right.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                db.getPlayersInTeam(leagueId2, teamId2, v.getText().toString(), new FFDatabase.QueryListener() {
                    @Override
                    public void onQueryComplete(Cursor c) {
                        selectListViewRight.setCursor(PlayerVsTeamsActivity.this, c);
                    }
                });

                return true;
            }
        });

    }

    public void restore(Bundle b) {
        if (b == null) return;

        if (b.containsKey(KEY_TEAM_1)) {
            teamId1 = (int) b.getLong(KEY_TEAM_1);
        }
        if (b.containsKey(KEY_TEAM_2)) {
            teamId2 = (int) b.getLong(KEY_TEAM_2);
        }
        if (b.containsKey(KEY_LEAGUE_ID_1)) {
            leagueId1 = b.getInt(KEY_LEAGUE_ID_1);
        }
        if (b.containsKey(KEY_LEAGUE_ID_1)) {
            leagueId2 = b.getInt(KEY_LEAGUE_ID_1);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_next, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_next) {

            if (selectListViewLeft.selectedId == -1 || selectListViewRight.selectedId == -1) {
                Toast.makeText(PlayerVsTeamsActivity.this, "Please select 2 players", Toast.LENGTH_SHORT).show();
                return true;
            }

            Intent i = new Intent(PlayerVsTeamsActivity.this, PlayerVsDetailActivity.class);
            i.putExtra(PlayerVsTeamsActivity.KEY_TEAM_1, (int) selectListViewLeft.selectedId);
            i.putExtra(PlayerVsTeamsActivity.KEY_TEAM_2, (int) selectListViewRight.selectedId);
            i.putExtra(PlayerVsTeamsActivity.KEY_LEAGUE_ID_1, selectListViewLeft.selectedLeague);
            i.putExtra(PlayerVsTeamsActivity.KEY_LEAGUE_ID_2, selectListViewRight.selectedLeague);
            startActivity(i);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        ListView list1 = (ListView) findViewById(R.id.list_1);
        ListView list2 = (ListView) findViewById(R.id.list_2);

        selectListViewLeft = new SelectListView(list1);
        selectListViewRight = new SelectListView(list2);

        db.getPlayersInTeam(leagueId1, teamId1, text_search_left.getText().toString(), new FFDatabase.QueryListener() {
            @Override
            public void onQueryComplete(Cursor c) {
                selectListViewLeft.setCursor(PlayerVsTeamsActivity.this, c);
            }
        });

        db.getPlayersInTeam(leagueId2, teamId2, text_search_left.getText().toString(), new FFDatabase.QueryListener() {
            @Override
            public void onQueryComplete(Cursor c) {
                selectListViewRight.setCursor(PlayerVsTeamsActivity.this, c);
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();

        selectListViewLeft.onDestroy();
        selectListViewRight.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle b) {
        b.putInt(KEY_TEAM_1, teamId1);
        b.putInt(KEY_TEAM_2, teamId2);

        b.putInt(KEY_LEAGUE_ID_1, leagueId1);
        b.putInt(KEY_LEAGUE_ID_2, leagueId2);

        super.onSaveInstanceState(b);
    }
}
