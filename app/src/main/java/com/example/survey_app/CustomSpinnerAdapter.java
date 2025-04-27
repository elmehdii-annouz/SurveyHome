package com.example.survey_app;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class CustomSpinnerAdapter extends ArrayAdapter<String> {

    private final LayoutInflater inflater;
    private final Typeface customFont;

    public CustomSpinnerAdapter(Context context, int resource, List<String> objects) {
        super(context, resource, objects);
        inflater = LayoutInflater.from(context);
        customFont = Typeface.createFromAsset(context.getAssets(), "fonts/roboto.ttf");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
        }
        TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
        if (textView != null) {
            textView.setText(getItem(position));
            textView.setTextColor(Color.BLACK); // Set text color to black
            textView.setTypeface(customFont); // Set custom font
        }
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.spinner_item, parent, false);
        }
        TextView textView = (TextView) convertView.findViewById(R.id.spinner_item_text);
        if (textView != null) {
            textView.setText(getItem(position));
            textView.setTextColor(Color.BLACK); // Set text color to black
            textView.setTypeface(customFont); // Set custom font
        }
        return convertView;
    }
}
