package uk.co.createanet.footballformapp.fragments.tab_containers.tabs;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import uk.co.createanet.footballformapp.MainActivity;
import uk.co.createanet.footballformapp.R;
import uk.co.createanet.footballformapp.data.FFDatabase;
import uk.co.createanet.footballformapp.lib.CreateaResponseHandler;
import uk.co.createanet.footballformapp.lib.PushMessaging;
import uk.co.createanet.footballformapp.lib.RestClient;
import uk.co.createanet.footballformapp.models.ColumnIdentifier;
import uk.co.createanet.footballformapp.models.GameResult;
import uk.co.createanet.footballformapp.views.FavouriteImageView;
import uk.co.createanet.footballformapp.views.Last5ImageView;

public class SubLeagueDetailsFragment extends ScrollViewFragment {

    private final static String ARG_LOGO_ID = "logo_id";

    private HashMap<String, Integer> colMappings;

    private HashMap<Integer, ArrayList<GameResult>> last5Games = new HashMap<Integer, ArrayList<GameResult>>();
    private LinearLayout.LayoutParams paramsGames = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    private LinearLayout.LayoutParams paramsImageView = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    private int leagueId;
    private boolean isFavourites;
    private String logoID;

    public static SubLeagueDetailsFragment newInstance(int leagueId, String logoID, boolean isFavourites) {
        SubLeagueDetailsFragment fragment = new SubLeagueDetailsFragment();

        Bundle b = new Bundle();
        b.putInt(LeagueDetailsFragment.KEY_LEAGUE_ID, leagueId);
        b.putBoolean(LeagueDetailsFragment.KEY_IS_FAVOURITES, isFavourites);
        b.putString(ARG_LOGO_ID, logoID);

        fragment.setArguments(b);

        return fragment;
    }

    private View.OnClickListener favouriteClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final FavouriteImageView iv = (FavouriteImageView) v;

            iv.isFavourite = !iv.isFavourite;
            iv.refreshImageView();

            ((MainActivity) getActivity()).getDatabase().updateTeamIsFavourite(iv.teamId, iv.leagueId, iv.isFavourite, new FFDatabase.QueryListener() {
                @Override
                public void onQueryComplete(Cursor c) {
                    c.moveToFirst();
                    c.close();

                    registerForPushes(iv.teamId, iv.isFavourite);
                }
            });

