package com.example.yoga_admin_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class ClassInstanceAdapter extends BaseAdapter {

    private Context context;
    private List<ClassInstance> instanceList;
    private LayoutInflater inflater;

    public ClassInstanceAdapter(Context context, List<ClassInstance> instanceList) {
        this.context = context;
        this.instanceList = instanceList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return instanceList.size();
    }

    @Override
    public Object getItem(int position) {
        return instanceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return instanceList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_class_instance, parent, false);
            holder = new ViewHolder();
            holder.tvDate = convertView.findViewById(R.id.tv_instance_date);
            holder.tvInstructor = convertView.findViewById(R.id.tv_instance_instructor);
            holder.tvComments = convertView.findViewById(R.id.tv_instance_comments);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ClassInstance instance = instanceList.get(position);

        holder.tvDate.setText(instance.getDate());
        holder.tvInstructor.setText("Instructor: " + instance.getInstructor());
        
        // Handle optional comments field
        if (instance.getAdditionalComments() != null && !instance.getAdditionalComments().isEmpty()) {
            holder.tvComments.setVisibility(View.VISIBLE);
            holder.tvComments.setText(instance.getAdditionalComments());
        } else {
            holder.tvComments.setVisibility(View.GONE);
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView tvDate;
        TextView tvInstructor;
        TextView tvComments;
    }

    // Update the list data
    public void updateData(List<ClassInstance> newInstanceList) {
        this.instanceList = newInstanceList;
        notifyDataSetChanged();
    }
}