package uk.co.createanet.footballformapp.lib;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import uk.co.createanet.footballformapp.R;

/**
 * Created by matthew on 07/08/2014.
 */
public class NoResultsAdapter extends ArrayAdapter<String> {

    private final Context context;
    private String message;

    public NoResultsAdapter(Context context, String message) {
        super(context, R.layout.row_no_results, new String[]{ "1" });
        this.context = context;
        this.message = message;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.row_no_results, parent, false);

        TextView edit_message = (TextView)rowView.findViewById(R.id.edit_message);
        edit_message.setText(message);

        return rowView;
    }
}