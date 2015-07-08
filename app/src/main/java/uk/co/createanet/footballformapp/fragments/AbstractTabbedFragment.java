package uk.co.createanet.footballformapp.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Field;
import java.util.ArrayList;

import uk.co.createanet.footballformapp.R;
import uk.co.createanet.footballformapp.models.TabItem;

public abstract class AbstractTabbedFragment
        extends Fragment
        implements ActionBar.TabListener {

    public static final String KEY_PREFS_NAME = "ff_prefs";
    public static final String KEY_CURRENT_TAB = "current_tab";

    protected ArrayList<TabItem> tabItems;

    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    public int currentPosition;

    private static final Field sChildFragmentManagerField;

    static {
        Field f = null;
        try {
            f = Fragment.class.getDeclaredField("mChildFragmentManager");
            f.setAccessible(true);
        } catch (NoSuchFieldException e) {
        }
        sChildFragmentManagerField = f;
    }

    public AbstractTabbedFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();

        if (actionBar != null) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        }

//        initTabs();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fixtures_list, container, false);

        final ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();

        // this does work, but gets destroyed when recreated by the nav draw
        if(savedInstanceState != null){
            currentPosition = savedInstanceState.getInt(KEY_CURRENT_TAB, currentPosition);
        }

//        SharedPreferences settings = getActivity().getSharedPreferences(KEY_PREFS_NAME, 0);
//        currentPosition = settings.getInt(KEY_CURRENT_TAB + this.getClass().getName(), 0);

        Log.d("FF", "Restarting at: " + currentPosition + " for key: " + KEY_CURRENT_TAB + this.getClass().getName());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) view.findViewById(R.id.pager);
        mViewPager.setOffscreenPageLimit(0);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a ence to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

//        initTabs();

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        currentPosition = tab.getPosition();
        mViewPager.setCurrentItem(currentPosition);
//        mSectionsPagerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onSaveInstanceState(Bundle b){
        super.onSaveInstanceState(b);

        // this is bull, I think it's caused by abusing the drawlayout and tablayout together
        SharedPreferences settings = getActivity().getSharedPreferences(KEY_PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(KEY_CURRENT_TAB + this.getClass().getName(), currentPosition);
        editor.commit();

        b.putInt(KEY_CURRENT_TAB, currentPosition);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (sChildFragmentManagerField != null) {
            try {
                sChildFragmentManagerField.set(this, null);
            } catch (Exception e) {
            }
        }

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

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Fragment getItem(int position) {
            return tabItems.get(position).getFragment();
        }

        @Override
        public int getCount() {
            return tabItems.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabItems.get(position).getTitle();
        }
    }

    public void setUpTabs(ArrayList<TabItem> items) {
        tabItems = items;
        initTabs();
    }

    private void initTabs() {
        if (tabItems == null || tabItems.size() == 0 || getActivity() == null) {
            Log.d("FF", "No data for the tabs");
            return;
        }

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        FragmentManager fm = getChildFragmentManager();

        mSectionsPagerAdapter = new SectionsPagerAdapter(fm);

        if(fm.getFragments() != null) {
            fm.getFragments().clear();
        }

        mViewPager.removeAllViews();
        mViewPager.setAdapter(mSectionsPagerAdapter);

        final ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        actionBar.removeAllTabs();

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < tabItems.size(); i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(tabItems.get(i).getTitle())
                            .setTabListener(this)
            );
        }

        Log.d("FF", "Reloading tab pager with index: " + currentPosition);

//        actionBar.selectTab(actionBar.getTabAt(currentPosition));
    }

    public int getCurrentTabId() {
        return getTabId(currentPosition);
    }

    public Fragment getCurrentFragment(){
        return tabItems.get(currentPosition).getFragment();
    }

    public int getTabId(int pos) {
        if (tabItems == null) {
            return -1;
        }

        return tabItems.get(pos).getId();

    }

    public void setSelectedTab(int pos){
        currentPosition = pos;
//        mViewPager.setCurrentItem(currentPosition);
    }

}
