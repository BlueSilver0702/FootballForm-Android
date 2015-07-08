package uk.co.createanet.footballformapp.models;

/**
 * Created by Matthew Grundy (Createanet) on 28/02/2014
 */
public class StoreItem {
    public int id = 0;
    public String description;
    public boolean isPurchased = false;
    public String title;
    public String googleSKU;

    public boolean showOnStore = true;

    public String toString(){
        return title;
    }

}
