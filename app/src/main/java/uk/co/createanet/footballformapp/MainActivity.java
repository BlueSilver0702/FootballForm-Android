package uk.co.createanet.footballformapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import uk.co.createanet.footballformapp.data.FFDatabase;
import uk.co.createanet.footballformapp.fragments.LiveScoresFragment;
import uk.co.createanet.footballformapp.fragments.NewsFragment;
import uk.co.createanet.footballformapp.fragments.RefreshInterface;
import uk.co.createanet.footballformapp.fragments.tab_containers.tabs.FixturesFragment;
import uk.co.createanet.footballformapp.fragments.tab_containers.tabs.FormPlayersDetailFragment;
import uk.co.createanet.footballformapp.fragments.tab_containers.tabs.LeagueDetailsFragment;
import uk.co.createanet.footballformapp.fragments.tab_containers.tabs.LeaguesTabFragment;
import uk.co.createanet.footballformapp.fragments.tab_containers.tabs.ScrollViewFragment;
import uk.co.createanet.footballformapp.fragments.tab_containers.tabs.SubLeagueDetailsFragment;
import uk.co.createanet.footballformapp.lib.CountrySelector;
import uk.co.createanet.footballformapp.lib.DataManager;
import uk.co.createanet.footballformapp.lib.GetRSSFeed;
import uk.co.createanet.footballformapp.lib.PushMessaging;
import uk.co.createanet.footballformapp.models.NavigationDrawerItem;
import uk.co.createanet.footballformapp.models.StoreItem;
import uk.co.createanet.footballformapp.models.StoreManager;
import uk.co.createanet.footballformapp.util.Purchase;

