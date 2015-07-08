package uk.co.createanet.footballformapp.models;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import uk.co.createanet.footballformapp.util.*;

/**
 * Created by Matthew Grundy (Createanet) on 28/02/2014
 */
public class StoreManager {

    private final static String PREFS_NAME = "ff_store";

    public static final int ID_GAMES = 0;

    private PurchaseResultListener purchaseListener;
    private PurchaseUpdateListener updateListener;

    private static final String PURCHASED_ITEMS_KEYS = "purchased";
    private static final String PUBLIC_PURCHASE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsxDCmOd0BSu28rXUbcNDDg2MwotOgzzn5eXSK8V6HhRi741cwQjUbMzkQ4REfXVvJ9ITl7WUs26eYmul5TYAFZaZ7grcN//YVe1gok1VBYMTwh9yL5BxVKVKe0TNOQLnv7miaZE9FagAx07usdcU/IRCqjiy67Ey4s0Bu2DvlEvtLYg4VghLmiXv1p+ktZba+TtfjCOKlxRLrunI/yywCzxjG5lott8VlufuVxNJXRaqvjDDOcRz4ETEAkodo19bKu8ud7zIVYBsZ8GHzE+kO+eR7jEVVEYTmOofEnXQboEORLZcsIKSpgxmAQegJuPaZGTgV40DqrgjU59MtPNBkQIDAQAB";

    public IabHelper mHelper;

    private Context c;

    public boolean isStoreReady = false;

    private static ArrayList<StoreItem> allStoreItems;

    public StoreManager(Context cIn){
        c = cIn;
    }

    public static ArrayList<StoreItem> getAllStoreItems(Context c){
        return getAllStoreItems(c, true);
    }

    public static ArrayList<StoreItem> getAllStoreItems(Context c, boolean updateFromLocal){

        if(allStoreItems == null) {
            allStoreItems = new ArrayList<StoreItem>();

            StoreItem item = new StoreItem();
            item.id = ID_GAMES;
            item.title = "No Games";
            item.isPurchased = false;
            item.showOnStore = true;
            item.googleSKU = "no_games";

            allStoreItems.add(item);
        }

        if(updateFromLocal){
            // update with purchased
            allStoreItems = readPurchasedItems(allStoreItems, c);
        }

        return allStoreItems;
    }

    public static ArrayList<StoreItem> getPurchasedStoreItems(Context c){
        ArrayList<StoreItem> purchasedItems = new ArrayList<StoreItem>();

        for(StoreItem item : StoreManager.getAllStoreItems(c)){
            if(item.isPurchased){
               purchasedItems.add(item);
            }
        }

        return purchasedItems;
    }

    public static ArrayList<StoreItem> getPurchasableStoreItems(Context c){
        ArrayList<StoreItem> purchasedItems = new ArrayList<StoreItem>();

        for(StoreItem item : StoreManager.getAllStoreItems(c)){
            if(item.showOnStore){
                purchasedItems.add(item);
            }
        }

        return purchasedItems;
    }

    public static StoreItem getById(Context c, int id){
        StoreItem storeItem = null;

        for(StoreItem item : StoreManager.getAllStoreItems(c)){
            if(item.id == id){
                storeItem = item;
                break;
            }
        }

        return storeItem;
    }

    /*
    public void updateStoreFromGoogle(Context c){

        List additionalSkuList = new ArrayList();

        for(StoreItem item : getAllStoreItems(c, false)){
            if(item.googleSKU != null){
                additionalSkuList.add(item.googleSKU);
            }
        }

        mHelper.queryInventoryAsync(true, additionalSkuList, new IabHelper.QueryInventoryFinishedListener() {
            @Override
            public void onQueryInventoryFinished(IabResult result, Inventory inv) {

                if (result.isFailure()) {
                    // handle error
                    return;
                }

                for(StoreItem item : getAllStoreItems()){
                    if(item.googleSKU != null){
                        Log.d(Constants.TAG_APP, "App Price: " + inv.getSkuDetails(item.googleSKU));
                    }
                }

            }
        });

    }
    */

