package com.hezhujun.shopping.view.activity;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hezhujun on 2017/7/13.
 * 管理活动
 */
public class ActivityCollector {
    private static List<Activity> activities = new ArrayList<>();

    public static void addActivity(Activity activity) {
        activities.add(activity);
    }

    public static void removeActivity(Activity activity) {
        activities.remove(activity);
    }

    /**
     * 结束所有的活动
     */
    public static void finishAll() {
        for (Activity activity :
                activities) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
    }
}
