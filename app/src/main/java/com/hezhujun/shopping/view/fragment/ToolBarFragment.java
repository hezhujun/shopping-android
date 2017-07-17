package com.hezhujun.shopping.view.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hezhujun.shopping.R;
import com.hezhujun.shopping.model.User;
import com.hezhujun.shopping.view.activity.BaseActivity;
import com.hezhujun.shopping.view.activity.UserActivity;

/**
 * Created by hezhujun on 2017/7/13.
 * 封装页面的toolbar，需要获取用户对象
 * 用户对象用来返回用户信息页面
 */
public class ToolBarFragment extends Fragment implements View.OnClickListener {

    private View goBackView;
    private TextView titleView;
    private View homeView;
    private User user;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tool_bar, container);
        goBackView = view.findViewById(R.id.go_back);
        goBackView.setOnClickListener(this);
        titleView = (TextView) view.findViewById(R.id.title);
        homeView = view.findViewById(R.id.home);
        homeView.setOnClickListener(this);
        return view;
    }

    /**
     * 是否显示返回图标
     * @param visibility
     */
    public void setGoBackViewVisibility(int visibility) {
        goBackView.setVisibility(visibility);
    }

    /**
     * 是否显示home图标
     * @param visibility
     */
    public void setHomeViewVisibility(int visibility) {
        homeView.setVisibility(visibility);
    }

    /**
     * 设置页面标题
     * @param title
     */
    public void setTitle(String title) {
        titleView.setText(title);
    }

    /**
     * 设置用户对象
     * @param user
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * 获取home的view
     * @return
     */
    public View getHomeView() {
        return homeView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.go_back:
                getActivity().finish();
                break;
            case R.id.home:
                Intent intent = UserActivity.startActivity(getActivity(), user);
                startActivity(intent);
                break;
        }
    }
}