    /*
        Internal listeners - use the custom listener interfaces at the bottom instead of these
     */
    private IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase)
        {
            StoreItem purchasedItem = null;
            int index = 0;

            mHelper.flagEndAsync();

            if(purchase == null){
                Toast.makeText(c, "We can't find the requested item. Please try again soon!", Toast.LENGTH_SHORT).show();
                return;
            }

            for(StoreItem item : getAllStoreItems(c)){
                if(item.googleSKU.compareTo(purchase.getSku()) == 0){
                    purchasedItem = item;
                    break;
                }

                index++;
            }

            if (result.isFailure() || purchasedItem == null) {
                Log.d("FF PURCHASE", "Error purchasing: " + result);
                purchaseListener.purchaseFailed(purchasedItem, purchase);
                return;
            } else {
                purchasedItem.isPurchased = true;
                getAllStoreItems(c).get(index).isPurchased = true;
                purchaseListener.purchaseSuccess(purchasedItem, purchase);
            }

            writePurchasedItems(c);

        }
    };

     /*
        Internal listeners - use the custom listener interfaces at the bottom instead of these
     */
    private IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result,
                                             Inventory inventory) {

            mHelper.flagEndAsync();

            if (result.isFailure()) {
                updateListener.updateFailed();
                return;
            }

            for(StoreItem item : getAllStoreItems(c)){
                if(item.googleSKU != null){
                    item.isPurchased = inventory.hasPurchase(item.googleSKU);
                }
            }

            writePurchasedItems(c);

            updateListener.updateSuccess();

        }
    };

    /*
        Write purchased items to local storage
     */
    public static void writePurchasedItems(Context c){
        SharedPreferences settings = c.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        ArrayList<String> itemsOut = new ArrayList<String>();

        for(StoreItem item : getAllStoreItems(c, false)){
            if(item.isPurchased){
                itemsOut.add(item.googleSKU);
            }
        }

        String set = TextUtils.join("|", itemsOut);

        editor.putString(PURCHASED_ITEMS_KEYS, set);
        editor.commit();
    }

    /*
        Get purchased items from local storage
     */
    public static ArrayList<StoreItem> readPurchasedItems(ArrayList<StoreItem> items, Context c){
        SharedPreferences settings = c.getSharedPreferences(PREFS_NAME, 0);
        String storedItems = settings.getString(PURCHASED_ITEMS_KEYS, "");

        String[] pItems = TextUtils.split(storedItems, "");

        for(int i = 0, j = pItems.length; i < j; i++){
            for(StoreItem item : items){
                if(item.googleSKU != null && item.googleSKU.compareTo(pItems[i]) == 0){
                    item.isPurchased = true;
                }
            }
        }

        return items;
    }

    /*
        Buy the items - notifies of purchase success or failure
        Updates local store
     */
    public void purchaseStoreItem(Activity c, StoreItem item, PurchaseResultListener listener){
        purchaseListener = listener;

        mHelper.flagEndAsync();
        mHelper.launchPurchaseFlow(c, item.googleSKU, 10001, mPurchaseFinishedListener, "bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ");
    }

    /*
        Get the purchased items from Google
        Stores them locally for next time - should be called at app launch to sync with G account
     */
    public void checkPurchasedItems(PurchaseUpdateListener listener){
        updateListener = listener;

        mHelper.queryInventoryAsync(mGotInventoryListener);
    }

    /*
        Init method - must be called before doing any store activity
        Wait for the ready listener to return storeReady
        isStore ready can be used for manual checks
     */
    public void initialiseInAppPurchase(Context c, final PurchaseStoreReady readyListener){
        mHelper = new IabHelper(c, PUBLIC_PURCHASE_KEY);

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    isStoreReady = false;
                    readyListener.storeFailed();
                } else {
                    isStoreReady = true;
                    readyListener.storeReady();
                }
            }
        });
    }

    /*
        Destroy method - call on fragment/activity destroy
     */
    public void destroyInAppPurchase(){
        if (mHelper != null) mHelper.dispose();
        mHelper = null;
    }

    public abstract static class PurchaseResultListener {
        public abstract void purchaseSuccess(StoreItem item, Purchase purchase);
        public abstract void purchaseFailed(StoreItem item, Purchase purchase);
    }

    public abstract static class PurchaseUpdateListener {
        public abstract void updateSuccess();
        public abstract void updateFailed();
    }

    public abstract static class PurchaseStoreReady {
        public abstract void storeReady();
        public abstract void storeFailed();
    }

}
