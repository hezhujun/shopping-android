package com.hezhujun.shopping.view.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.hezhujun.shopping.R;
import com.hezhujun.shopping.model.Order;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by hezhujun on 2017/7/11.
 * 订单列表的适配器
 */
public class OrderAdapter extends ArrayAdapter<Order> {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private int resource;

    public OrderAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Order> objects) {
        super(context, resource, objects);
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        ViewHolder holder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resource, null);
            holder = new ViewHolder();
            holder.orderIdView = (TextView) view.findViewById(R.id.order_id);
            holder.orderStateView = (TextView) view.findViewById(R.id.order_state);
            holder.orderProductNameView = (TextView) view.findViewById(R.id.order_product_name);
            holder.orderTimeView = (TextView) view.findViewById(R.id.order_time);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }
        Order order = getItem(position);
        holder.orderIdView.setText(String.valueOf(order.getId()));
        holder.orderStateView.setText(order.getState());
        holder.orderProductNameView.setText(order.getProduct().getName());
        holder.orderTimeView.setText(sdf.format(order.getTime()));
        return view;
    }

    class ViewHolder {
        TextView orderIdView;
        TextView orderStateView;
        TextView orderProductNameView;
        TextView orderTimeView;
    }
}
