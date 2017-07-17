package com.hezhujun.shopping.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hezhujun.shopping.R;
import com.hezhujun.shopping.common.Const;
import com.hezhujun.shopping.common.HttpQueryBuilder;
import com.hezhujun.shopping.common.MD5Coder;
import com.hezhujun.shopping.model.PageBean;
import com.hezhujun.shopping.model.Result;
import com.hezhujun.shopping.model.User;
import com.hezhujun.shopping.view.fragment.ToolBarFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * 更新密码页面
 */
public class UpdatePasswordActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "UpdatePasswordActivity";
    public static final int UPDATE = 1;

    private ToolBarFragment toolBarFragment;
    private EditText oldPasswordView;
    private EditText newPasswordView;
    private EditText newPasswordConfirmView;
    private Button updatePasswordBtn;
    private TextView resultView;

    private User user;

    public static Intent startActivity(Context context, User user) {
        Intent intent = new Intent(context, UpdatePasswordActivity.class);
        intent.putExtra("user", user);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password);

        Intent intent = getIntent();
        user = (User) intent.getSerializableExtra("user");
        if (user == null) {
            Toast.makeText(this, "user为null", Toast.LENGTH_SHORT).show();
            System.exit(1);
        }

        initView();
    }

    private void initView() {
        toolBarFragment = (ToolBarFragment) getFragmentManager().findFragmentById(R.id.tool_bar);
        toolBarFragment.setTitle("修改密码");
        toolBarFragment.setUser(user);
        oldPasswordView = (EditText) findViewById(R.id.old_password);
        newPasswordView = (EditText) findViewById(R.id.new_password);
        newPasswordConfirmView = (EditText) findViewById(R.id.new_password_confirm);
        updatePasswordBtn = (Button) findViewById(R.id.update_password_btn);
        updatePasswordBtn.setOnClickListener(this);
        resultView = (TextView) findViewById(R.id.result);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.update_password_btn:
                updatePassword();
                break;
        }
    }

    private void updatePassword() {
        String oldPassword = oldPasswordView.getText().toString();
        if ("".equals(oldPassword)) {
            Toast.makeText(this, "请输入旧密码", Toast.LENGTH_SHORT).show();
            return;
        }
        String newPassword = newPasswordView.getText().toString();
        if ("".equals(newPassword)) {
            Toast.makeText(this, "请输入新密码", Toast.LENGTH_SHORT).show();
            return;
        }
        String newPasswordConfirm = newPasswordConfirmView.getText().toString();
        if ("".equals(newPasswordConfirm)) {
            Toast.makeText(this, "请输入确认密码", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newPassword.equals(newPasswordConfirm)) {
            Toast.makeText(this, "新密码不匹配", Toast.LENGTH_SHORT).show();
            return;
        }
        new UpdatePasswordAsyncTask(user.getId(), oldPassword, newPassword).execute();
        updatePasswordBtn.setOnClickListener(null);
        resultView.setText("");
    }

    class UpdatePasswordAsyncTask extends AsyncTask<Void, Void, String> {

        private Integer userId;
        private String oldPassword;
        private String newPassword;

        public UpdatePasswordAsyncTask(Integer userId, String oldPassword, String newPassword) {
            this.userId = userId;
            this.oldPassword = oldPassword;
            this.newPassword = newPassword;
        }

        @Override
        protected String doInBackground(Void... params) {
            HttpQueryBuilder builder = new HttpQueryBuilder();
            builder.addHeader("Content-Type", "application/x-www-form-urlencoded");
            builder.addParam("userId", String.valueOf(userId));
            builder.addParam("oldPassword", oldPassword);
            builder.addParam("newPassword", newPassword);
            builder.post();
            builder.url(Const.BASE_URL + "/user/update_password");
            try {
                return builder.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.d(TAG, "update password return: " + s);
            if (s == null) {
                Toast.makeText(UpdatePasswordActivity.this, Const.ERROR_NETWORK, Toast.LENGTH_SHORT).show();
                updatePasswordBtn.setOnClickListener(UpdatePasswordActivity.this);
                return;
            }
            try {
                JSONObject jsonObject = new JSONObject(s);
                String resultStr = jsonObject.getString("result");
                ObjectMapper objectMapper = new ObjectMapper();
                Result result = objectMapper.readValue(resultStr, Result.class);
                if (result.isSuccess()) {
                    Toast.makeText(UpdatePasswordActivity.this, "密码更改成功", Toast.LENGTH_SHORT).show();
                    user.setPassword(MD5Coder.encode(newPassword));
                    oldPasswordView.setText("");
                    newPasswordView.setText("");
                    newPasswordConfirmView.setText("");
                    resultView.setText("密码更改成功");
                } else {
                    Toast.makeText(UpdatePasswordActivity.this, "密码更改失败", Toast.LENGTH_SHORT).show();
                    resultView.setText("密码更改失败");
                }
            } catch (JsonParseException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            updatePasswordBtn.setOnClickListener(UpdatePasswordActivity.this);
        }
    }
}
