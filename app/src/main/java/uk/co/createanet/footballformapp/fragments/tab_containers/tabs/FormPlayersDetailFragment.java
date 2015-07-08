package uk.co.createanet.footballformapp.fragments.tab_containers.tabs;

import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

import uk.co.createanet.footballformapp.MainActivity;
import uk.co.createanet.footballformapp.data.FFDatabase;
import uk.co.createanet.footballformapp.models.ColumnIdentifier;

/**
 * Created by matt on 03/07/2014.
 */
public class FormPlayersDetailFragment extends ScrollViewFragment {

    private int leagueId;
    private DecimalFormat df = new DecimalFormat("0.000");

    public static FormPlayersDetailFragment newInstance(int leagueId){

        FormPlayersDetailFragment fragment = new FormPlayersDetailFragment();

        Bundle b = new Bundle();
        b.putInt(LeagueDetailsFragment.KEY_LEAGUE_ID, leagueId);
        fragment.setArguments(b);


        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        if(getArguments() != null){
            leagueId = getArguments().getInt(LeagueDetailsFragment.KEY_LEAGUE_ID);
        }

    }

    @Override
    public void getCursorLocal(ScrollViewFragment.FILTER_TYPE filter_type, String sortOrder, FFDatabase.QueryListener l) {
        ((MainActivity)getActivity()).getDatabase().getFormPlayers(leagueId, 3, sortOrder, filter_type, l);
    }

    @Override
    public ArrayList<ColumnIdentifier> getColumnIdentifiers() {
        ArrayList<ColumnIdentifier> ids = new ArrayList<ColumnIdentifier>();
        ids.add(new ColumnIdentifier("P", ColumnIdentifier.ColumnSize.SMALL, "position"));
        ids.add(new ColumnIdentifier("Player", ColumnIdentifier.ColumnSize.LARGE, "player_name"));
        ids.add(new ColumnIdentifier("P", ColumnIdentifier.ColumnSize.SMALL, "appearances", true));
        ids.add(new ColumnIdentifier("G", ColumnIdentifier.ColumnSize.SMALL, "p1_goals", true));
        ids.add(new ColumnIdentifier("G", ColumnIdentifier.ColumnSize.SMALL, "p2_goals", true));
        ids.add(new ColumnIdentifier("T", ColumnIdentifier.ColumnSize.SMALL, "total_goals", true));
        ids.add(new ColumnIdentifier("T/P", ColumnIdentifier.ColumnSize.MEDIUM, "tp"));

        return ids;
    }

    @Override
    public View customiseColumn(View convertView, ColumnIdentifier columnIdentifier, Cursor cursor) {
        if(columnIdentifier.sqlColumn.equals("position")) {
            return createTextViewColumn(String.valueOf(cursor.getPosition() + 1), true);
        } else if(columnIdentifier.sqlColumn.equals("player_name")) {

            LinearLayout ll = new LinearLayout(getActivity());

            TextView tv = (TextView)createTextViewColumn(cursor.getString(cursor.getColumnIndex("player_name")));
            tv.setTypeface(null, Typeface.BOLD);

            ll.addView(tv);

            ll.addView(createTextViewColumn(cursor.getString(cursor.getColumnIndex("team_name"))));
            ll.setOrientation(LinearLayout.VERTICAL);

            return ll;

        } else if(columnIdentifier.sqlColumn.equals("tp")) {
            return createTextViewColumn(df.format(cursor.getDouble(cursor.getColumnIndex("tp"))));
        }

        /* else if(columnIdentifier.sqlColumn.equals("tp")) {
            double totalGoals = cursor.getInt(cursor.getColumnIndex("total_goals"));
            return createTextViewColumn(df.format((totalGoals / cursor.getInt(cursor.getColumnIndex("appearances")))));
        }*/

        return null;
    }



}
