package com.hezhujun.shopping.view.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.view.MenuItem;
import android.widget.Toast;

import com.hezhujun.shopping.R;
import com.hezhujun.shopping.model.User;
import com.hezhujun.shopping.view.fragment.OrderDoneFragment;
import com.hezhujun.shopping.view.fragment.OrderTodoFragment;

/**
 * 产品销售商处理订单的活动
 */
public class ManageOrderActivity extends BaseActivity {

    private Fragment orderDoneFragment;
    private Fragment orderTodoFragment;
    private FragmentManager fragmentManager;

    private User user;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            showFragment(item.getItemId());
            return true;
        }

    };

    /**
     * 启动此活动需要调用的函数
     * @param context
     * @param user
     * @return
     */
    public static Intent startActivity(Context context, User user) {
        Intent intent = new Intent(context, ManageOrderActivity.class);
        intent.putExtra("user", user);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_order);

        Intent intent = getIntent();
        user = (User) intent.getSerializableExtra("user");
        if (user == null) {
            Toast.makeText(this, "user为null", Toast.LENGTH_SHORT).show();
            System.exit(1);
        }

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        fragmentManager = getFragmentManager();
        showFragment(R.id.todo);
    }

    /**
     * 更加id显示Fragment
     * @param id
     */
    private void showFragment(int id) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        hideFragment(fragmentTransaction);
        switch (id) {
            case R.id.todo:
                if (orderTodoFragment == null) {
                    orderTodoFragment = OrderTodoFragment.newInstance(user);
                    fragmentTransaction.add(R.id.content, orderTodoFragment);
                } else {
                    fragmentTransaction.show(orderTodoFragment);
                }
                break;
            case R.id.done:
                if (orderDoneFragment == null) {
                    orderDoneFragment = OrderDoneFragment.newInstance(user);
                    fragmentTransaction.add(R.id.content, orderDoneFragment);
                } else {
                    fragmentTransaction.show(orderDoneFragment);
                }
                break;
            default:
                break;
        }
        fragmentTransaction.commit();
    }

    /**
     * 隐藏所有的Fragment
     * @param fragmentTransaction
     */
    private void hideFragment(FragmentTransaction fragmentTransaction) {
        if (orderDoneFragment != null) {
            fragmentTransaction.hide(orderDoneFragment);
        }
        if (orderTodoFragment != null) {
            fragmentTransaction.hide(orderTodoFragment);
        }
    }
}
