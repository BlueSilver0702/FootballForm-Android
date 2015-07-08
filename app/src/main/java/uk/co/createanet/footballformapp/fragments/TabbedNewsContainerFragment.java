package uk.co.createanet.footballformapp.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.util.ArrayList;

import uk.co.createanet.footballformapp.MainActivity;
import uk.co.createanet.footballformapp.data.FFDatabase;
import uk.co.createanet.footballformapp.models.TabItem;

public class TabbedNewsContainerFragment extends AbstractTabbedFragment {


    public static TabbedNewsContainerFragment newInstance() {
        TabbedNewsContainerFragment fragment = new TabbedNewsContainerFragment();
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TabbedNewsContainerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((MainActivity)getActivity()).getDatabase().getNewsRSSLinks(new FFDatabase.QueryListener() {
            @Override
            public void onQueryComplete(Cursor c) {

                final ArrayList<TabItem> items = new ArrayList<TabItem>();

                do {
                    items.add(new TabItem(c.getInt(0), c.getString(1), getNewFragment(c.getString(2))));
                } while (c.moveToNext());

                c.close();

                setUpTabs(items);

            }
        });
    }

    public Fragment getNewFragment(String url){
        return NewsFragment.newInstance(url);
    }

}
