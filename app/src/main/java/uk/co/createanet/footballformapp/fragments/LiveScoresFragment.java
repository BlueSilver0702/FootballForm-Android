package uk.co.createanet.footballformapp.fragments;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import uk.co.createanet.footballformapp.MainActivity;
import uk.co.createanet.footballformapp.R;
import uk.co.createanet.footballformapp.lib.CreateaResponseHandler;
import uk.co.createanet.footballformapp.lib.NoResultsAdapter;
import uk.co.createanet.footballformapp.lib.RestClient;
import uk.co.createanet.footballformapp.models.LiveScore;

public class LiveScoresFragment extends Fragment implements AbsListView.OnItemClickListener, RefreshInterface {


    private OnChosenLiveScore mListener;
    private ArrayList<LiveScore> items;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private LiveScoresAdapter mAdapter;

    public static LiveScoresFragment newInstance() {
        LiveScoresFragment fragment = new LiveScoresFragment();
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public LiveScoresFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        mAdapter = new ArrayAdapter<DummyContent.DummyItem>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, DummyContent.ITEMS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_livescores, container, false);


        getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        getActivity().setTitle("Live Scores");


        setHasOptionsMenu(true);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        mAdapter = new LiveScoresAdapter(getActivity(), R.layout.row_live_score);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        getLiveScores();

        return view;
    }

    public void getLiveScores(){

        // TODO: Use the country API that isn't hacked to always give results
        RequestParams params = new RequestParams();
        params.add("league_id", String.valueOf(((MainActivity) getActivity()).getCountyId()));
        params.add("type", "GET_GAMES");

        RestClient.get(getActivity(), "live_scores_api_country_id.php", params, new CreateaResponseHandler(getActivity()) {

            @Override
            public void onSuccess(JSONObject jsonObject) {
                super.onSuccess(jsonObject);

                try {
                    items = new ArrayList<LiveScore>();

                    if(jsonObject.has("data")) {
                        JSONObject matchData = jsonObject.getJSONObject("data");

                        if(matchData.has("match_data")) {
                            JSONArray data = matchData.getJSONArray("match_data");

                            for (int i = 0, j = data.length(); i < j; i++) {
                                items.add(new LiveScore(data.getJSONObject(i)));
                            }
                        }
                    }

                    if (items.size() == 0) {
                        mListView.setAdapter(new NoResultsAdapter(getActivity(), "No Live Results"));
                    } else {
                        mListView.setAdapter(mAdapter);
                    }

                    mAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

        });

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnChosenLiveScore) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (items != null && items.size() > 0 && null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onChosenLiveScore(items.get(position).id);
        }
    }

    @Override
    public void refresh(int countryId) {
        getLiveScores();
    }

    public interface OnChosenLiveScore {
        public void onChosenLiveScore(int id);
    }

    public class LiveScoresAdapter extends ArrayAdapter<JSONArray> {

        public LiveScoresAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
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
                convertView = View.inflate(getContext(), R.layout.row_live_score, null);
            }

            final LiveScore rowItem = items.get(position);

            if (rowItem != null) {

                convertView.setBackgroundColor(getResources().getColor(position % 2 == 0 ? R.color.white : R.color.grey));

                convertView.setId(rowItem.id);

                TextView player_left = (TextView) convertView.findViewById(R.id.player_left);
                TextView player_right = (TextView) convertView.findViewById(R.id.player_right);
                TextView text_score = (TextView) convertView.findViewById(R.id.text_score);
                TextView text_time = (TextView) convertView.findViewById(R.id.text_time);
                TextView league_name = (TextView) convertView.findViewById(R.id.league_name);

                player_left.setText(rowItem.team_home_name);
                player_right.setText(rowItem.team_away_name);

                text_score.setText(rowItem.score);
                text_time.setText(rowItem.isFullTime() ? "FT" : rowItem.start_time);

                league_name.setText(rowItem.league_name);

                boolean redText = !rowItem.isFullTime() && rowItem.score != null && rowItem.score.length() > 0;

                text_time.setTextColor(getResources().getColor(redText ? R.color.lost : R.color.black));
                text_score.setTextColor(getResources().getColor(redText ? R.color.lost : R.color.black));
            }

            return convertView;

        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_refresh, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        final int id = item.getItemId();

        switch (id) {
            case R.id.action_refresh:

                getLiveScores();
                break;

            default:
                return true;
        }

        return false;
    }
}
