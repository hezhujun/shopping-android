package com.hezhujun.shopping.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hezhujun.shopping.R;
import com.hezhujun.shopping.common.Const;
import com.hezhujun.shopping.common.HttpQueryBuilder;
import com.hezhujun.shopping.model.Result;
import com.hezhujun.shopping.model.Role;
import com.hezhujun.shopping.model.User;
import com.hezhujun.shopping.view.adapter.RoleAdapter;
import com.hezhujun.shopping.view.fragment.ToolBarFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 添加用户的活动
 */
public class ManageUserDetailActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "ManageUserDetailActivity";

    private ToolBarFragment toolBarFragment;
//    private View goBackView;
//    private TextView titleView;
    private EditText usernameView;
    private EditText passwordView;
    private EditText confirmPasswordView;
    private Spinner roleView;
    private View addUserBtn;
    private TextView resultView;

    private List<Role> roleList = new ArrayList<>();
    private RoleAdapter roleAdapter;
    private User me;

    /**
     * 启动此活动需要调用的函数
     * @param context
     * @param me
     * @return
     */
    public static Intent startActivity(Context context, User me) {
        Intent intent = new Intent(context, ManageUserDetailActivity.class);
        intent.putExtra("me", me);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_user_detail);

        Intent intent = getIntent();
        me = (User) intent.getSerializableExtra("me");
        if (me == null) {
            Toast.makeText(this, "user为空", Toast.LENGTH_SHORT).show();
        }

        initView();
        initData();
    }

    /**
     * 页面控件初始化
     */
    private void initView() {
//        goBackView = findViewById(R.id.go_back);
//        goBackView.setOnClickListener(this);
//        titleView = (TextView) findViewById(R.id.title);
//        titleView.setText("添加用户");
        toolBarFragment = (ToolBarFragment) getFragmentManager().findFragmentById(R.id.tool_bar);
        toolBarFragment.setTitle("添加用户");
        toolBarFragment.setUser(me);
        usernameView = (EditText) findViewById(R.id.username);
        passwordView = (EditText) findViewById(R.id.password);
        confirmPasswordView = (EditText) findViewById(R.id.confirm_password);
        roleView = (Spinner) findViewById(R.id.role);
        roleAdapter = new RoleAdapter(this, android.R.layout.simple_list_item_1, roleList);
        roleView.setAdapter(roleAdapter);
        addUserBtn = findViewById(R.id.add_user);
        addUserBtn.setOnClickListener(this);
        resultView = (TextView) findViewById(R.id.result);
    }

    /**
     * 数据初始化
     */
    private void initData() {
        new GetRoleListAsyncTask().execute();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.go_back:
                finish();
                break;
            case R.id.add_user:
                addUser();
                break;
            default:
                break;
        }
    }

    /**
     * 添加用户函数
     */
    private void addUser() {
        String username = usernameView.getText().toString().trim();
        if ("".equals(username)) {
            Toast.makeText(this, "请输入用户名", Toast.LENGTH_SHORT).show();
            return;
        }
        if (username.contains(",") || username.contains("\"")) {
            Toast.makeText(this, "用户名含有特殊字符'\"", Toast.LENGTH_SHORT).show();
            return;
        }
        String password = passwordView.getText().toString().trim();
        if ("".equals(password)) {
            Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
            return;
        }
        String confirmPassword = confirmPasswordView.getText().toString().trim();
        if ("".equals(confirmPassword)) {
            Toast.makeText(this, "请输入确认密码", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "密码前后不一致", Toast.LENGTH_SHORT).show();
            return;
        }
        Role role = roleList.get(roleView.getSelectedItemPosition());
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);

        addUserBtn.setOnClickListener(null);
        new AddUserAsyncTask(user).execute();
    }

    /**
     * 获取角色列表的异步操作类
     */
    class GetRoleListAsyncTask extends AsyncTask<Void, Void, List<Role>> {

        @Override
        protected List<Role> doInBackground(Void... params) {
            HttpQueryBuilder builder = new HttpQueryBuilder();
            builder.url(Const.BASE_URL + "/role/list").get();
            try {
                String json = builder.connect();
                JSONObject jsonObject = new JSONObject(json);
                String resultStr = jsonObject.getString("result");
                ObjectMapper objectMapper = new ObjectMapper();
                Result result = objectMapper.readValue(resultStr, Result.class);
                if (result.isSuccess()) {
                    String roleListStr = jsonObject.getString("roles");
                    List<Role> roleList = objectMapper.readValue(roleListStr,
                            new TypeReference<List<Role>>() {
                            });
                    return roleList;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Role> roles) {
            if (roles == null) {
                Toast.makeText(ManageUserDetailActivity.this, Const.ERROR_NETWORK, Toast.LENGTH_SHORT).show();
                return;
            }
            if (roles.size() > 0) {
                roleList.addAll(roles);
                roleAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * 添加用户的异步操作类别
     */
    class AddUserAsyncTask extends AsyncTask<Void, Void, Result> {

        private User user;

        public AddUserAsyncTask(User user) {
            this.user = user;
        }

        @Override
        protected Result doInBackground(Void... params) {
            HttpQueryBuilder builder = new HttpQueryBuilder();
            builder.post().url(Const.BASE_URL + "/user/save");
            builder.addParam("username", user.getUsername());
            builder.addParam("password", user.getPassword());
            builder.addParam("roleId", String.valueOf(user.getRole().getId()));
            try {
                String json = builder.connect();
                JSONObject jsonObject = new JSONObject(json);
                String resultStr = jsonObject.getString("result");
                ObjectMapper objectMapper = new ObjectMapper();
                Result result = objectMapper.readValue(resultStr, Result.class);
                return result;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Result result) {
            if (result == null) {
                Toast.makeText(ManageUserDetailActivity.this, Const.ERROR_NETWORK, Toast.LENGTH_SHORT).show();
                addUserBtn.setOnClickListener(ManageUserDetailActivity.this);
                return;
            }
            if (result.isSuccess()) {
                Toast.makeText(ManageUserDetailActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                resultView.setText("添加成功");
            } else {
                Toast.makeText(ManageUserDetailActivity.this, "添加失败\n" + result.getErr(),
                        Toast.LENGTH_SHORT).show();
            }
            addUserBtn.setOnClickListener(ManageUserDetailActivity.this);
        }
    }
}
