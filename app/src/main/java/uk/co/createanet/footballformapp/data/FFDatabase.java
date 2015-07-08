package uk.co.createanet.footballformapp.data;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.io.File;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import uk.co.createanet.footballformapp.R;
import uk.co.createanet.footballformapp.fragments.tab_containers.tabs.ScrollViewFragment;
import uk.co.createanet.footballformapp.lib.DataManager;
import uk.co.createanet.footballformapp.models.GameResult;

/**
 * Created by matt on 03/07/2014.
 */
public class FFDatabase extends SQLiteAssetHelper {

    private static final int DATABASE_VERSION = 1;
    private Activity context;

    private Dialog pDialog;

    public static enum GameOrder {
        WIN, DREW, LOST, FOR, AGAINST, GOAL_DIFFERENCE, POSITION
    }

    private final String[] tables = new String[]{
            "countries", "fixtures", "goals_scored", "league", "line_up_and_history_id",
            "line_ups", "players", "players_deleted", "rss_feed_links", "seasons", "sub_league",
            "team_positions", "teams", "yellow_cards"
    };

    public FFDatabase(Activity context) {
        super(context, (new File(DataManager.getDatabasePath(context))).getName(), (new File(DataManager.getDatabasePath(context))).getParentFile().getAbsolutePath(), null, DATABASE_VERSION);

        this.context = context;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
        for (String table : tables) {
            db.execSQL("DROP TABLE IF EXISTS " + table);
        }

        onCreate(db);
    }

    public static abstract class QueryListener {
        public abstract void onQueryComplete(Cursor c);

        public void onQueryComplete(HashMap<String, String> map) {
        }
    }

    public Cursor runSQL(String sql) {
        return runSQL(sql, new String[]{});
    }

    public HashMap<String, String> runSQLSingle(String sql, String[] params) {

        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(sql, params);

        c.moveToNext();

        HashMap<String, String> resultSet = new HashMap<String, String>();
        if (c.getCount() > 0) {
            for (int i = 0, j = c.getColumnCount(); i < j; i++) {
                resultSet.put(c.getColumnName(i), c.getString(i));
            }
        }

        return resultSet;
    }

    public void runSQLAsync(final String sql, final QueryListener listener) {
        runSQLAsync(sql, new String[]{}, listener);
    }

    public void runSQLAsync(final String sql, final String[] params, final QueryListener listener) {
        runSQLAsync(sql, params, listener, false);
    }

    public void runSQLAsync(final String sql, final String[] params, final QueryListener listener, boolean singleRow) {
        runSQLAsync(sql, params, listener, singleRow, !singleRow);
    }

    public void runSQLAsync(final String sql, final String[] params, final QueryListener listener, final boolean singleRow, final boolean showLoader) {
        if (listener == null) {
            throw new InvalidParameterException("QueryListener must not be null");
        }

        if (showLoader) {
            if (pDialog == null) {
                pDialog = new ProgressDialog(context);
            }

            pDialog.show();
            pDialog.setContentView(R.layout.dialog_loading);
        }

        Log.d("FFQuery", sql);
        Log.d("FFQuery", "Params: " + TextUtils.join(", ", params));

        Thread t = new Thread() {
            @Override
            public void run() {
                final Cursor c = runSQL(sql, params);

                final HashMap<String, String> resultSet = new HashMap<String, String>();
                if (singleRow) {
                    if (c.getCount() > 0) {
                        c.moveToFirst();

                        for (int i = 0, j = c.getColumnCount(); i < j; i++) {
                            resultSet.put(c.getColumnName(i), c.getString(i));
                        }
                    }
                } else {
                    if (c.getCount() > 0) {
                        c.moveToFirst();
                    }
                }

                context.runOnUiThread(new Runnable() {
                    public void run() {
                        if (pDialog != null && pDialog.isShowing()) {
                            try {
                                pDialog.dismiss();
                            } catch(IllegalArgumentException e) {
                                // ignore
                            }
                        }

                        if (listener != null) {
                            if (singleRow) {
                                listener.onQueryComplete(resultSet);
                            } else {
                                listener.onQueryComplete(c);
                            }
                        }
                    }
                });
            }
        };
        t.start();
    }

