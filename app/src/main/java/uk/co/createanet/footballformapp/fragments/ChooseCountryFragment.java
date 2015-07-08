package uk.co.createanet.footballformapp.fragments;


import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import uk.co.createanet.footballformapp.MainActivity;
import uk.co.createanet.footballformapp.R;
import uk.co.createanet.footballformapp.data.FFDatabase;
import uk.co.createanet.footballformapp.lib.CountrySelector;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChooseCountryFragment extends Fragment implements AdapterView.OnItemClickListener {

    public static final String KEY_TYPE = "type";
    public static final String KEY_CONTINENT = "continent";

    public static final int TYPE_CONTINENT = 1;
    public static final int TYPE_COUNTRY = 2;

    public int type;

    public String continentId;
    public String currentContinentId;
    public int countryId;

    private ListView list_items;

    public static ChooseCountryFragment newInstanceChooseContinent() {
        ChooseCountryFragment f = new ChooseCountryFragment();

        Bundle b = new Bundle();
        b.putInt(KEY_TYPE, TYPE_CONTINENT);
        f.setArguments(b);

        return f;
    }

    public static ChooseCountryFragment newInstanceChooseCountry(String continentId) {
        ChooseCountryFragment f = new ChooseCountryFragment();

        Bundle b = new Bundle();
        b.putInt(KEY_TYPE, TYPE_COUNTRY);
        b.putString(KEY_CONTINENT, continentId);
        f.setArguments(b);

        return f;
    }

    public ChooseCountryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_choose_country, container, false);

        currentContinentId = CountrySelector.getLastContinentName(getActivity(), ((MainActivity) getActivity()).getDatabase());
        countryId = CountrySelector.getLastCountry(getActivity());

        list_items = (ListView) v.findViewById(R.id.list_items);
        list_items.setOnItemClickListener(this);

        if (getArguments() != null) {
            type = getArguments().getInt(KEY_TYPE);
            continentId = getArguments().getString(KEY_CONTINENT);
        }

        Button btn_back = (Button) v.findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        FFDatabase.QueryListener l = new FFDatabase.QueryListener() {
            @Override
            public void onQueryComplete(Cursor c) {
                ClientCursorAdapter adapter = new ClientCursorAdapter(getActivity(), R.layout.row_country, c, 0);
                list_items.setAdapter(adapter);
            }
        };

        if (type == TYPE_CONTINENT) {
            ((MainActivity) getActivity()).getDatabase().getContinents(l);
        } else {
            ((MainActivity) getActivity()).getDatabase().getCountries(continentId, l);
        }

        return v;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String clickedName = (String) view.getTag();

        if (type == TYPE_CONTINENT) {
            ((MainActivity) getActivity()).mNavigationDrawerFragment.onContinentChosen(clickedName);
        } else {
            ((MainActivity) getActivity()).mNavigationDrawerFragment.onCountryChosen(id, clickedName);
        }

    }

    public class ClientCursorAdapter extends ResourceCursorAdapter {

        public ClientCursorAdapter(Context context, int layout, Cursor c, int flags) {
            super(context, layout, c, flags);
        }

        @Override
        public void bindView(View convertView, Context context, Cursor cursor) {

            if (convertView == null) {
                convertView = View.inflate(context, R.layout.row_country, null);
            }

            ImageView icon = (ImageView) convertView.findViewById(R.id.image_flag);
            ImageView tick = (ImageView) convertView.findViewById(R.id.image_tick);
            TextView text = (TextView) convertView.findViewById(R.id.text_title);

            text.setText(cursor.getString(1));

            if(type == TYPE_CONTINENT){
                icon.setVisibility(View.GONE);
            } else {
                icon.setImageDrawable(getResources().getDrawable(getFlagForCountry(cursor.getString(cursor.getColumnIndex("name")))));
                icon.setVisibility(View.VISIBLE);
            }

            if(type == TYPE_CONTINENT && currentContinentId != null && cursor.getString(cursor.getColumnIndex("continent")).equals(currentContinentId)) {
                tick.setVisibility(View.VISIBLE);
            } else if(type == TYPE_COUNTRY && cursor.getInt(cursor.getColumnIndex("_id")) == countryId){
                tick.setVisibility(View.VISIBLE);
            } else {
                tick.setVisibility(View.INVISIBLE);
            }

            convertView.setTag(cursor.getString(1));
            convertView.setId(cursor.getInt(0));

            /*
                if(mCurrentSelectedPosition == position){
                    row_item.setBackgroundColor(getResources().getColor(R.color.blue_button));
                } else {
                    row_item.setBackgroundColor(getResources().getColor(R.color.transparent));
                }
*/

//            return convertView;

        }
    }

    public int getFlagForCountry(String countryName){

        String flagName = "flag_" + countryName.toLowerCase().replace(" ", "_");

        int bracketIndex = flagName.indexOf('(');

        if(bracketIndex > -1) {
            flagName = flagName.substring(0, flagName.indexOf('(') - 1);
        }

        int resourceId = this.getResources().getIdentifier("drawable/" + flagName, null, getActivity().getPackageName());

        return resourceId != 0 ? resourceId : R.drawable.flag_unknown;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
