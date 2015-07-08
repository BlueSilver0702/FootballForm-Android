package uk.co.createanet.footballformapp.models;


import android.support.v4.app.Fragment;

/**
 * Created by matt on 09/04/2014.
 */
public class TabItem {

    private Fragment fragment;
    private String title;
    private int id;

    public TabItem(int id, String titleIn, Fragment fragmentIn){
        this.id = id;
        title = titleIn;
        fragment = fragmentIn;
    }

    public String getTitle(){
        return title;
    }

    public Fragment getFragment(){
        return fragment;
    }

    public int getId(){ return id; }

}
