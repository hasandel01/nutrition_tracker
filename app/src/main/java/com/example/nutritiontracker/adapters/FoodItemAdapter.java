package com.example.nutritiontracker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.nutritiontracker.R;
import com.example.nutritiontracker.models.FoodItem;

import java.util.List;

public class FoodItemAdapter extends ArrayAdapter<FoodItem> {

    private LayoutInflater inflater;
    private List<FoodItem> foodItemList;
    private int resource;

    public FoodItemAdapter(Context context, int resource, List<FoodItem> foodItemList) {
        super(context, resource, foodItemList);
        this.resource = resource;
        this.foodItemList = foodItemList;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = inflater.inflate(resource, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.foodNameTextView = convertView.findViewById(R.id.foodNameTextView);
            viewHolder.caloriesTextView = convertView.findViewById(R.id.caloriesTextView);
            viewHolder.countTextView = convertView.findViewById(R.id.countTextView);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        FoodItem foodItem = foodItemList.get(position);
        viewHolder.foodNameTextView.setText(foodItem.getName());
        viewHolder.caloriesTextView.setText("calories (100gr): " + foodItem.getCalories());
        viewHolder.countTextView.setText("total grams: " + foodItem.getCount());

        return convertView;
    }

    static class ViewHolder {
        TextView foodNameTextView;
        TextView caloriesTextView;
        TextView countTextView;
    }
}
