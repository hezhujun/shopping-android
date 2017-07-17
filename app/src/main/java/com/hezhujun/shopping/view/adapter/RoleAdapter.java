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

import com.hezhujun.shopping.model.Role;

import java.util.List;

/**
 * Created by hezhujun on 2017/7/13.
 * 角色列表的适配器
 */
public class RoleAdapter extends ArrayAdapter<Role> {
    private int resource;
    public RoleAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Role> objects) {
        super(context, resource, objects);
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        TextView text;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resource, null);
        } else {
            view = convertView;
        }
        text = (TextView) view;
        text.setText(getItem(position).getRoleName());
        return view;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}
