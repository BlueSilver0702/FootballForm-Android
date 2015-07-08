package uk.co.createanet.footballformapp.views;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by matt on 09/07/2014.
 */
public class Last5ImageView extends android.widget.ImageView {

    public String team1Name;
    public String team2Name;
    public int team1Score;
    public int team2Score;

    public Last5ImageView(Context context) {
        super(context);
    }

    public Last5ImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public String getTitle(){
        return String.format("%s v %s", team1Name, team2Name);
    }

    public String getScore(){
        return String.format("%d - %d", team1Score, team2Score);
    }
}
