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
import com.hezhujun.shopping.model.User;

import java.util.List;

/**
 * Created by hezhujun on 2017/7/13.
 * 用户列表适配器
 */
public class UserAdapter extends ArrayAdapter<User> {
    private int resource;
    private DeleteUserListener deleteUserListener;

    public UserAdapter(@NonNull Context context, @LayoutRes int resource,
                       @NonNull List<User> objects, DeleteUserListener deleteUserListener) {
        super(context, resource, objects);
        this.resource = resource;
        this.deleteUserListener = deleteUserListener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable final View convertView, @NonNull ViewGroup parent) {
        View view;
        ViewHolder holder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resource, null);
            holder = new ViewHolder();
            holder.usernameView = (TextView) view.findViewById(R.id.username);
            holder.userRoleView = (TextView) view.findViewById(R.id.user_role);
            holder.deleteBtn = view.findViewById(R.id.delete);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }
        final User user = getItem(position);
        holder.usernameView.setText(user.getUsername());
        holder.userRoleView.setText(user.getRole().getRoleName());
        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteUserListener.delete(user);
            }
        });
        return view;
    }

    class ViewHolder {
        TextView usernameView;
        TextView userRoleView;
        View deleteBtn;
    }

    public interface DeleteUserListener {
        void delete(User user);
    }
}
