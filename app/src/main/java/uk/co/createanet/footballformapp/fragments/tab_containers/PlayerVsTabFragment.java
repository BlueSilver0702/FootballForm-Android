package uk.co.createanet.footballformapp.fragments.tab_containers;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;

import uk.co.createanet.footballformapp.MainActivity;
import uk.co.createanet.footballformapp.PlayerVsTeamsActivity;
import uk.co.createanet.footballformapp.R;
import uk.co.createanet.footballformapp.data.FFDatabase;
import uk.co.createanet.footballformapp.fragments.tab_containers.tabs.LeagueDetailsFragment;
import uk.co.createanet.footballformapp.lib.SelectListView;

/**
 * Created by matt on 03/07/2014.
 */
public class PlayerVsTabFragment extends Fragment {

    private SelectListView selectListViewLeft;
    private SelectListView selectListViewRight;

    private int leagueId;

    private EditText text_search_left, text_search_right;

    public static PlayerVsTabFragment newInstance(int leagueId) {

        PlayerVsTabFragment fragment = new PlayerVsTabFragment();

        Bundle b = new Bundle();
        b.putInt(LeagueDetailsFragment.KEY_LEAGUE_ID, leagueId);

        fragment.setArguments(b);

        return fragment;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        setHasOptionsMenu(true);

        Bundle b = getArguments();
        if (b != null) {
            leagueId = b.getInt(LeagueDetailsFragment.KEY_LEAGUE_ID);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playervs, container, false);

        ListView list1 = (ListView) view.findViewById(R.id.list_1);
        ListView list2 = (ListView) view.findViewById(R.id.list_2);

        selectListViewLeft = new SelectListView(list1);
        selectListViewRight = new SelectListView(list2);

        ((MainActivity) getActivity()).getDatabase().getTeamsInLeague(leagueId, null, new FFDatabase.QueryListener() {
            @Override
            public void onQueryComplete(Cursor c) {
                if(getActivity() != null) {
                    selectListViewLeft.setCursor(getActivity(), c);
                }
            }
        });

        ((MainActivity) getActivity()).getDatabase().getTeamsInLeague(leagueId, null, new FFDatabase.QueryListener() {
            @Override
            public void onQueryComplete(Cursor c) {
                if(getActivity() != null) {
                    selectListViewRight.setCursor(getActivity(), c);
                }
            }
        });

        text_search_left = (EditText) view.findViewById(R.id.text_search_left);
        text_search_right = (EditText) view.findViewById(R.id.text_search_right);

        text_search_left.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                ((MainActivity) getActivity()).getDatabase().getTeamsInLeague(leagueId, v.getText().toString(), new FFDatabase.QueryListener() {
                    @Override
                    public void onQueryComplete(Cursor c) {
                        selectListViewLeft.setCursor(getActivity(), c);
                    }
                });

                return true;
            }
        });

        text_search_right.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                ((MainActivity) getActivity()).getDatabase().getTeamsInLeague(leagueId, v.getText().toString(), new FFDatabase.QueryListener() {
                    @Override
                    public void onQueryComplete(Cursor c) {
                        selectListViewRight.setCursor(getActivity(), c);
                    }
                });

                return true;
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_next, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        final int id = item.getItemId();

        switch (id) {
            case R.id.action_next:
                if (selectListViewLeft.selectedId == -1 || selectListViewRight.selectedId == -1) {
                    Toast.makeText(getActivity(), "Please select 2 teams", Toast.LENGTH_SHORT).show();
                } else {
                    Intent i = new Intent(getActivity(), PlayerVsTeamsActivity.class);
                    i.putExtra(PlayerVsTeamsActivity.KEY_TEAM_1, selectListViewLeft.selectedId);
                    i.putExtra(PlayerVsTeamsActivity.KEY_LEAGUE_ID_1, selectListViewLeft.selectedLeague);
                    i.putExtra(PlayerVsTeamsActivity.KEY_TEAM_2, selectListViewRight.selectedId);
                    i.putExtra(PlayerVsTeamsActivity.KEY_LEAGUE_ID_2, selectListViewLeft.selectedLeague);
                    startActivity(i);
                }

                return true;
        }

        return false;
    }

    @Override
    public void onPause(){
        super.onPause();

        selectListViewLeft.onDestroy();
        selectListViewRight.onDestroy();
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
