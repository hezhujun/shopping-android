package com.hezhujun.shopping.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hezhujun.shopping.R;
import com.hezhujun.shopping.common.Const;
import com.hezhujun.shopping.common.HttpQueryBuilder;
import com.hezhujun.shopping.model.Order;
import com.hezhujun.shopping.model.Result;
import com.hezhujun.shopping.model.Role;
import com.hezhujun.shopping.model.User;

import org.json.JSONObject;

import java.io.IOException;

/**
 * 登录Activity
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";

    private EditText usernameView;
    private EditText passwordView;
    private Button loginBtn;

    /**
     * 启动此活动时调用
     * @param context
     * @return
     */
    public static Intent startActivity(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        initView();
    }

    /**
     * 页面控件初始化
     */
    private void initView() {
        usernameView = (EditText) findViewById(R.id.username);
        passwordView = (EditText) findViewById(R.id.password);
        loginBtn = (Button) findViewById(R.id.login);

        loginBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login:
                login();
                break;
            default:
                break;
        }
    }

    /**
     * 处理登录函数
     */
    private void login() {
        String username = usernameView.getText().toString();
        if ("".equals(username)) {
            Toast.makeText(this, "请输入账号", Toast.LENGTH_SHORT).show();
            return;
        }
        if (username.contains("'") || username.contains("\"")) {
            Toast.makeText(this, "账号包含特殊字符\"'\"或\"\"\"", Toast.LENGTH_SHORT).show();
            return;
        }
        String password = passwordView.getText().toString();
        if ("".equals(password)) {
            Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
            return;
        }
        /**
         * 防止重复点击
         */
        loginBtn.setOnClickListener(null);
        new LoginAsyncTask(username, password).execute();
    }

    /**
     * 处理登录请求的异步操作类
     */
    class LoginAsyncTask extends AsyncTask<Void, Void, String> {

        private String username;
        private String password;

        public LoginAsyncTask(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected String doInBackground(Void... params) {
            HttpQueryBuilder builder = new HttpQueryBuilder();
            builder.addHeader("Content-Type", "application/x-www-form-urlencoded");
            builder.addParam("username", username);
            builder.addParam("password", password);
            builder.url(Const.BASE_URL + "/user/login");
            builder.post();
            try {
                return builder.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.d(TAG, "task return: " + s);
            if (s == null) {
                Toast.makeText(LoginActivity.this, Const.ERROR_NETWORK, Toast.LENGTH_SHORT).show();
                loginBtn.setOnClickListener(LoginActivity.this);
                return;
            }
            try {
                JSONObject jsonObject = new JSONObject(s);
                String resultStr = jsonObject.getString("result");
                ObjectMapper objectMapper = new ObjectMapper();
                Result result = objectMapper.readValue(resultStr, Result.class);
                if (result.isSuccess()) {
                    String userStr = jsonObject.getString("user");
                    User user = objectMapper.readValue(userStr, User.class);
                    Log.d(TAG, "get user: " + user);
//                    if (user.getRole().getRoleName().equals(Role.USER_ROLE_COMMON)) {
//                        Intent intent = UserActivity.startActivity(LoginActivity.this, user);
//                        startActivity(intent);
//                    }
//                    else if (user.getRole().getRoleName().equals(Role.USER_ROLE_PRODUCER)) {
//                        Intent intent = SelectActivity.startActivity(LoginActivity.this, user);
//                        startActivity(intent);
//                    }
//                    else if (user.getRole().getRoleName().equals(Role.USER_ROLE_ADMIN)) {
//                        Intent intent = ManageUserActivity.startActivity(LoginActivity.this, user);
//                        startActivity(intent);
//                    }
                    Intent intent = UserActivity.startActivity(LoginActivity.this, user);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, result.getErr(), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            loginBtn.setOnClickListener(LoginActivity.this);
        }
    }
}
