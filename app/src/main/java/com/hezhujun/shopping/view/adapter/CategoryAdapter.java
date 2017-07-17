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
import com.hezhujun.shopping.model.Category;

import java.util.List;

/**
 * Created by hezhujun on 2017/7/10.
 * 类别类别的适配器
 */
public class CategoryAdapter extends ArrayAdapter<Category> {

    private int resource;

    public CategoryAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Category> objects) {
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
            holder.textView = (TextView) view.findViewById(R.id.textView);
            view.setTag(holder);
        }
        else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }
        holder.textView.setText(getItem(position).getName());
        return view;
    }

    class ViewHolder {
        TextView textView;
    }
}
