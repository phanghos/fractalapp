package org.taitascioredev.adapters;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by roberto on 04/11/15.
 */
public class CustomSpinnerAdapter extends ArrayAdapter<String> {

    private final AppCompatActivity context;
    private final String[] objects;

    public CustomSpinnerAdapter(AppCompatActivity context, int resource, String[] objects) {
        super(context, resource, objects);
        this.context = context;
        this.objects = objects;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int selectedItemPosition = position;
        if (parent instanceof AdapterView) {
            selectedItemPosition = ((AdapterView) parent)
                    .getSelectedItemPosition();
        } else if (parent instanceof AppCompatSpinner) {
            selectedItemPosition = ((AppCompatSpinner) parent)
                    .getSelectedItemPosition();
        }
        return getCustomView(selectedItemPosition, convertView, parent);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        TextView label= (TextView) row.findViewById(android.R.id.text1);
        label.setText(objects[position]);
        return row;
    }
}
