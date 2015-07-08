package uk.co.createanet.footballformapp.fragments.tab_containers.tabs;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import uk.co.createanet.footballformapp.MainActivity;
import uk.co.createanet.footballformapp.R;
import uk.co.createanet.footballformapp.data.FFDatabase;
import uk.co.createanet.footballformapp.models.SubTab;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 */
public class LeagueDetailsFragment extends Fragment {

    public static final int[] SUB_FILTER_CATEGORIES = new int[] { 35477 };

    public static final String KEY_LEAGUE_ID = "league_id";
    public static final String KEY_IS_FAVOURITES = "is_favs";

    public ArrayList<SubTab> subTabs;

    public ViewPager sub_pager;
    public PagerTabStrip pager_title_strip;

    private int leagueId;
    private boolean isFavourites;

    private boolean shouldHaveSubTabs = false;
    private SampleFragmentPagerAdapter sampleFragmentPagerAdapter;

    public static LeagueDetailsFragment newInstance(int leagueId, boolean isFavourites) {
        LeagueDetailsFragment fragment = new LeagueDetailsFragment();

        Bundle b = new Bundle();
        b.putInt(KEY_LEAGUE_ID, leagueId);
        b.putBoolean(KEY_IS_FAVOURITES, isFavourites);

        fragment.setArguments(b);

        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public LeagueDetailsFragment() {
    }

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        setHasOptionsMenu(true);

        if (getArguments() != null) {
            leagueId = getArguments().getInt(KEY_LEAGUE_ID);
            isFavourites = getArguments().getBoolean(KEY_IS_FAVOURITES);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        View v = super.onCreateView(inflater, container, savedInstanceState);

        View v = inflater.inflate(R.layout.fragment_sub_scroll, container, false);

        sampleFragmentPagerAdapter = new SampleFragmentPagerAdapter();

        if(isSubTabedCountry(((MainActivity)getActivity()).getCountyId())){
            shouldHaveSubTabs = true;

            // get the subtabs, refresh the adapter
            ((MainActivity)getActivity()).getDatabase().getLeagueGroups(leagueId, new FFDatabase.QueryListener() {
                @Override
                public void onQueryComplete(Cursor c) {

                    subTabs = new ArrayList<SubTab>();
                    if(c.getCount() > 0) {
                        do {
                            subTabs.add(new SubTab(1, c.getString(1)));
                        } while (c.moveToNext());
                    }

                    sampleFragmentPagerAdapter.notifyDataSetChanged();
                }
            });

        } else {
            shouldHaveSubTabs = false;
        }

        sub_pager = (ViewPager) v.findViewById(R.id.sub_pager);
        pager_title_strip = (PagerTabStrip) v.findViewById(R.id.pager_title_strip);

        sub_pager.setAdapter(sampleFragmentPagerAdapter);

        if(!shouldHaveSubTabs){
            pager_title_strip.setVisibility(View.GONE);
        }

        return v;
    }

    private boolean isSubTabedCountry(int countryId){
        for(int i = 0, j = SUB_FILTER_CATEGORIES.length; i < j; i++){
            if(countryId == SUB_FILTER_CATEGORIES[i]){
                return true;
            }
        }

        return false;
    }

    /*
    public void updateLLWithResults(LinearLayout ll, int rowId) {
        ll.removeAllViews();

        Log.d("IMAGE", "Updating row with images: " + rowId);

        ArrayList<GameResult> games = last5Games.get(rowId);

        for (final GameResult result : games) {
            final Last5ImageView v = new Last5ImageView(getActivity());
            v.setLayoutParams(paramsImageView);

            final String item = result.game_result;

            if (item.equals("WON")) {
                v.setImageDrawable(getResources().getDrawable(R.drawable.won_circle));
            } else if (item.equals("LOST")) {
                v.setImageDrawable(getResources().getDrawable(R.drawable.lost_circle));
            } else if (item.equals("DRAW")) {
                v.setImageDrawable(getResources().getDrawable(R.drawable.draw_circle));
            }

            v.setPadding(2, 2, 2, 2);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder
                            .setTitle(result.teams_home_name + " v " + result.teams_away_name)
                            .setMessage(result.team_home_score + " - " + result.team_away_score)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    builder.create().show();
                }
            });


            ll.addView(v);

        }

        ll.invalidate();

    }
    */

    public class SampleFragmentPagerAdapter extends FragmentPagerAdapter {

        HashMap<Integer, SubLeagueDetailsFragment> fragments = new HashMap<Integer, SubLeagueDetailsFragment>();

        public SampleFragmentPagerAdapter() {
            super(getChildFragmentManager());
        }

        @Override
        public int getCount() {
            return shouldHaveSubTabs ? (subTabs == null ? 0 : subTabs.size()) : 1;
        }

        @Override
        public SubLeagueDetailsFragment getItem(int position) {
            if(!fragments.containsKey(position)){
                fragments.put(position, SubLeagueDetailsFragment.newInstance(leagueId, subTabs == null ? null : subTabs.get(position).tabName, isFavourites));
            }

            return fragments.get(position);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return shouldHaveSubTabs ? subTabs.get(position).tabName : "";
        }
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

    public SubLeagueDetailsFragment getCurrentItem(){
        return sampleFragmentPagerAdapter.getItem(sub_pager.getCurrentItem());
    }

}
