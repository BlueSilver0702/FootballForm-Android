package uk.co.createanet.footballformapp.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;

import uk.co.createanet.footballformapp.MainActivity;
import uk.co.createanet.footballformapp.NavigationDrawerFragment;
import uk.co.createanet.footballformapp.R;
import uk.co.createanet.footballformapp.fragments.tab_containers.tabs.LeaguesTabFragment;
import uk.co.createanet.footballformapp.lib.CountrySelector;
import uk.co.createanet.footballformapp.models.NavigationDrawerItem;
import uk.co.createanet.footballformapp.models.StoreItem;
import uk.co.createanet.footballformapp.models.StoreManager;
import uk.co.createanet.footballformapp.util.Purchase;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public class DrawerListFragment extends Fragment {

    public static final String SHARE_PACKAGE = "uk.co.createanet.football-form";
    public static final String SHARE_SUBJECT = "Football Form App";
    public static final String SHARE_TEXT = "Download Football Form app for FREE on iPhone, iPad and Android. http://play.google.com/store/apps/details?id=" + SHARE_PACKAGE;

    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";


    public ArrayList<NavigationDrawerItem> items;

    private TextView text_continent;

    private NavigationDrawerFragment.NavigationDrawerCallbacks mCallbacks;

    private static int mCurrentSelectedPosition = 2;

    private ListView mDrawerListView;
    public DrawerLayout mDrawerLayout;
    private View mFragmentContainerView;
    private NavigationDrawerAdapter mDrawerAdapter;
    private NavigationDrawerItem upgradeDrawItem;

    public static DrawerListFragment newInstance(){
        return new DrawerListFragment();
    }

    public DrawerListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle b){
        super.onCreate(b);

//        setRetainInstance(true);

        if(items == null) {
            items = new ArrayList<NavigationDrawerItem>();
            items.add(new NavigationDrawerItem("Select Country", null, R.drawable.menu_select_country));
            items.add(new NavigationDrawerItem("Live Scores", LiveScoresFragment.newInstance(), R.drawable.menu_live_scores));
            items.add(new NavigationDrawerItem("Leagues", LeaguesTabFragment.newInstance(false, LeaguesTabFragment.TYPE_LEAGUE), R.drawable.menu_leagues));
            items.add(new NavigationDrawerItem("Fixtures", LeaguesTabFragment.newInstance(false, LeaguesTabFragment.FIXTURES), R.drawable.menu_fixtures));
            items.add(new NavigationDrawerItem("Player Vs", LeaguesTabFragment.newInstance(false, LeaguesTabFragment.TYPE_PLAYER_VS), R.drawable.menu_player_vs));
            items.add(new NavigationDrawerItem("Form Players", LeaguesTabFragment.newInstance(false, LeaguesTabFragment.TYPE_FORM_PLAYERS), R.drawable.menu_form_players));
//            items.add(new NavigationDrawerItem("Favourites", LeaguesTabFragment.newInstance(true, LeaguesTabFragment.TYPE_LEAGUE), R.drawable.menu_favourites));
            items.add(new NavigationDrawerItem("News", TabbedNewsContainerFragment.newInstance(), R.drawable.menu_news));
//            items.add(new NavigationDrawerItem("Betting", null, R.drawable.icon_betting));
            items.add(new NavigationDrawerItem("Share", null, R.drawable.menu_share));
            items.add(new NavigationDrawerItem("Rate On App Store", null, R.drawable.menu_favourites));

            upgradeDrawItem = new NavigationDrawerItem("Upgrade To Premium", null, R.drawable.menu_favourites);

            if (((MainActivity) getActivity()).gamesStoreItem != null && !((MainActivity) getActivity()).gamesStoreItem.isPurchased) {
                items.add(upgradeDrawItem);
            }

            if (b != null) {
                mCurrentSelectedPosition = b.getInt(STATE_SELECTED_POSITION);

            } else {
                mDrawerLayout = ((MainActivity) getActivity()).mNavigationDrawerFragment.mDrawerLayout;
                mFragmentContainerView = ((MainActivity) getActivity()).mNavigationDrawerFragment.mFragmentContainerView;

                mDrawerLayout.closeDrawer(mFragmentContainerView);
//            mCurrentSelectedPosition = 1;
            }

            mCallbacks.onNavigationDrawerItemSelected(items.get(mCurrentSelectedPosition));

            if(b != null){
                // set the positions of our child tab bar
                items.get(mCurrentSelectedPosition).restoreState(b);
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_drawer_list, container, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
        }

        text_continent = (TextView)v.findViewById(R.id.league_name);
        text_continent.setText(CountrySelector.getLastCountryName(getActivity(), ((MainActivity)getActivity()).getDatabase()));

        mDrawerListView = (ListView)v.findViewById(R.id.fragment_drawer_list);
        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // overriding some actions
                if (position == 0) {
                    getFragmentManager().beginTransaction().replace(R.id.outer_container, ChooseCountryFragment.newInstanceChooseContinent()).addToBackStack("continent").commit();
                    return;

                } else if (position == 7) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_SUBJECT, SHARE_SUBJECT);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, SHARE_TEXT);
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);

                    return;
                } else if (position == 8) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("market://details?id=" + SHARE_PACKAGE));
                    startActivity(intent);

                    return;
                }
                /* else if (position == 8){
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("http://affiliatehub.skybet.com/processing/clickthrgh.asp?btag=a_15777b_1"));
                    startActivity(intent);

                    return;
                }
                */ else if (position == 9) {

                    ((MainActivity)getActivity()).purchaseUpgrade(new StoreManager.PurchaseResultListener() {
                        @Override
                        public void purchaseSuccess(StoreItem item, Purchase purchase) {
                            // show them a message
                            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.YourAlertDialogTheme);
                            builder.setTitle("Purchase Complete").setMessage("You've now upgraded to Football Form Premium!").create().show();

                            onPurchaseCompleted();
                        }

                        @Override
                        public void purchaseFailed(StoreItem item, Purchase purchase) {
                            Log.d("FF", "Purchase cancelled");
                        }
                    });

                    return;
                }

                selectItem(position);
            }
        });

        if(mDrawerAdapter == null) {
            mDrawerAdapter = new NavigationDrawerAdapter(getActivity(), R.layout.row_nav_drawer_item);
            mDrawerListView.setAdapter(mDrawerAdapter);
            mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);

            mDrawerAdapter.notifyDataSetChanged();
        }

        return v;
    }


    public class NavigationDrawerAdapter extends ArrayAdapter<NavigationDrawerItem> {

        public NavigationDrawerAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount(){
            return items.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.row_nav_drawer_item, null);
            }

            final NavigationDrawerItem rowItem = items.get(position);

            if (rowItem != null) {
                ImageView icon = (ImageView)convertView.findViewById(R.id.icon);
                TextView text = (TextView)convertView.findViewById(R.id.title);
                LinearLayout row_item = (LinearLayout)convertView.findViewById(R.id.row_item);

                icon.setImageResource(rowItem.getImage());
                text.setText(rowItem.getTitle());

                if(mCurrentSelectedPosition == position){
                    row_item.setBackgroundColor(getResources().getColor(R.color.blue_button));
                } else {
                    row_item.setBackgroundColor(getResources().getColor(R.color.transparent));
                }

            }

            return convertView;
        }
    }


    private void selectItem(int position) {

        mDrawerLayout = ((MainActivity)getActivity()).mNavigationDrawerFragment.mDrawerLayout;
        mFragmentContainerView = ((MainActivity)getActivity()).mNavigationDrawerFragment.mFragmentContainerView;

        mCurrentSelectedPosition = position;
        mDrawerAdapter.notifyDataSetChanged();

        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(position, true);
        }

        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }

        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(items.get(position));
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);

        for(NavigationDrawerItem item : items){
            item.saveState(outState);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallbacks = (NavigationDrawerFragment.NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;

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

    public void onPurchaseCompleted(){

        if(((MainActivity)getActivity()).gamesStoreItem == null || ((MainActivity)getActivity()).gamesStoreItem.isPurchased) {
            if(items.contains(upgradeDrawItem)){
                items.remove(upgradeDrawItem);
            }
        } else {
            if(!items.contains(upgradeDrawItem)) {
                items.add(upgradeDrawItem);
            }
        }

        mDrawerAdapter.notifyDataSetChanged();
    }
}
