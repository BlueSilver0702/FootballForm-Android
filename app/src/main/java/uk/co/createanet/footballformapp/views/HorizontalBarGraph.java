package uk.co.createanet.footballformapp.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import uk.co.createanet.footballformapp.R;

/**
 * Created by matt on 09/07/2014.
 */
public class HorizontalBarGraph extends LinearLayout {

    private Context c;
    private int leftValue = 0;
    private int rightValue = 0;
    private String title;

    private LinearLayout.LayoutParams paramsInnerBar;
    private LinearLayout barGraphContainer, barGraph;

    private TextView leftAmount;
    private TextView rightAmount;

    private int pxHeight;
    private int color;
    private boolean isMins;

    public HorizontalBarGraph(Context context, String titleIn, int leftValueIn, int rightValueIn) {
        this(context, titleIn, leftValueIn, rightValueIn, R.color.white);
    }

    public HorizontalBarGraph(Context context, String titleIn, int leftValueIn, int rightValueIn, int color) {
        this(context, titleIn, leftValueIn, rightValueIn, color, false);
    }

    public HorizontalBarGraph(Context context, String titleIn, int leftValueIn, int rightValueIn, int color, boolean mins) {
        super(context);

        this.c = context;
        this.color = color;
        this.isMins = mins;

        init(titleIn, leftValueIn, rightValueIn);
        buildView();
    }

    public HorizontalBarGraph(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.c = context;

        buildView();
    }

    public void init(String titleIn, int leftValueIn, int rightValueIn){
        title = titleIn;
        leftValue = leftValueIn;
        rightValue = rightValueIn;

        Resources r = getResources();
        pxHeight = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, r.getDisplayMetrics());

        setBackgroundColor(r.getColor(color));

        buildView();
    }

    public void setValues(int left, int right){
        leftValue = left;
        rightValue = right;

        buildView();
    }

    public void buildView(){
        if(barGraphContainer != null){
            setBarWidth();
            return;
        }

        LinearLayout.LayoutParams paramsTV = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        paramsTV.gravity = Gravity.CENTER;

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        LinearLayout wrapper = new LinearLayout(c);
        wrapper.setOrientation(VERTICAL);
        wrapper.setPadding(10, 10, 10, 10);
        wrapper.setLayoutParams(params);

        TextView titleTextView = new TextView(c);
        titleTextView.setText(title);
        titleTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        titleTextView.setTypeface(null, Typeface.BOLD);

        wrapper.addView(titleTextView);


        // Container view
        LinearLayout ll = new LinearLayout(c);

        ll.setLayoutParams(params);

        // Left score text view
        leftAmount = buildTextView(String.valueOf(leftValue), paramsTV);
        rightAmount = buildTextView(String.valueOf(rightValue), paramsTV);
        ll.addView(leftAmount);

        LinearLayout.LayoutParams paramsBar = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, pxHeight);
        paramsBar.weight = 1;

        barGraphContainer = new LinearLayout(c);
        barGraphContainer.setLayoutParams(paramsBar);

        if(!isMins) {
            barGraphContainer.setBackgroundColor(c.getResources().getColor(R.color.blue_button));

            barGraph = new LinearLayout(c);
            barGraph.setBackgroundColor(c.getResources().getColor(R.color.dark_blue));
            barGraphContainer.addView(barGraph);
        }

        ll.addView(barGraphContainer);

        // Right score text view
        ll.addView(rightAmount);

        wrapper.addView(ll);
        addView(wrapper);

        setBarWidth();

    }

    private void setBarWidth(){
        if(barGraphContainer == null) return;

        if(barGraph != null) {

            float totalWidth = barGraphContainer.getMeasuredWidth();
            float totalItems = leftValue + rightValue;
            float perItem = totalWidth / totalItems;
            int width = Math.round(perItem * leftValue);

            Log.d("FF", "Width: " + width);

            paramsInnerBar = new LinearLayout.LayoutParams(width, pxHeight);
            barGraph.setLayoutParams(paramsInnerBar);
        }

        leftAmount.setText(String.valueOf(leftValue) + (isMins ? " mins" : ""));
        rightAmount.setText(String.valueOf(rightValue) + (isMins ? " mins" : ""));

    }

    private TextView buildTextView(String value, LayoutParams params){

        TextView tv = new TextView(c);
        tv.setText(value + (isMins ? " mins" : ""));
        tv.setGravity(Gravity.CENTER);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setPadding(8, 8, 8, 8);

        tv.setLayoutParams(params);

        return tv;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        buildView();
    }
}
