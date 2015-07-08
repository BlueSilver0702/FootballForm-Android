package uk.co.createanet.footballformapp;

import android.app.Activity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

/**
 * Created by matt on 09/02/15.
 */
public class AdvertActivity extends Activity {

    private AdView advertView;

    @Override
    public void onResume(){
        super.onResume();

        if(advertView == null){
            advertView = (AdView)findViewById(R.id.adView);

            AdRequest adRequest = new AdRequest.Builder().addTestDevice("B695A33BA396FC7A91EF5899F06F2E4C").build();
            advertView.loadAd(adRequest);
        }
    }



}
