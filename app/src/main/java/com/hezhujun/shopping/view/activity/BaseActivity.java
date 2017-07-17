package com.hezhujun.shopping.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by hezhujun on 2017/7/13.
 * <p>
 * 项目所有的 Activity 继承 BaseActivity
 * BaseActivity 负责管理项目的 Activity
 */
public class BaseActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }

    /**
     * 注销，跳到登录界面
     */
    public void logout() {
        ActivityCollector.finishAll();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

}
