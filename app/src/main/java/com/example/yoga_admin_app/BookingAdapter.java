package com.example.yoga_admin_app;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class BookingAdapter extends BaseAdapter {

    private Context context;
    private List<Booking> bookingList;
    private LayoutInflater inflater;

    public BookingAdapter(Context context, List<Booking> bookingList) {
        this.context = context;
        this.bookingList = bookingList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return bookingList.size();
    }

    @Override
    public Object getItem(int position) {
        return bookingList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return bookingList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_booking, parent, false);
            holder = new ViewHolder();
            holder.tvCustomerName = convertView.findViewById(R.id.tv_customer_name);
            holder.tvBookingId = convertView.findViewById(R.id.tv_booking_id);
            holder.tvClassName = convertView.findViewById(R.id.tv_class_name);
            holder.tvBookingDateTime = convertView.findViewById(R.id.tv_booking_datetime);
            holder.tvStatus = convertView.findViewById(R.id.tv_status);
            holder.tvPaymentInfo = convertView.findViewById(R.id.tv_payment_info);
            holder.tvContactInfo = convertView.findViewById(R.id.tv_contact_info);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Booking booking = bookingList.get(position);

        // Set customer name
        holder.tvCustomerName.setText(booking.getCustomerName());

        // Set booking ID
        holder.tvBookingId.setText("ID: " + booking.getBookingId());

        // Set class name - show count if multiple classes
        String classDisplay;
        List<String> allClasses = booking.getAllClassNames();
        if (allClasses != null && allClasses.size() > 1) {
            classDisplay = allClasses.get(0) + " (+" + (allClasses.size() - 1) + " more)";
        } else if (allClasses != null && allClasses.size() == 1) {
            classDisplay = allClasses.get(0);
        } else {
            classDisplay = booking.getClassName() != null ? booking.getClassName() : "No class";
        }
        holder.tvClassName.setText(classDisplay);

        // Set booking date and time
        String dateTime = booking.getBookingDate() + " at " + booking.getBookingTime();
        holder.tvBookingDateTime.setText(dateTime);

        // Set status with color coding
        holder.tvStatus.setText(booking.getStatus().toUpperCase());
        setStatusColor(holder.tvStatus, booking.getStatus());

        // Set payment information
        String paymentInfo = String.format("£%.2f - %s", 
            booking.getPaymentAmount(), 
            booking.getPaymentStatus().toUpperCase());
        holder.tvPaymentInfo.setText(paymentInfo);

        // Set contact information (email and phone only, not name)
        String contactInfo = booking.getCustomerEmail();
        if (booking.getCustomerPhone() != null && !booking.getCustomerPhone().trim().isEmpty()) {
            contactInfo += " • " + booking.getCustomerPhone();
        }
        holder.tvContactInfo.setText(contactInfo);

        return convertView;
    }

    private void setStatusColor(TextView statusTextView, String status) {
        switch (status.toLowerCase()) {
            case "confirmed":
                statusTextView.setTextColor(Color.parseColor("#4CAF50")); // Green
                break;
            case "pending":
                statusTextView.setTextColor(Color.parseColor("#FF9800")); // Orange
                break;
            case "cancelled":
                statusTextView.setTextColor(Color.parseColor("#F44336")); // Red
                break;
            case "completed":
                statusTextView.setTextColor(Color.parseColor("#2196F3")); // Blue
                break;
            default:
                statusTextView.setTextColor(Color.parseColor("#757575")); // Gray
                break;
        }
    }

    static class ViewHolder {
        TextView tvCustomerName;
        TextView tvBookingId;
        TextView tvClassName;
        TextView tvBookingDateTime;
        TextView tvStatus;
        TextView tvPaymentInfo;
        TextView tvContactInfo;
    }
}
