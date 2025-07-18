package com.example.yoga_admin_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class YogaClassAdapter extends BaseAdapter {

    private Context context;
    private List<YogaClass> yogaClassList;
    private LayoutInflater inflater;

    public YogaClassAdapter(Context context, List<YogaClass> yogaClassList) {
        this.context = context;
        this.yogaClassList = yogaClassList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return yogaClassList.size();
    }

    @Override
    public Object getItem(int position) {
        return yogaClassList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return yogaClassList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_yoga_class, parent, false);
            holder = new ViewHolder();
            holder.tvClassType = convertView.findViewById(R.id.tv_class_type);
            holder.tvDayAndTime = convertView.findViewById(R.id.tv_day_and_time);
            holder.tvCapacityAndPrice = convertView.findViewById(R.id.tv_capacity_and_price);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        YogaClass yogaClass = yogaClassList.get(position);

        holder.tvClassType.setText(yogaClass.getClassType());
        holder.tvDayAndTime.setText(yogaClass.getDayOfWeek() + " at " + yogaClass.getTime());
        holder.tvCapacityAndPrice.setText(yogaClass.getCapacity() + " people • £" + 
                String.format("%.2f", yogaClass.getPrice()));

        return convertView;
    }

    static class ViewHolder {
        TextView tvClassType;
        TextView tvDayAndTime;
        TextView tvCapacityAndPrice;
    }
} 