public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        LiveScoresFragment.OnChosenLiveScore,
        NewsFragment.OnSelectNewsItem,
        FixturesFragment.OnFixtureChosen,
        ScrollViewFragment.OnFilterChangeListener {

    private FFDatabase db;

    private boolean shouldExit;

    private int countyId;
    private StoreManager storeManager;
    public StoreItem gamesStoreItem;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    public NavigationDrawerFragment mNavigationDrawerFragment;
    private NavigationDrawerItem currentItem;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private GoogleCloudMessaging gcm;
    String gcmId = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadDatabase();

        countyId = CountrySelector.getLastCountry(MainActivity.this);

        mTitle = getTitle();

        if(mNavigationDrawerFragment == null) {
            mNavigationDrawerFragment = (NavigationDrawerFragment)
                    getFragmentManager().findFragmentById(R.id.navigation_drawer);

        /*
        Log.d("DB Path", "PATH: " + DataManager.getDatabasePath(MainActivity.this));
        Log.d("DB", (new File(DataManager.getDatabasePath(MainActivity.this))).getName());
        Log.d("DB", (new File(DataManager.getDatabasePath(MainActivity.this))).getParentFile().getAbsolutePath());
        */

            // Set up the drawer.
            mNavigationDrawerFragment.setUp(
                    R.id.navigation_drawer,
                    (DrawerLayout) findViewById(R.id.drawer_layout));

        }

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .showImageOnFail(R.drawable.ic_launcher)
                .resetViewBeforeLoading(false)
                .delayBeforeLoading(200)
                .cacheInMemory(false)
                .cacheOnDisc(false)
                .bitmapConfig(Bitmap.Config.ARGB_8888)
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .build();
        ImageLoader.getInstance().init(config);

        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("B695A33BA396FC7A91EF5899F06F2E4C").build();
        adView.loadAd(adRequest);

        storeManager = new StoreManager(MainActivity.this);
        storeManager.initialiseInAppPurchase(MainActivity.this, new StoreManager.PurchaseStoreReady() {
            @Override
            public void storeReady() {

                storeManager.checkPurchasedItems(new StoreManager.PurchaseUpdateListener() {
                    @Override
                    public void updateSuccess() {
                        gamesStoreItem = StoreManager.getById(MainActivity.this, StoreManager.ID_GAMES);

                        if (savedInstanceState == null && DataManager.shouldShowPurchasePrompt(MainActivity.this)) {

                            if (!gamesStoreItem.isPurchased) {

                                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.YourAlertDialogTheme);
                                builder.setTitle("Get the Best")
                                        .setMessage("Get the best out of Football Form by purchasing the ability to view a larger range of previous stats!")
                                        .setPositiveButton("Buy Now", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();

                                                storeManager.purchaseStoreItem(MainActivity.this, gamesStoreItem, new StoreManager.PurchaseResultListener() {
                                                    @Override
                                                    public void purchaseSuccess(StoreItem item, Purchase purchase) {

                                                        // show them a message
                                                        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.YourAlertDialogTheme);
                                                        builder.setTitle("Purchase Complete").setMessage("You can now view more than 3 games")
                                                                .create().show();

                                                    }

                                                    @Override
                                                    public void purchaseFailed(StoreItem item, Purchase purchase) {

                                                    }
                                                });

                                            }
                                        })
                                        .setNegativeButton("Not Now", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });

                                Dialog d = builder.create();
                                d.show();
                            }

                        }

                        mNavigationDrawerFragment.onPurchaseUpdated();
                    }

                    @Override
                    public void updateFailed() {
                    }
                });

            }

            @Override
            public void storeFailed() {

            }
        });

    }

    public void loadDatabase() {
        db = new FFDatabase(MainActivity.this);
    }

    @Override
    public void onNavigationDrawerItemSelected(NavigationDrawerItem item) {
        // update the main content by replacing fragments
        if(item != currentItem) {
            // reset the current tab
            /*
            SharedPreferences settings = getSharedPreferences(AbstractTabbedFragment.KEY_PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt(AbstractTabbedFragment.KEY_CURRENT_TAB, 0);
            editor.commit();
            */

            FragmentManager fragmentManager = getSupportFragmentManager();

            Fragment f = item.getFragment();

            fragmentManager.beginTransaction()
                    .replace(R.id.container, f)
//                    .addToBackStack("frame")
                    .commit();

            currentItem = item;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        int googlePlayResult = GooglePlayServicesUtil.isGooglePlayServicesAvailable(MainActivity.this);

        if (googlePlayResult == ConnectionResult.SUCCESS) {
            if (gcm == null) {
                gcm = GoogleCloudMessaging.getInstance(MainActivity.this);
            }

            if (gcm != null) {
                new AsyncTask() {
                    @Override
                    protected String doInBackground(Object[] objects) {
                        try {
                            gcmId = gcm.register("982759616887");
                            PushMessaging.saveToken(MainActivity.this, gcmId);

                            Log.d("GCM", "Push message: " + gcmId);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        return gcmId;
                    }
                }.execute(null, null, null);
            }
        } else {
            Dialog d = GooglePlayServicesUtil.getErrorDialog(googlePlayResult, MainActivity.this, 1000);
            d.show();
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(mNavigationDrawerFragment.getNavigationMode());
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public void onChosenLiveScore(int id) {

        Intent i = new Intent(MainActivity.this, LiveScoreDetailActivity.class);
        i.putExtra(LiveScoreDetailActivity.KEY_LIVE_SCORE_ID, id);
        startActivity(i);

    }

    @Override
    public void onNewsItemSelected(GetRSSFeed.Entry selectedEntry) {
        startActivity(NewsDetailActivity.newInstance(MainActivity.this, selectedEntry));
    }

    @Override
    public void onFixtureChosen(int leagueId, int fixtureId) {

        Intent i = new Intent(MainActivity.this, FixtureDetailActivity.class);
        i.putExtra(FixtureDetailActivity.KEY_FIXTURE_ID, fixtureId);
        i.putExtra(FixtureDetailActivity.KEY_LEAGUE_ID, leagueId);
        startActivity(i);

    }

    public void setCountryId(int countryId) {
        this.countyId = countryId;
        CountrySelector.setCountryId(MainActivity.this, countryId);

        try {
            ((RefreshInterface) currentItem.getFragment()).refresh(countyId);
        } catch (ClassCastException e) {
            Log.d("No Refresh", "Tab does not implement refresh listener");
        }
    }

    public int getCountyId() {
        return countyId;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mNavigationDrawerFragment.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFilterSet(ScrollViewFragment.FILTER_TYPE filterId) {
        Log.d("FF", "Filter set");

        LeaguesTabFragment frag = (LeaguesTabFragment) currentItem.getFragment();
        Fragment fragDetails = frag.getCurrentFragment();

        if (fragDetails instanceof FormPlayersDetailFragment) {
            FormPlayersDetailFragment subLeagueDetailsFragment = (FormPlayersDetailFragment) fragDetails;
            subLeagueDetailsFragment.setFilterType(filterId);
        } else if (fragDetails instanceof LeagueDetailsFragment) {
            SubLeagueDetailsFragment subLeagueDetailsFragment = ((LeagueDetailsFragment) fragDetails).getCurrentItem();
            subLeagueDetailsFragment.setFilterType(filterId);
        } else {
            Log.d("FF", "Unrecognised instance for setting filter");
        }

    }

    class UpdateExitStatus extends TimerTask {
        public void run() {
            shouldExit = false;
        }
    }

    @Override
    public void onBackPressed() {

        if (shouldExit) {
            doFinish();
        } else {
            Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();

            shouldExit = true;

            new Timer().schedule(new UpdateExitStatus(), 2000);
        }

    }

    private void doFinish() {
        finish();
    }

    public FFDatabase getDatabase() {
        if (db == null) {
            db = new FFDatabase(MainActivity.this);
        }

        return db;
    }

    @Override
    public void onSaveInstanceState(Bundle b) {
        b.putInt("STATE", 1);

        super.onSaveInstanceState(b);
    }

    public void purchaseUpgrade(StoreManager.PurchaseResultListener purchaseResultListener) {
        storeManager.purchaseStoreItem(MainActivity.this, gamesStoreItem, purchaseResultListener);
    }

}
