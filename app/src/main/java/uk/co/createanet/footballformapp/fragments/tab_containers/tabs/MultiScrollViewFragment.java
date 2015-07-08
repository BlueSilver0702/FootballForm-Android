package uk.co.createanet.footballformapp.fragments.tab_containers.tabs;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import uk.co.createanet.footballformapp.R;

/**
 * A fragment representing a list of Items.
 * <p />
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 */
public class MultiScrollViewFragment extends Fragment implements AbsListView.OnItemClickListener {

    private ListView leftListView, rightListView;
    private ListAdapter mAdapter;

    // TODO: Rename and change types of parameters
    public static MultiScrollViewFragment newInstance() {
        MultiScrollViewFragment fragment = new MultiScrollViewFragment();
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MultiScrollViewFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        mAdapter = new ArrayAdapter<DummyContent.DummyItem>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, DummyContent.ITEMS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_multiscroll, container, false);

        // Set the adapter
        leftListView = (ListView) view.findViewById(R.id.left_list);
        rightListView = (ListView) view.findViewById(R.id.right_list);

        leftListView.setAdapter(mAdapter);
        rightListView.setAdapter(mAdapter);


        leftListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            int previousVisibleItem = 0;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, final int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(leftListView.getChildAt(0) == null){
                    return;
                }

                final int amountPassedItem = leftListView.getChildAt(0).getTop() * -1;
                final int amountAbove = firstVisibleItem * leftListView.getChildAt(0).getMeasuredHeight();

                view.post(new Runnable() {
                              public void run() {
                                  rightListView.setScrollY(amountAbove + amountPassedItem);

                                  Log.d("FF", "Scrolled: " + (amountAbove + amountPassedItem));

                                  if(firstVisibleItem != previousVisibleItem){
                                      rightListView.scrollBy(0, 0);
                                      rightListView.invalidateViews();

                                      Log.d("FF", "REFRESH");
                                  }
                              }
                          });

                previousVisibleItem = firstVisibleItem;

            }
        });

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        /*
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                + " must implement OnFragmentInteractionListener");
        }
        */
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    }

    /**
    * This interface must be implemented by activities that contain this
    * fragment to allow an interaction in this fragment to be communicated
    * to the activity and potentially other fragments contained in that
    * activity.
    * <p>
    * See the Android Training lesson <a href=
    * "http://developer.android.com/training/basics/fragments/communicating.html"
    * >Communicating with Other Fragments</a> for more information.
    */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

}
