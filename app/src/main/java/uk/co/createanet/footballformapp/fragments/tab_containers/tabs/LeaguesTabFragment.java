package uk.co.createanet.footballformapp.fragments.tab_containers.tabs;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Field;

import uk.co.createanet.footballformapp.fragments.RefreshInterface;
import uk.co.createanet.footballformapp.fragments.tab_containers.AbstractLeaguesTabsFragment;
import uk.co.createanet.footballformapp.fragments.tab_containers.PlayerVsTabFragment;

/**
 * Created by matt on 03/07/2014.
 */
public class LeaguesTabFragment extends AbstractLeaguesTabsFragment implements RefreshInterface {

    public static final String KEY_TYPE = "view_type";

    public static final int TYPE_LEAGUE = 0;
    public static final int TYPE_PLAYER_VS = 1;
    public static final int TYPE_FORM_PLAYERS = 2;
    public static final int FIXTURES = 3;

    public static LeaguesTabFragment newInstance(boolean isFavourites, int type){
        LeaguesTabFragment frag = new LeaguesTabFragment();

        Bundle b = new Bundle();
        b.putBoolean(LeagueDetailsFragment.KEY_IS_FAVOURITES, isFavourites);
        b.putInt(KEY_TYPE, type);

        frag.setArguments(b);

        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = super.onCreateView(inflater, container, savedInstanceState);

        getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        setTitle();

        return v;
    }

    @Override
    public Fragment getNewFragment(int id) {
        switch (type){
            case TYPE_PLAYER_VS:
                return PlayerVsTabFragment.newInstance(id);
            case TYPE_FORM_PLAYERS:
                return FormPlayersDetailFragment.newInstance(id);
            case FIXTURES:
                return FixturesFragment.newInstance(id);
        }

        return LeagueDetailsFragment.newInstance(id, isFavourites);
    }

    public void setTitle(){
        String title = "Leagues";

        switch (type){
            case TYPE_PLAYER_VS:
                title = "Player VS";
                break;
            case TYPE_FORM_PLAYERS:
                title = "Form Players";
                break;
            case FIXTURES:
                title = "Fixtures";
                break;
        }

        if(isFavourites){
            title = "Favourites";
        }

        getActivity().getActionBar().setTitle(title);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        try {
            Field childFragmentManager = android.support.v4.app.Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void refresh(int countryId) {
        getTabs();
    }
}
