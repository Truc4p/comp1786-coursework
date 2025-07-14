package com.example.yoga_admin_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

public class CustomSpinnerAdapter extends BaseAdapter {

    private Context context;
    private String[] items;
    private LayoutInflater inflater;

    public CustomSpinnerAdapter(Context context, String[] items) {
        this.context = context;
        this.items = items;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public Object getItem(int position) {
        return items[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.spinner_item_custom, parent, false);
        }

        TextView textView = convertView.findViewById(R.id.spinner_text);
        textView.setText(items[position]);

        // First item (placeholder) in light_purple_2, others in medium_purple
        if (position == 0) {
            textView.setTextColor(ContextCompat.getColor(context, R.color.light_purple_2));
        } else {
            textView.setTextColor(ContextCompat.getColor(context, R.color.medium_purple));
        }

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.spinner_dropdown_item_custom, parent, false);
        }

        TextView textView = convertView.findViewById(R.id.spinner_dropdown_text);
        textView.setText(items[position]);

        // First item (placeholder) in light_purple_2, others in medium_purple
        if (position == 0) {
            textView.setTextColor(ContextCompat.getColor(context, R.color.light_purple_2));
        } else {
            textView.setTextColor(ContextCompat.getColor(context, R.color.medium_purple));
        }

        return convertView;
    }

    @Override
    public boolean isEnabled(int position) {
        // Disable the first item (placeholder) so it can't be selected
        return position != 0;
    }
} 