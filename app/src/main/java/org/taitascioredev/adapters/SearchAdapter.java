package org.taitascioredev.adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.taitascioredev.fractal.R;

import java.util.List;

/**
 * Created by roberto on 28/04/15.
 */
public class SearchAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final int resource;
    private final List<String> objects;

    public SearchAdapter(Activity context, int resource, List<String> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.objects = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = context.getLayoutInflater().inflate(resource, parent, false);
        TextView tv = (TextView) rowView.findViewById(R.id.text);
        tv.setText("/r/" + objects.get(position));
        return rowView;
    }
}