            Log.d("FF Click", "Clicked favourite: " + iv.teamId);
        }
    };


    @Override
    public ArrayList<ColumnIdentifier> getColumnIdentifiers() {

        ArrayList<ColumnIdentifier> ids = new ArrayList<ColumnIdentifier>();
        ids.add(new ColumnIdentifier("POS", ColumnIdentifier.ColumnSize.SMALL, "position"));
        ids.add(new ColumnIdentifier("Team", ColumnIdentifier.ColumnSize.LARGE, "team_name"));
        ids.add(new ColumnIdentifier("PLD", ColumnIdentifier.ColumnSize.SMALL, "total_played_amount"));
        ids.add(new ColumnIdentifier("W", ColumnIdentifier.ColumnSize.SMALL, "total_win", true));
        ids.add(new ColumnIdentifier("D", ColumnIdentifier.ColumnSize.SMALL, "total_draw", true));
        ids.add(new ColumnIdentifier("L", ColumnIdentifier.ColumnSize.SMALL, "total_lose", true));
        ids.add(new ColumnIdentifier("F", ColumnIdentifier.ColumnSize.SMALL, "total_goals_for", true));
        ids.add(new ColumnIdentifier("A", ColumnIdentifier.ColumnSize.SMALL, "total_goals_against", true));
        ids.add(new ColumnIdentifier("GD", ColumnIdentifier.ColumnSize.SMALL, "goal_difference", true));
        ids.add(new ColumnIdentifier("PTS", ColumnIdentifier.ColumnSize.SMALL, "points"));
        ids.add(new ColumnIdentifier("Last5", ColumnIdentifier.ColumnSize.LARGE, "last_five"));
        ids.add(new ColumnIdentifier("Favourite", ColumnIdentifier.ColumnSize.MEDIUM, "is_favourite"));

        return ids;
    }

    /*
        Override point for derived columns, for example calculating total games, position etc
     */
    @Override
    public View customiseColumn(View convertView, ColumnIdentifier columnIdentifier, Cursor cursor) {

        if (columnIdentifier.sqlColumn.equals("position")) {
            return createTextViewColumn(String.valueOf(cursor.getPosition() + 1), true);
        } else if (columnIdentifier.sqlColumn.equals("F")) {
            return createTextViewColumn(cursor.getString(cursor.getColumnIndex(columnIdentifier.sqlColumn)).equals("Y") ? "Y" : "N");
        } else if (columnIdentifier.sqlColumn.equals("last_five")) {
            return buildLast5GamesView(cursor.getString(cursor.getColumnIndex("results")));
        } else if (columnIdentifier.sqlColumn.equals("is_favourite")) {
            return buildFavouriteStar(cursor.getInt(cursor.getColumnIndex("team_id")), cursor.getInt(cursor.getColumnIndex("league_id")), cursor.getString(cursor.getColumnIndex(columnIdentifier.sqlColumn)).equals("Y"));
        }

        return null;
    }

    public View buildFavouriteStar(int rowId, int leagueId, boolean isFavourite) {
        FavouriteImageView imageView = new FavouriteImageView(getActivity());
        imageView.teamId = rowId;
        imageView.isFavourite = isFavourite;
        imageView.leagueId = leagueId;
        imageView.setOnClickListener(favouriteClick);
        imageView.refreshImageView();

        return imageView;
    }

    public View buildLast5GamesView(String results) {

        LinearLayout ll = new LinearLayout(getActivity());
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setLayoutParams(paramsGames);
        ll.setId(getResources().getInteger(R.integer.temp_container));

        Resources r = getResources();
        float minWidthPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, r.getDisplayMetrics());

        String[] eachResult = results.split("\\+");
        for (int i = 0, j = 5; i < j; i++) {
            final Last5ImageView v = new Last5ImageView(getActivity());
            v.setLayoutParams(paramsImageView);
            v.setPadding(2, 2, 2, 2);

            if (eachResult.length > i) {
                String result = eachResult[i];

                final String[] matchInfo = result.split("\\*");
                final String item = matchInfo[colMappings.get("result")];

                if (item.equals("WON")) {
                    v.setImageDrawable(getResources().getDrawable(R.drawable.won_circle));
                } else if (item.equals("LOST")) {
                    v.setImageDrawable(getResources().getDrawable(R.drawable.lost_circle));
                } else if (item.equals("DRAW")) {
                    v.setImageDrawable(getResources().getDrawable(R.drawable.draw_circle));
                }

                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.YourAlertDialogTheme);
                        builder
                                .setTitle(matchInfo[colMappings.get("home_name")] + " v " + matchInfo[colMappings.get("away_name")])
                                .setMessage(matchInfo[colMappings.get("home_score")] + " - " + matchInfo[colMappings.get("away_score")])
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                    }
                                });
                        builder.create().show();
                    }
                });
            } else {
                v.setMinimumWidth(Math.round(minWidthPx));
            }

            ll.addView(v);
        }

        return ll;

    }

    @Override
    public void getCursorLocal(final FILTER_TYPE filter_type, final String sortCol, final FFDatabase.QueryListener l) {
        last5Games = new HashMap<Integer, ArrayList<GameResult>>();

        ((MainActivity) getActivity()).getDatabase().getLeagueTable(filter_type, leagueId, isFavourites, logoID, sortCol, new FFDatabase.QueryListener() {
            @Override
            public void onQueryComplete(Cursor c) {
                l.onQueryComplete(c);
                //        build5GamesView(filter_type, sortCol);
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        if (getArguments() != null) {
            leagueId = getArguments().getInt(LeagueDetailsFragment.KEY_LEAGUE_ID);
            isFavourites = getArguments().getBoolean(LeagueDetailsFragment.KEY_IS_FAVOURITES);
            logoID = getArguments().getString(ARG_LOGO_ID);
        }

        colMappings = new HashMap<String, Integer>();
        colMappings.put("result", 0);
        colMappings.put("home_name", 1);
        colMappings.put("away_name", 2);
        colMappings.put("home_score", 3);
        colMappings.put("away_score", 4);

        paramsImageView.weight = 1;
    }

    public void registerForPushes(int teamId, boolean remove) {

        RequestParams params = new RequestParams();
        params.add("deviceToken", PushMessaging.getToken(getActivity()));
        params.add("teamId", String.valueOf(teamId));
        params.add("device_type", "ANDROID");
        params.add("remove_device", !remove ? "Y" : "N");

        RestClient.get(getActivity(), "register_team_for_push_notifications.php", params, new CreateaResponseHandler(getActivity()) {

            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);
            }

        }, false);

    }

    @Override
    public void onDetach() {
        super.onDetach();

        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}