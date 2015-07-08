package uk.co.createanet.footballformapp.fragments.tab_containers.tabs;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import uk.co.createanet.footballformapp.R;
import uk.co.createanet.footballformapp.data.FFDatabase;
import uk.co.createanet.footballformapp.models.ColumnIdentifier;

public abstract class ScrollViewFragment extends Fragment implements AbsListView.OnItemClickListener {

    public static enum FILTER_TYPE {
        ALL, HOME, AWAY
    };

    public FILTER_TYPE filterHomeAway = FILTER_TYPE.ALL;
    private String sortOrder;

    private static final int COLUMN_PADDING = 5;

    protected ListView listView;
    private LinearLayout headerLayout;

    private ArrayList<ColumnIdentifier> identifiers;

    private String lastSortCol = null;
    private boolean descending = false;

    protected ClientCursorAdapter adapter;

    private LinearLayout.LayoutParams params;

    private OnFilterChangeListener onFilterChangeListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ScrollViewFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scroll, container, false);

        // Set the adapter
        listView = (ListView) view.findViewById(R.id.list_view);
        headerLayout = (LinearLayout) view.findViewById(R.id.header_view);

        identifiers = getColumnIdentifiers();


        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER_VERTICAL;

        View.OnClickListener cl = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tagCol = (String)v.getTag();

                if(tagCol.equals(lastSortCol)){
                    descending = !descending;
                } else {
                    descending = false;
                }

                lastSortCol = tagCol;

                getCursor();
            }
        };

        Resources r = getResources();
        int px = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, r.getDisplayMetrics()));

        LinearLayout.LayoutParams layoutParamsCenterVertical = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParamsCenterVertical.gravity = Gravity.CENTER_VERTICAL;

        for(ColumnIdentifier id : identifiers) {
            TextView headerText = (TextView)createTextViewColumn(id.title);
            headerText.setTypeface(null, Typeface.BOLD);
            headerText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            headerText.setTextColor(getResources().getColor(R.color.white));
            headerText.setPadding(px, px, px, px);
            headerText.setGravity(Gravity.CENTER_VERTICAL);

            if(!id.sortable) {
                headerText.setLayoutParams(new LinearLayout.LayoutParams(id.getColumnSizePx(), LinearLayout.LayoutParams.MATCH_PARENT));
                addColumnPadding(headerText);
                headerLayout.addView(headerText);
            } else {

                LinearLayout l = new LinearLayout(getActivity());
                l.setTag(id.sqlColumn);
                l.setOrientation(LinearLayout.HORIZONTAL);
                l.addView(headerText);

                ImageView i = new ImageView(getActivity());
                i.setImageDrawable(getResources().getDrawable(R.drawable.sort_arrows));
                i.setLayoutParams(layoutParamsCenterVertical);
                i.setPadding(COLUMN_PADDING, 0, 0, 0);
                l.addView(i);

                l.setOnClickListener(cl);

                l.setLayoutParams(new LinearLayout.LayoutParams(id.getColumnSizePx(), LinearLayout.LayoutParams.MATCH_PARENT));
                addColumnPadding(l);

                headerLayout.addView(l);
            }

        }

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            getCursor();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            onFilterChangeListener = (OnFilterChangeListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                + " must implement OnFragmentInteractionListener");
        }
    }

    public interface OnFilterChangeListener {
        public void onFilterSet(FILTER_TYPE filterId);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    public class ClientCursorAdapter extends ResourceCursorAdapter {

        public ClientCursorAdapter(Context context, int layout, Cursor c, int flags) {
            super(context, layout, c, flags);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            LinearLayout ll = (LinearLayout)view.findViewById(R.id.row_item);
            ll.removeAllViews();

            for(ColumnIdentifier id : identifiers){

                View v = customiseColumn(view, id, cursor);
                if(v == null){
                    v = createTextViewColumn(cursor.getString(cursor.getColumnIndex(id.sqlColumn)));
                }

                ll.setBackgroundColor(getResources().getColor(cursor.getPosition() % 2 == 0 ? R.color.white : R.color.grey));

                v.setLayoutParams(new LinearLayout.LayoutParams(id.getColumnSizePx(), LinearLayout.LayoutParams.WRAP_CONTENT));
                addColumnPadding(v);

                ll.addView(v);
            }
        }
    }

    private void addColumnPadding(View v){
        v.setPadding(COLUMN_PADDING, COLUMN_PADDING, COLUMN_PADDING, COLUMN_PADDING);
    }

    public void getCursor(){
        getCursorLocal(filterHomeAway, lastSortCol == null ? null : lastSortCol + (descending ? " ASC" : " DESC"), new FFDatabase.QueryListener() {
            @Override
            public void onQueryComplete(Cursor c) {
                adapter = new ClientCursorAdapter(getActivity(), R.layout.row_grid, c, 0);
                listView.setAdapter(adapter);
            }
        });
    }

    public View createTextViewColumn(String value) {
        return createTextViewColumn(value, false);
    }

    public View createTextViewColumn(String value, boolean highlight){
        TextView name = new TextView(getActivity());
        name.setText(value);
        name.setGravity(Gravity.CENTER_VERTICAL);
        name.setLayoutParams(params);
        name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);

        if(highlight){
            name.setTextColor(getResources().getColor(R.color.blue_button));
            name.setTypeface(null, Typeface.BOLD_ITALIC);
        }

        return name;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();

        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_home_away_all, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        final int id = item.getItemId();

        switch (id){
            case R.id.action_all:
                filterHomeAway = FILTER_TYPE.ALL;
                break;

            case R.id.action_away:
                filterHomeAway = FILTER_TYPE.AWAY;
                break;

            case R.id.action_home:
                filterHomeAway = FILTER_TYPE.HOME;
                break;

            default:
                return false;
        }

        onFilterChangeListener.onFilterSet(filterHomeAway);

        setFilterType(filterHomeAway);
        return true;
    }

    public void setFilterType(FILTER_TYPE type){
        filterHomeAway = type;
        getCursor();

    }

    public abstract void getCursorLocal(FILTER_TYPE filter_type, String sortCol, FFDatabase.QueryListener l);
    public abstract ArrayList<ColumnIdentifier> getColumnIdentifiers();
    public abstract View customiseColumn(View convertView, ColumnIdentifier columnIdentifier, Cursor cursor);
}
