package uk.co.createanet.footballformapp.lib;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import uk.co.createanet.footballformapp.R;
import uk.co.createanet.footballformapp.models.TeamPlayerView;

/**
 * Created by matt on 28/07/2014.
 */
public class SelectListView {

    public ArrayList<TeamPlayerView> items;

    public int selectedIndex = -1;
    public long selectedId = -1;
    public int selectedLeague = -1;

    private Cursor cursor;

    private ListView listView;

    public SelectListView(final ListView listViewIn){
        listView = listViewIn;

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TeamPlayerView tpv = items.get(position);

                selectedIndex = position;
                selectedId = tpv.playerId;
                selectedLeague = tpv.leagueId;

                ((ScrollListViewAdapter) listView.getAdapter()).notifyDataSetChanged();
            }
        });
    }

    public void setCursor(Context context, final Cursor c){

        items = new ArrayList<TeamPlayerView>();

        cursor = c;

        c.moveToFirst();

        if(!c.isClosed()) {
            do {

                TeamPlayerView tpv = new TeamPlayerView();

                int colIndexGS = c.getColumnIndex("goals_scored");
                if (colIndexGS > -1) {
                    tpv.shouldShowGoalsScored = true;
                    tpv.playerName = c.getString(1);
                    tpv.goalsScored = c.getInt(colIndexGS);
                } else {
                    tpv.playerName = c.getString(1);
                }

                tpv.leagueId = c.getInt(c.getColumnIndex("league_id"));
                tpv.playerId = c.getInt(c.getColumnIndex("_id"));

                items.add(tpv);

            } while (c.moveToNext());

            c.close();

            ScrollListViewAdapter adapter = new ScrollListViewAdapter(context, R.layout.row_scroll_select);
            listView.setAdapter(adapter);
        }
    }

    public class ScrollListViewAdapter extends ArrayAdapter {

        private Resources r;

        public ScrollListViewAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);

            r = context.getResources();
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public int getItemViewType(int pos) {
            return 0;
        }

        @Override
        public int getCount() {
            return items == null ? 0 : items.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.row_scroll_select, null);
            }

            final TeamPlayerView rowItem = items.get(position);

            if (rowItem != null) {

                convertView.setBackgroundColor(r.getColor((position % 2 == 0) ? R.color.white : R.color.grey));

                ImageView image_selected = (ImageView)convertView.findViewById(R.id.image_selected);
                TextView text_title = (TextView)convertView.findViewById(R.id.text_title);
                TextView text_goals = (TextView)convertView.findViewById(R.id.text_goals);

                if(rowItem.shouldShowGoalsScored){
                    text_goals.setText(String.valueOf(rowItem.goalsScored));
                } else {
                    text_goals.setVisibility(View.GONE);
                }

                text_title.setText(rowItem.playerName);

                image_selected.setImageResource(selectedIndex == position ? R.drawable.tick_on : R.drawable.tick_off);

            }

            return convertView;

        }
    }

    public void onDestroy(){
        if(cursor != null){
            cursor.close();
        }
    }

}
