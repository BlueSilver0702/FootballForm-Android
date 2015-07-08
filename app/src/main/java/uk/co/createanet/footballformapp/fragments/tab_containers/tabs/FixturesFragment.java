package uk.co.createanet.footballformapp.fragments.tab_containers.tabs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import uk.co.createanet.footballformapp.MainActivity;
import uk.co.createanet.footballformapp.R;
import uk.co.createanet.footballformapp.data.FFDatabase;
import uk.co.createanet.footballformapp.lib.DateFormatting;

public class FixturesFragment extends Fragment implements AbsListView.OnItemClickListener {

    private ListView listView;
    private OnFixtureChosen mListener;

    private int leagueId;

    private ArrayList<Date> next5Games;
    private Date selectedDate;
    private TextView headerText;

    public static FixturesFragment newInstance(int leagueId) {

        FixturesFragment fragment = new FixturesFragment();

        Bundle b = new Bundle();
        b.putInt(LeagueDetailsFragment.KEY_LEAGUE_ID, leagueId);
        fragment.setArguments(b);

        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FixturesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        if (getArguments() != null) {
            leagueId = getArguments().getInt(LeagueDetailsFragment.KEY_LEAGUE_ID);
        }

        ((MainActivity) getActivity()).getDatabase().getNext5Games(leagueId, new FFDatabase.QueryListener() {
            @Override
            public void onQueryComplete(Cursor c) {

                SimpleDateFormat today = new SimpleDateFormat("yyyy-MM-dd");

                next5Games = new ArrayList<Date>();
                if(c.getCount() > 0) {

                    do {
                        try {
                            next5Games.add(today.parse(c.getString(c.getColumnIndex("fixture_date"))));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    } while (c.moveToNext());
                }

                c.close();

                updateHeaderText();

                getCursor();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fixtues, container, false);

        listView = (ListView) view.findViewById(R.id.list_view);
        listView.setOnItemClickListener(this);

        headerText = (TextView) view.findViewById(R.id.text_title);

        updateHeaderText();

        return view;
    }

    public void updateHeaderText() {
        Date d = getCurrentDate();

        if (d != null && headerText != null) {
            headerText.setText(DateFormatting.dateToDisplayString(d));
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && next5Games != null) {
            getCursor();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (OnFixtureChosen) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }

    }

    public interface OnFixtureChosen {
        public void onFixtureChosen(int leagueId, int fixtureId);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mListener.onFixtureChosen(leagueId, (int) id);
    }

    public class ClientCursorAdapter extends ResourceCursorAdapter {

        public ClientCursorAdapter(Context context, int layout, Cursor c, int flags) {
            super(context, layout, c, flags);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            view.setBackgroundColor(getResources().getColor((cursor.getPosition() % 2 == 0) ? R.color.white : R.color.grey));

            TextView player_left = (TextView) view.findViewById(R.id.player_left);
            TextView player_right = (TextView) view.findViewById(R.id.player_right);
            TextView text_time = (TextView) view.findViewById(R.id.text_time);

            TextView text_score = (TextView)view.findViewById(R.id.text_score);
            ImageView image_vs = (ImageView)view.findViewById(R.id.image_vs);

            player_left.setText(cursor.getString(cursor.getColumnIndex("teams_home_name")));
            player_right.setText(cursor.getString(cursor.getColumnIndex("teams_away_name")));

            Date d = DateFormatting.stringToDate(cursor.getString(cursor.getColumnIndex("fixture_date")) + " " + cursor.getString(cursor.getColumnIndex("start_time")));
            text_time.setText(DateFormatting.dateToDisplayStringTime(d));

            String gameStatus = cursor.getString(cursor.getColumnIndex("status"));

            if(gameStatus.equals("Fin")) {
                image_vs.setVisibility(View.GONE);
                text_score.setVisibility(View.VISIBLE);

                int teamHomeScore = cursor.getInt(cursor.getColumnIndex("team_home_score"));
                int teamAwayScore = cursor.getInt(cursor.getColumnIndex("team_away_score"));

                text_score.setText(teamHomeScore + " - " + teamAwayScore);
            } else {
                image_vs.setVisibility(View.VISIBLE);
                text_score.setVisibility(View.GONE);
            }

        }
    }

    public void getCursor() {
        if(getActivity() == null){
            return;
        }

        ((MainActivity) getActivity()).getDatabase().getFixtures(leagueId, getCurrentDate(), new FFDatabase.QueryListener() {
            @Override
            public void onQueryComplete(Cursor c) {
                if(c != null && getActivity() != null) {
                    ClientCursorAdapter adapter = new ClientCursorAdapter(getActivity(), R.layout.row_fixture, c, 0);
                    listView.setAdapter(adapter);
                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_date, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        final int id = item.getItemId();

        switch (id) {
            case R.id.action_date:
                showDatePicker();
                return true;
        }

        return false;
    }

    public Date getCurrentDate() {
        return selectedDate == null ? getDefaultDate() : selectedDate;
    }

    private Date getDefaultDate(){

        int index = getDefaultDateIndex();

        if(index > -1){
            return next5Games.get(index);
        }

        return new Date();
    }

    private int getDefaultDateIndex(){
        Date todayDate = new Date();

        if(next5Games != null && next5Games.size() > 0) {

            // we want to find the first date in the future
            for(int i = next5Games.size() - 1, j = 0; i >= j; i--){
                Date d = next5Games.get(i);
                if(d.after(todayDate)){
                    return i;
                }
            }

            // we've not found a date after today, but we'll default
            // to the last date a game was played
            return 0;
        }

        return -1;
    }

    public String[] getDateString() {

        String[] dates = new String[next5Games.size()];

        SimpleDateFormat today = new SimpleDateFormat("EE d MMM yyyy");

        int i = 0;
        for (Date d : next5Games) {
            dates[i++] = today.format(d);
        }

        return dates;
    }

    public void showDatePicker() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.YourAlertDialogTheme);
        builder.setTitle("Choose Date")
            .setSingleChoiceItems(getDateString(), getDefaultDateIndex(), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                selectedDate = next5Games.get(item);
                updateHeaderText();

                getCursor();

                dialog.dismiss();
            }
        });
        Dialog d = builder.create();

        d.show();
    }


}