    public Cursor runSQL(String sql, String[] params) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(sql, params);

//        c.moveToFirst();

        return c;
    }

    public void getContinents(QueryListener l) {
        String query = "SELECT _id, continent " +
                "FROM countries " +
                "WHERE continent IS NOT NULL " +
                "AND should_show_on_app='Y' " +
                "GROUP BY continent " +
                "ORDER BY continent ASC";

        runSQLAsync(query, l);
    }


    public String getContinent(int countryId, String key) {
        String query = "SELECT _id, name, continent FROM countries WHERE _id = ?";

        HashMap<String, String> res = runSQLSingle(query, new String[]{String.valueOf(countryId)});
        return res.get(key);
    }

    public void getCountries(String continentName, QueryListener l) {
        String query = "SELECT * FROM countries " +
                "WHERE should_show_on_app='Y' " +
                "AND continent=? " +
                "ORDER BY name ASC";

        runSQLAsync(query, new String[]{continentName}, l);
    }

    public void getLeagues(int countryId, QueryListener l) {
        String query = "SELECT _id, name " +
                "FROM league " +
                "WHERE country_id = ? " +
                "AND should_show_on_app = 'Y' " +
                "ORDER BY `sort` ASC";

        runSQLAsync(query, new String[]{String.valueOf(countryId)}, l);
    }

    public void getLeagueGroups(int leagueId, QueryListener l) {
        String query = "SELECT _id, logoID from team_positions WHERE league_id IN (" +
                "SELECT _id " +
                "FROM sub_league " +
                "WHERE league_id= :leagueId " +
                "AND description='PHASE HAS TABLE AND IS LEAF' " +
                "LIMIT 1) " +
                "GROUP BY logoID " +
                "ORDER BY logoID ASC";

        runSQLAsync(query, new String[]{String.valueOf(leagueId)}, l);
    }

    public void getLeaguesWithData(int countryId, QueryListener l) {
        String query = "SELECT l._id, l.name" +
                "       FROM league l, form_players_view fpv" +
                "       LEFT OUTER JOIN sub_league sl ON (sl._id=fpv.league_id)" +
                "       WHERE (l._id=fpv.league_id OR sl.league_id=l._id)" +
                "       AND l.country_id = ?" +
                "       AND l.should_show_on_app='Y'" +
                "       AND (fpv.p1_goals > 0 OR fpv.p2_goals > 0)" +
                "       GROUP BY l._id" +
                "       ORDER BY l.sort ASC";

        runSQLAsync(query, new String[]{String.valueOf(countryId)}, l);
    }

    public void updateTeamIsFavourite(int teamId, int leagueId, boolean isFavourite, QueryListener l) {
        String query = "UPDATE teams" +
                " SET is_favourite = ?" +
                " WHERE _id = ? AND league_id = ?";

        runSQLAsync(query, new String[]{(isFavourite ? "Y" : "N"), String.valueOf(teamId), String.valueOf(leagueId)}, l);
    }

    public void getLeagueTable(ScrollViewFragment.FILTER_TYPE filter_type, int leagueId, boolean favourites, String orderBy, QueryListener l) {
        getLeagueTable(filter_type, leagueId, favourites, orderBy, l);
    }

    public void getLeagueTable(ScrollViewFragment.FILTER_TYPE filter_type, int leagueId, boolean favourites, String logoId, String orderBy, QueryListener l) {

        if (orderBy == null) {
            orderBy = "points DESC";
        }

        // default secondary order of team name
        orderBy += ", tp.team_name ASC";

        String filterFavourites = "";
        if (favourites) {
            filterFavourites = " AND t.is_favourite = 'Y' ";
        }

        String logoIDFilter = "";
        if(logoId != null){
            logoIDFilter = " AND tp.logoID = '" + logoId + "' ";
        }

        HashMap<String, String> sortKeys = new HashMap<String, String>();
        if (filter_type == ScrollViewFragment.FILTER_TYPE.HOME) {

            sortKeys.put("total_win", "home_win");
            sortKeys.put("total_draw", "home_draw");
            sortKeys.put("total_lose", "home_lose");

            sortKeys.put("total_goals_for", "home_total_goals_for");
            sortKeys.put("total_goals_against", "home_total_goals_against");
            sortKeys.put("points", "team_home_points");

        } else if (filter_type == ScrollViewFragment.FILTER_TYPE.AWAY) {

            sortKeys.put("total_win", "away_win");
            sortKeys.put("total_draw", "away_draw");
            sortKeys.put("total_lose", "away_lose");

            sortKeys.put("total_goals_for", "away_total_goals_for");
            sortKeys.put("total_goals_against", "away_total_goals_against");
            sortKeys.put("points", "team_away_points");

        } else {

            sortKeys.put("total_win", "total_win");
            sortKeys.put("total_draw", "total_draw");
            sortKeys.put("total_lose", "total_lose");

            sortKeys.put("total_goals_for", "total_goals_for");
            sortKeys.put("total_goals_against", "total_goals_against");
            sortKeys.put("points", "points");

        }

        ArrayList<String> selectCols = new ArrayList<String>();

        Iterator it = sortKeys.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            selectCols.add("tp." + pairs.getValue() + " as " + pairs.getKey());
        }

        selectCols.add("(tp." + sortKeys.get("total_win") + " + tp." + sortKeys.get("total_draw") + " + tp." + sortKeys.get("total_lose") + ") as total_played_amount");
        selectCols.add("(tp." + sortKeys.get("total_goals_for") + " - tp." + sortKeys.get("total_goals_against") + ") as goal_difference");

        String selectColsOut = TextUtils.join(", ", selectCols);

        String query = "SELECT tp._id, tp.league_id, tp.teams_id, tp.team_name, tp.position, tp.team_home_points, tp.team_away_points," +
                "           " + selectColsOut + ", tp.home_win, tp.home_draw, tp.home_lose, tp.home_total_goals_for, tp.home_total_goals_against, " +
                "           tp.away_win, tp.away_draw, tp.away_lose, tp.away_total_goals_for, tp.away_total_goals_against, tp.date_updated, " +
                "           tp.logoID," +
                "           t._id as team_id, t.is_favourite, t.logoID, lg.results " +
                "FROM last_5_games lg, team_positions tp, teams t " +
                "LEFT OUTER JOIN sub_league AS sl ON (tp.league_id = sl._id) " +
                "WHERE (tp.league_id = ? OR sl.league_id = ?) " +
                "AND t._id = tp.teams_id " + filterFavourites +
                "AND t.league_id = tp.league_id " +
                "AND lg.team_id = t._id " +
                "AND lg.league_id = t.league_id " +
                logoIDFilter +
                "GROUP BY tp._id " +
                "ORDER BY " + orderBy;

        runSQLAsync(query, new String[]{String.valueOf(leagueId), String.valueOf(leagueId)}, l);
    }

    public void getFavouriteIds(QueryListener l) {

        String query = "SELECT _id, league_id " +
                "FROM teams t " +
                "WHERE is_favourite = 'Y'";

        runSQLAsync(query, l);
    }

    public ArrayList<GameResult> getLast5Games(int teamId, int leagueId) {
        SimpleDateFormat today = new SimpleDateFormat("yyyy-MM-dd");

        String query = "SELECT (" +
                "            CASE WHEN (`team_home_score` > team_away_score AND teams_home_id = t2._id) OR (team_away_score > team_home_score AND teams_away_id = t2._id) " +
                "           THEN 'WON' " +
                "           WHEN (`team_home_score` < team_away_score AND teams_home_id = t2._id) OR (team_away_score < team_home_score AND teams_away_id = t2._id) " +
                "           THEN 'LOST' " +
                "           WHEN (`team_home_score` = team_away_score AND teams_home_id = t2._id) OR (team_away_score = team_home_score AND teams_away_id = t2._id) " +
                "           THEN 'DRAW' " +
                "           END) as game_result, " +
                "           teams_home_name, teams_away_name, team_away_score, team_home_score" +
                "       FROM teams t2, fixtures f " +
                "       LEFT OUTER JOIN sub_league sl " +
                "       ON sl._id = f.league_id" +
                "       WHERE (sl._id = ? OR f.league_id = ? OR sl.league_id = ?)" +
                "       AND f.status='Fin' " +
                "       AND (f.teams_home_id = t2._id OR f.teams_away_id = t2._id) " +
                "       AND f.fixture_date < '" + today.format(new Date()) + " 00:00:00' " +
                "       AND t2._id = ? " +
                "       GROUP BY f._id " +
                "       ORDER BY f.fixture_date DESC " +
                "       LIMIT 5";

        long startTime = System.nanoTime();

        Cursor c = runSQL(query, new String[]{String.valueOf(leagueId), String.valueOf(leagueId), String.valueOf(leagueId), String.valueOf(teamId)});

        ArrayList<GameResult> results = new ArrayList<GameResult>();
        while (c.moveToNext()) {
            GameResult result = new GameResult();
            result.game_result = c.getString(0);
            result.teams_home_name = c.getString(1);
            result.teams_away_name = c.getString(2);
            result.team_away_score = c.getInt(3);
            result.team_home_score = c.getInt(4);

            results.add(result);
        }

        c.close();

        long endTime = System.nanoTime();

        long duration = (endTime - startTime) / 1000000;

        Log.d("TIMER", "Took " + duration);

        return results;
    }


    public void getNewsRSSLinks(QueryListener l) {
        String query = "SELECT * FROM rss_feed_links ORDER BY _id ASC";
        runSQLAsync(query, l);
    }

    public void getPlayersInTeam(int leagueId, int teamId, String filterName, QueryListener l) {
        /*
        String query = "SELECT p._id, p.name AS playername," +
                " t._id AS teamid, t.name AS teamname, COUNT(gs._id) AS goals_scored" +
                " FROM league l, teams t, players p" +
                " LEFT OUTER JOIN goals_scored gs ON (gs.player_id=p._id)" +
                " WHERE l.should_show_on_app='Y'" +
                " AND t.league_id = l._id " +
                " AND p.team_id = ? " +
                " AND t._id= p.team_id" +
                " AND p.name LIKE ? " +
                " GROUP BY p._id" +
                " ORDER BY goals_scored DESC";
        */

        String query = "SELECT player_id as _id, player_name, league_id, player_id, team_id, team_name," +
                "           (p1_goals+p2_goals) AS goals_scored" +
                "       FROM form_players_view" +
                "       WHERE team_id = ?" +
                "       AND league_id = ?" +
                "       AND type = 0" +
                "       AND player_name LIKE ?" +
                "       GROUP BY player_id" +
                "       ORDER BY goals_scored DESC";

        filterName = "%" + filterName + "%";

        runSQLAsync(query, new String[]{String.valueOf(teamId), String.valueOf(leagueId), filterName}, l);

    }

    public void getTeamsInLeague(int leagueId, String search, QueryListener l) {

        String filterName = (search == null) ? "%%" : "%" + search + "%";

        /*
        String query = "SELECT t._id, t.name" +
                "       FROM team_positions tp, teams t" +
                "       LEFT OUTER JOIN sub_league sl ON (sl._id = t.league_id)" +
                "       WHERE (sl._id = ? OR t.league_id = ? OR sl.league_id = ?) " +
                "       AND tp.teams_id = t._id" +
                "       AND t.name LIKE ? " +
                "       GROUP BY t._id";
        */

        String query = "SELECT fpv.team_id as _id, fpv.team_name, fpv.league_id" +
                "           FROM form_players_view fpv" +
                "           LEFT OUTER JOIN sub_league sl" +
                "           ON (sl._id=fpv.league_id)" +
                "           WHERE (sl._id= ? OR fpv.league_id= ? OR sl.league_id= ?)" +
                "           AND fpv.team_name LIKE ?" +
                "           GROUP BY fpv.team_id" +
                "           ORDER BY fpv.team_name ASC";

        runSQLAsync(query, new String[]{String.valueOf(leagueId), String.valueOf(leagueId), String.valueOf(leagueId), filterName}, l);
    }

    public void getTeamDetails(int teamId, int leagueId, QueryListener l) {
        String query = "SELECT t._id, t.name, tp.position FROM teams t, team_positions tp WHERE tp.teams_id = t._id AND t._id = ? AND tp.league_id = ?";

        runSQLAsync(query, new String[]{String.valueOf(teamId), String.valueOf(leagueId)}, l, true);
    }

    public void getFixtureDetails(int fixtureId, QueryListener l) {
        String query = "SELECT f._id, f.teams_home_id, f.teams_away_id FROM fixtures f WHERE f._id = ?";

        runSQLAsync(query, new String[]{String.valueOf(fixtureId)}, l, true);
    }

    public String buildInStatement(String col, Integer[] items) {
        if (items == null) {
            return "";
        }

        return " AND " + col + " IN ( " + TextUtils.join(", ", new ArrayList<Integer>(Arrays.asList(items))) + " )";
    }

    /*
        Player details section
     */
    public HashMap<String, String> getPlayerDetails(int playerId) {

        String query = "SELECT p._id, p.name, t.name as team_name" +
                "           FROM players p, teams t" +
                "           WHERE p._id = ?" +
                "           AND t._id=p.team_id" +
                "           GROUP BY p._id";

        return runSQLSingle(query, new String[]{String.valueOf(playerId)});
    }

    public HashMap<String, String> getPlayerScored(int playerId, Integer[] lastGames) {

        String games = buildInStatement("f._id", lastGames);

        String query = "SELECT Count(gs._id) as scored" +
                "       FROM   goals_scored gs" +
                "       INNER JOIN fixtures AS f ON (f._id = gs.match_id " + games + ")" +
                "       WHERE gs.player_id = ?";

        return runSQLSingle(query, new String[]{String.valueOf(playerId)});
    }

    public HashMap<String, String> getPlayerAvgTimeScoredIn(int playerId, Integer[] lastGames) {
        String games = buildInStatement("match_id", lastGames);

        String query = "SELECT AVG(time_scored_in) as time_scored_in" +
                "           FROM goals_scored" +
                "           WHERE player_id = ?" +
                "           " + games;

        return runSQLSingle(query, new String[]{String.valueOf(playerId)});
    }

    public HashMap<String, String> getPlayerCards(int playerId, String type, Integer[] lastGames) {
        String games = buildInStatement("match_id", lastGames);

        String query = "SELECT COUNT(*) as cards" +
                "           FROM yellow_cards" +
                "           WHERE player_id = ?" +
                "           AND (type = ? OR type = ?)" +
                "           " + games;

        return runSQLSingle(query, new String[]{String.valueOf(playerId), type, "Yellow/Red"});
    }

    public HashMap<String, String> getPlayerTimeGotCard(int playerId, Integer[] lastGames) {
        String games = buildInStatement("match_id", lastGames);

        String query = "SELECT AVG(time_player_got_card) as time_player_got_card" +
                "           FROM yellow_cards" +
                "           WHERE player_id= ?" +
                "           " + games;

        return runSQLSingle(query, new String[]{String.valueOf(playerId)});
    }

    public Cursor getPlayerLastGames(int leagueId, int playerId, int limit) {

        // every game is league
        if (limit == 0) {
            limit = 10000;
        }

        String query = "SELECT DISTINCT(f._id) as _id" +
                "       FROM teams t2, line_ups lu, fixtures f" +
                "       WHERE f.status='Fin' " +
                "       AND lu.player_id = ? AND lu.match_id = f._id" +
                "       AND (f.teams_home_id = t2._id OR f.teams_away_id = t2._id) " +
                "       AND f.fixture_date < date('now')" +
                "       AND f.league_id = ?" +
                "       GROUP BY f._id " +
                "       ORDER BY f.fixture_date DESC " +
                "       LIMIT ?";

        return runSQL(query, new String[]{String.valueOf(playerId), String.valueOf(leagueId), String.valueOf(limit)});

    }

    // TODO: looks like no games limit isn't used?
    public void getFormPlayers(int leagueId, int noGamesLimit, String sortOrder, ScrollViewFragment.FILTER_TYPE side, QueryListener l) {

        String sideFilter = "AND fpv.type = 0";
        if (side == ScrollViewFragment.FILTER_TYPE.AWAY) {
            sideFilter = "AND fpv.type = 2";
        } else if (side == ScrollViewFragment.FILTER_TYPE.HOME) {
            sideFilter = "AND fpv.type = 1";
        }

        if (sortOrder == null || sortOrder.length() == 0) {
            sortOrder = "total_goals DESC, tp DESC";
        }

        sortOrder += ", fpv.player_name ASC";

        String query = "SELECT 1 as _id, fpv.player_id, fpv.player_name, fpv.team_name, fpv.team_id, " +
                "           fpv.p1_goals, fpv.p2_goals, fpv.appearances, fpv.type, (p1_goals + p2_goals) as total_goals, " +
                "           (CAST((fpv.p1_goals + fpv.p2_goals) as REAL) / fpv.appearances) as tp" +
                "       FROM form_players_view fpv" +
                "       LEFT OUTER JOIN sub_league as sl ON (sl._id=fpv.league_id)" +
                "       WHERE (sl._id = ? OR fpv.league_id = ? OR sl.league_id = ?)" +
                "       " + sideFilter +
                "       AND (fpv.p1_goals > 0 OR fpv.p2_goals > 0)" +
                "       GROUP BY fpv.player_id" +
                "       ORDER BY " + sortOrder + ", tp DESC";

        runSQLAsync(query, new String[]{String.valueOf(leagueId), String.valueOf(leagueId), String.valueOf(leagueId)}, l);

    }

    // This is redundant
    // If 0 no subleague else has subleague
    public int getSubLeagueId(int leagueId) {
        String query = "SELECT has_subleague" +
                "           FROM league" +
                "           WHERE _id = ?";

        HashMap<String, String> hasSubLeague = runSQLSingle(query, new String[]{String.valueOf(leagueId)});

        Log.d("FF", "Has subleague: " + hasSubLeague.get("has_subleague"));


        if (hasSubLeague.containsKey("has_subleague") && hasSubLeague.get("has_subleague").equals("1")) {

            String innerQuery = "SELECT _id FROM sub_league WHERE league_id = ?";
            HashMap<String, String> subLeague = runSQLSingle(innerQuery, new String[]{String.valueOf(leagueId)});

            if (subLeague.containsKey("_id")) {
                Log.d("FormPlayer", "Using subleague: " + subLeague.get("_id"));

                return Integer.parseInt(subLeague.get("_id"));
            }

        }

        return 0;
    }

    public void getFixtures(int leagueId, Date date, QueryListener l) {

        SimpleDateFormat today = new SimpleDateFormat("yyyy-MM-dd");

        String query = "SELECT f._id, f.teams_home_id, f.teams_home_name, f.team_home_standing, f.teams_away_id," +
                "               f.teams_away_name, f.league_id, f.league_type, f.fixture_date, f.start_time, f.notes, " +
                "               f.team_home_score, f.team_away_score, f.status" +
                "       FROM fixtures f" +
                "       LEFT OUTER JOIN sub_league sl ON sl._id=f.league_id" +
                "       WHERE (fixture_date = '%@ 00:00:00' OR fixture_date = ?)" +
                "       AND (sl.league_id = ? OR f.league_id = ?)" +
                "       ORDER BY start_time";

        runSQLAsync(query, new String[]{today.format(date), String.valueOf(leagueId), String.valueOf(leagueId)}, l);

    }

    public void getNext5Games(int leagueId, QueryListener l) {

        /* Gets 5 games before and 5 games after
        String query = "SELECT * FROM (" +
                "           SELECT " +
                "           FROM fixtures f" +
                "           LEFT OUTER JOIN sub_league sl ON sl._id=f.league_id" +
                "           WHERE (sl.league_id = ? OR f.league_id = ?)" +
                "           AND fixture_date < date('now')" +
                "           GROUP BY fixture_date" +
                "           ORDER BY fixture_date DESC" +
                "           LIMIT 5" +
                ") UNION " +
                "      SELECT * FROM (" +
                "           SELECT f._id, fixture_date" +
                "           FROM fixtures f" +
                "           LEFT OUTER JOIN sub_league sl ON sl._id=f.league_id" +
                "           WHERE (sl.league_id = ? OR f.league_id = ?)" +
                "           AND fixture_date >= date('now')" +
                "           GROUP BY fixture_date" +
                "           ORDER BY fixture_date ASC" +
                "           LIMIT 5" +
                ")" +
                " ORDER BY fixture_date ASC";
        */

        String query = "SELECT f._id, fixture_date" +
                "           FROM fixtures f " +
                "           LEFT OUTER JOIN sub_league sl ON sl._id=f.league_id" +
                "           WHERE (sl.league_id = ? OR f.league_id = ?)" +
                "           GROUP BY fixture_date " +
                "       ORDER BY fixture_date DESC";

        runSQLAsync(query, new String[]{String.valueOf(leagueId), String.valueOf(leagueId)}, l);

    }

    public void getPreviousGamesForTeam(int leagueId, int teamId, Date date, int noGames, ScrollViewFragment.FILTER_TYPE filterType, QueryListener l) {
        SimpleDateFormat today = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String filter;
        if (filterType == ScrollViewFragment.FILTER_TYPE.AWAY) {
            filter = "f.teams_away_id = " + teamId;
        } else if (filterType == ScrollViewFragment.FILTER_TYPE.HOME) {
            filter = "f.teams_home_id = " + teamId;
        } else {
            filter = "(f.teams_home_id = " + teamId + " OR f.teams_away_id = " + teamId + ")";
        }

        String query = "SELECT f._id, f.teams_home_name, f.teams_away_name, f.fixture_date," +
                "           f.team_away_score, f.team_home_score, f.start_time, f.teams_home_id," +
                "           f.teams_away_id, f.status, " +
                "       (" +
                "           CASE" +
                "               WHEN (teams_home_id = " + teamId + " AND team_home_score > team_away_score) THEN 3" +
                "               WHEN (teams_away_id = " + teamId + " AND team_away_score > team_home_score) THEN 3" +
                "               WHEN (team_away_score = team_home_score) THEN 1" +
                "               ELSE 0" +
                "           END" +
                "       ) as points" +
                "       FROM fixtures f" +
                "       WHERE " + filter +
                "       AND fixture_date < ?" +
                "       ORDER BY fixture_date DESC" +
                "       LIMIT ?";

        runSQLAsync(query, new String[]{
                today.format(date), String.valueOf(noGames)
        }, l);

    }

    public void getPointsForTeam(int teamId, Date date, int noGames, ScrollViewFragment.FILTER_TYPE filterType, QueryListener l) {
        SimpleDateFormat today = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String filter;
        if (filterType == ScrollViewFragment.FILTER_TYPE.AWAY) {
            filter = "teams_away_id = " + teamId;
        } else if (filterType == ScrollViewFragment.FILTER_TYPE.HOME) {
            filter = "teams_home_id = " + teamId;
        } else {
            filter = "(teams_home_id = " + teamId + " OR teams_away_id = " + teamId + ")";
        }

        String query = "SELECT 1 as _id, " +
                "       SUM(" +
                "           CASE" +
                "               WHEN (teams_home_id = " + teamId + " AND team_home_score > team_away_score) THEN 3" +
                "               WHEN (teams_away_id = " + teamId + " AND team_away_score > team_home_score) THEN 3" +
                "               WHEN (team_away_score = team_home_score) THEN 1" +
                "               ELSE 0" +
                "           END" +
                "       ) as total_points, " +
                "       SUM(" +
                "           CASE" +
                "             WHEN (teams_home_id = " + teamId + ") THEN team_home_score ELSE team_away_score" +
                "           END" +
                "       ) as total_goals" +
                "       FROM ( SELECT * FROM fixtures" +
                "       WHERE " + filter +
                "       AND fixture_date < ?" +
                "       ORDER BY fixture_date DESC" +
                "       LIMIT ?) as f";

        runSQLAsync(query, new String[]{
                today.format(date), String.valueOf(noGames)
        }, l, true);

    }

    public HashMap<String, String> getFixtureDetailsFull(int fixtureId) {
        String query = "SELECT _id, teams_home_id, teams_away_id, fixture_date, league_id," +
                "           team_home_score, team_away_score, status, notes, teams_home_name," +
                "           teams_away_name," +
                "           coalesce((SELECT position FROM team_positions WHERE teams_id = teams_home_id), 0) as home_position, " +
                "           coalesce((SELECT position FROM team_positions WHERE teams_id = teams_away_id), 0) as away_position, " +
                "           (SELECT name FROM league l WHERE l._id = league_id) as league_name " +
                "       FROM fixtures" +
                "       WHERE _id = ?";

        return runSQLSingle(query, new String[]{
                String.valueOf(fixtureId)
        });

    }

    public Cursor getGoalsScored(int fixtureId) {
        String query = "SELECT _id, player_name, time_scored_in, team_side" +
                "       FROM goals_scored" +
                "       WHERE match_id= ?" +
                "       ORDER BY team_side";

        return runSQL(query, new String[]{
                String.valueOf(fixtureId)
        });

    }

    public Cursor getYellowCards(int fixtureId) {
        String query = "SELECT _id, player_name, time_player_got_card, recieved_at" +
                "           FROM yellow_cards" +
                "           WHERE match_id= ?" +
                "           AND (type='Yellow' OR type='Yellow/Red')";

        return runSQL(query, new String[]{
                String.valueOf(fixtureId)
        });
    }

    public Cursor getRedCards(int fixtureId) {
        String query = "SELECT _id, player_name, time_player_got_card, recieved_at" +
                "           FROM yellow_cards" +
                "           WHERE match_id= ?" +
                "           AND (type='Red' OR type='Yellow/Red')";

        return runSQL(query, new String[]{
                String.valueOf(fixtureId)
        });
    }

    public Cursor getLineups(int fixtureId) {
        String query = "SELECT p._id AS playerID, p.name AS playerName, l.team_id AS teamid" +
                "           FROM line_ups l, players p" +
                "           WHERE match_id = ?" +
                "           AND p._id=l.player_id" +
                "           GROUP BY p.name" +
                "           ORDER BY teamid ASC";

        return runSQL(query, new String[]{
                String.valueOf(fixtureId)
        });
    }

}