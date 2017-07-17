package com.hezhujun.shopping.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hezhujun.shopping.R;
import com.hezhujun.shopping.model.Role;
import com.hezhujun.shopping.model.User;
import com.hezhujun.shopping.view.fragment.ToolBarFragment;

/**
 * 用户信息页面
 */
public class UserActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "UserActivity";

    private ToolBarFragment toolBarFragment;
    private TextView usernameView;
    private TextView nameView;
    private TextView phoneView;
    private TextView addressView;
    private Button updateUserBtn;
    private Button updatePasswordBtn;
    private Button lookOrderBtn;
    private Button lookProductBtn;

    private View manageButtonView;
    private View manageUserButtonView;
    private Button logoutButton;

    private User user;

    /**
     * 启动活动需要调用的函数
     * @param context
     * @param user
     * @return
     */
    public static Intent startActivity(Context context, User user) {
        Intent intent = new Intent(context, UserActivity.class);
        intent.putExtra("user", user);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        Intent intent = getIntent();
        user = (User) intent.getSerializableExtra("user");
        if (user == null) {
            Toast.makeText(this, "user为null", Toast.LENGTH_LONG).show();
            System.exit(1);
        }

        initView();
        initData();
    }

    /**
     * 初始化页面控件
     */
    private void initView() {
        toolBarFragment = (ToolBarFragment) getFragmentManager().findFragmentById(R.id.tool_bar);
        toolBarFragment.setGoBackViewVisibility(View.INVISIBLE);
        toolBarFragment.setHomeViewVisibility(View.INVISIBLE);
        toolBarFragment.setTitle("用户信息");
        usernameView = (TextView) findViewById(R.id.username);
        nameView = (TextView) findViewById(R.id.name);
        phoneView = (TextView) findViewById(R.id.phone);
        addressView = (TextView) findViewById(R.id.address);
        updateUserBtn = (Button) findViewById(R.id.update_user_btn);
        updatePasswordBtn = (Button) findViewById(R.id.update_password_btn);
        lookOrderBtn = (Button) findViewById(R.id.look_order);
        lookProductBtn = (Button) findViewById(R.id.look_product);

        updateUserBtn.setOnClickListener(this);
        updatePasswordBtn.setOnClickListener(this);
        lookOrderBtn.setOnClickListener(this);
        lookProductBtn.setOnClickListener(this);

        manageButtonView = findViewById(R.id.manage_buttons);
        manageUserButtonView = findViewById(R.id.manage_user_button);
        if (user.getRole().getRoleName().equals(Role.USER_ROLE_PRODUCER)) {
            manageButtonView.setVisibility(View.VISIBLE);
            Button orderManageButton = (Button) findViewById(R.id.order_manage);
            orderManageButton.setOnClickListener(this);
            Button productManageButton = (Button) findViewById(R.id.product_manage);
            productManageButton.setOnClickListener(this);
        } else if (user.getRole().getRoleName().equals(Role.USER_ROLE_ADMIN)) {
            manageUserButtonView.setVisibility(View.VISIBLE);
            Button userManageButton = (Button) findViewById(R.id.user_manage);
            userManageButton.setOnClickListener(this);
        }

        logoutButton = (Button) findViewById(R.id.logout);
        logoutButton.setOnClickListener(this);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        usernameView.setText(user.getUsername());
        if (user.getName() == null || "".equals(user.getName())) {
            nameView.setText("未设置");
        }
        else {
            nameView.setText(user.getName());
        }
        if (user.getPhone() == null || "".equals(user.getPhone())) {
            phoneView.setText("未设置");
        }
        else {
            phoneView.setText(user.getPhone());
        }
        if (user.getAddress() == null || "".equals(user.getAddress())) {
            addressView.setText("未设置");
        }
        else {
            addressView.setText(user.getAddress());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.update_user_btn:
                Intent updateUserIntent = UpdateUserActivity.startActivity(this, user);
                startActivityForResult(updateUserIntent, UpdateUserActivity.UPDATE);
                break;
            case R.id.update_password_btn:
                Intent updatePasswordIntent = UpdatePasswordActivity.startActivity(this, user);
                startActivityForResult(updatePasswordIntent, UpdatePasswordActivity.UPDATE);
                break;
            case R.id.look_order:
                Intent intent1 = OrderListActivity.startActivity(this, user);
                startActivity(intent1);
                break;
            case R.id.look_product:
                Intent intent2 = CategoryActivity.startActivity(this, user);
                startActivity(intent2);
                break;
            case R.id.order_manage:
                Intent intent3 = ManageOrderActivity.startActivity(this, user);
                startActivity(intent3);
                break;
            case R.id.product_manage:
                Intent intent4 = ManageCategoryActivity.startActivity(this, user);
                startActivity(intent4);
                break;
            case R.id.user_manage:
                Intent intent5 = ManageUserActivity.startActivity(this, user);
                startActivity(intent5);
                break;
            case R.id.logout:
                Intent logoutIntent = LoginActivity.startActivity(this);
                startActivity(logoutIntent);
                finish();
            default:
                break;
        }
    }

    /**
     * 获取更新用户信息后的用户信息
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 1) {
            User u = (User) data.getSerializableExtra("user");
            user = u;
            initData();
        }
    }
}
