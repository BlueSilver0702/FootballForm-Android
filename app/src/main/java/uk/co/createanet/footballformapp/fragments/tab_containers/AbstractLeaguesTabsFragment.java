package uk.co.createanet.footballformapp.fragments.tab_containers;

import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.util.ArrayList;

import uk.co.createanet.footballformapp.MainActivity;
import uk.co.createanet.footballformapp.data.FFDatabase;
import uk.co.createanet.footballformapp.fragments.AbstractTabbedFragment;
import uk.co.createanet.footballformapp.fragments.tab_containers.tabs.LeagueDetailsFragment;
import uk.co.createanet.footballformapp.fragments.tab_containers.tabs.LeaguesTabFragment;
import uk.co.createanet.footballformapp.models.TabItem;

/**
 * Created by matt on 03/07/2014.
 */
public abstract class AbstractLeaguesTabsFragment extends AbstractTabbedFragment {

    public int type;
    protected boolean isFavourites;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() != null){
            isFavourites = getArguments().getBoolean(LeagueDetailsFragment.KEY_IS_FAVOURITES);
            type = getArguments().getInt(LeaguesTabFragment.KEY_TYPE);
        }

        getTabs();
    }

    protected void getTabs(){

        // not yet attached
        if(getActivity() == null) return;

        Log.d("TABS", "COUNTRY ID: " + String.valueOf(((MainActivity) getActivity()).getCountyId()));

        FFDatabase.QueryListener ql = new FFDatabase.QueryListener() {
            @Override
            public void onQueryComplete(Cursor c) {

                final ArrayList<TabItem> items = new ArrayList<TabItem>();

                try {
                    do {
                        items.add(new TabItem(c.getInt(0), c.getString(1), getNewFragment(c.getInt(0))));
                    } while (c.moveToNext());

                } catch (CursorIndexOutOfBoundsException e){

                } finally {
                    c.close();
                }

                setUpTabs(items);

            }
        };

        if(type == LeaguesTabFragment.TYPE_FORM_PLAYERS || type == LeaguesTabFragment.TYPE_PLAYER_VS) {
            ((MainActivity) getActivity()).getDatabase().getLeaguesWithData(((MainActivity) getActivity()).getCountyId(), ql);
        } else {
            ((MainActivity) getActivity()).getDatabase().getLeagues(((MainActivity) getActivity()).getCountyId(), ql);
        }
    }

    public abstract Fragment getNewFragment(int id);

}
