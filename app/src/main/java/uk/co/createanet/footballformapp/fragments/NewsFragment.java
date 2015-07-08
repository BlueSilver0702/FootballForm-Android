package uk.co.createanet.footballformapp.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;

import java.util.ArrayList;

import uk.co.createanet.footballformapp.R;
import uk.co.createanet.footballformapp.lib.GetRSSFeed;

public class NewsFragment extends GetRSSFeed implements AbsListView.OnItemClickListener {

    private static final String KEY_URL_KEY = "key_url";

    private ArrayList<Entry> newsItems;
    private OnSelectNewsItem mListener;

    private ListView mListView;
    private NewsAdapter mAdapter;
    private String url;

    public static NewsFragment newInstance(String url) {
        NewsFragment fragment = new NewsFragment();

        Bundle b = new Bundle();
        b.putString(KEY_URL_KEY, url);

        fragment.setArguments(b);

        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NewsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() != null){
            url = getArguments().getString(KEY_URL_KEY);
        }

        new DownloadXmlTask().execute(url);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_newsfragment, container, false);

        getActivity().getActionBar().setTitle("News");

        // Set the adapter
        mListView = (ListView) view.findViewById(android.R.id.list);
        mAdapter = new NewsAdapter(getActivity(), R.layout.row_news);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnSelectNewsItem) activity;
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
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onNewsItemSelected(newsItems.get(position));
        }
    }

    @Override
    public void gotNewsItems(ArrayList<Entry> items) {
        newsItems = items;

        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    public interface OnSelectNewsItem {
        public void onNewsItemSelected(Entry selected);
    }

    public class NewsAdapter extends ArrayAdapter<JSONArray> {

        public NewsAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getViewTypeCount(){
            return 1;
        }

        @Override
        public int getItemViewType(int pos){
            return 0;
        }

        @Override
        public int getCount(){
            return (newsItems == null || newsItems.size() == 0) ? 0 : newsItems.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if(position >= newsItems.size()){
                View loadingRow = View.inflate(getContext(), R.layout.row_loading, null);
                return loadingRow;
            }

            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.row_news, null);
            }

            convertView.setBackgroundColor(getResources().getColor(position % 2 == 0 ? R.color.white : R.color.grey));

            final Entry rowItem = newsItems.get(position);

            if (rowItem != null) {
                TextView title = (TextView)convertView.findViewById(R.id.text_title);
                TextView text_date = (TextView)convertView.findViewById(R.id.text_date);
                ImageView image_news = (ImageView)convertView.findViewById(R.id.image_news);

                title.setText(rowItem.title);
                text_date.setText(rowItem.getPubDate());

                ImageLoader.getInstance().displayImage(rowItem.imageUrl, image_news);

            }

            return convertView;
        }
    }

}
