package uk.co.createanet.footballformapp.models;


import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import uk.co.createanet.footballformapp.fragments.AbstractTabbedFragment;
import uk.co.createanet.footballformapp.fragments.tab_containers.tabs.LeaguesTabFragment;

/**
 * Created by matt on 09/04/2014.
 */
public class NavigationDrawerItem {

    private Fragment fragment;
    private String title;
    private int imageResource;
    public int navigationMode = ActionBar.NAVIGATION_MODE_STANDARD;

    public NavigationDrawerItem(String titleIn, Fragment fragmentIn, int imageResourceIn) {

        int navMode = navigationMode;

        if(fragmentIn instanceof AbstractTabbedFragment){
            navMode = ActionBar.NAVIGATION_MODE_TABS;
        }

        title = titleIn;
        fragment = fragmentIn;
        imageResource = imageResourceIn;
        navigationMode = navMode;
    }

    public int getImage(){
        return imageResource;
    }

    public String getTitle(){
        return title;
    }

    public Fragment getFragment(){
        return fragment;
    }

    public void saveState(Bundle b){
        if(fragment instanceof LeaguesTabFragment){
            LeaguesTabFragment leaguesTabFragment = (LeaguesTabFragment)fragment;
            Bundle innerB = leaguesTabFragment.getArguments();

            Log.d("FF", "TABSTATE: " + "leaguetab_position_" + innerB.getInt("view_type") + " -> " + leaguesTabFragment.currentPosition);

            b.putInt("leaguetab_position_" + innerB.getInt("view_type"), leaguesTabFragment.currentPosition);
        }
    }

    public void restoreState(Bundle b){
        if(fragment instanceof LeaguesTabFragment){
            LeaguesTabFragment leaguesTabFragment = (LeaguesTabFragment)fragment;

            int curPos = b.getInt("leaguetab_position_" + leaguesTabFragment.type);

            Log.d("FF", "Restoring tab: " + leaguesTabFragment.type + " at index: " + curPos);

            if(curPos > 0){
                leaguesTabFragment.setSelectedTab(curPos);
            }
        }
    }

}
