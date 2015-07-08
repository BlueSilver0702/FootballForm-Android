package uk.co.createanet.footballformapp.views;

import android.content.Context;
import android.util.AttributeSet;

import uk.co.createanet.footballformapp.R;

/**
 * Created by matt on 09/07/2014.
 */
public class FavouriteImageView extends android.widget.ImageView {

    public boolean isFavourite = false;
    public int teamId;
    public int leagueId;

    public FavouriteImageView(Context context) {
        super(context);
    }

    public FavouriteImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void refreshImageView(){
        setImageDrawable(getResources().getDrawable(isFavourite ? R.drawable.icon_star_full : R.drawable.icon_star_empty));
    }
}
