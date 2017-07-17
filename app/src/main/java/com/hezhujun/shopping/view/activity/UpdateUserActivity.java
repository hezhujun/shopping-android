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
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hezhujun.shopping.R;
import com.hezhujun.shopping.common.Const;
import com.hezhujun.shopping.common.HttpUtil;
import com.hezhujun.shopping.model.Result;
import com.hezhujun.shopping.model.User;
import com.hezhujun.shopping.view.fragment.ToolBarFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 更新用户信息页面
 */
public class UpdateUserActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "UpdateUserActivity";

    public static final int NO_UPDATE = 0;
    public static final int UPDATE = 1;

    private ToolBarFragment toolBarFragment;
    private EditText nameView;
    private EditText phoneView;
    private EditText addressView;
    private Button updateUserBtn;
    private TextView resultView;

    private User user;

    /**
     * 启动活动需要调用的函数
     * @param context
     * @param user
     * @return
     */
    public static Intent startActivity(Context context, User user) {
        Intent intent = new Intent(context, UpdateUserActivity.class);
        intent.putExtra("user", user);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_updata_user);

        Intent intent = getIntent();
        user = (User) intent.getSerializableExtra("user");
        if (user == null) {
            Toast.makeText(this, "user为null", Toast.LENGTH_SHORT).show();
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
        toolBarFragment.setTitle("修改信息");
        toolBarFragment.setUser(user);
        /**
         * 改Activity需要把更新的用户信息返回，直接按toolbar的HOME键
         * 不能返回更新的信息
         */
        toolBarFragment.getHomeView().setOnClickListener(this);
        nameView = (EditText) findViewById(R.id.name);
        phoneView = (EditText) findViewById(R.id.phone);
        addressView = (EditText) findViewById(R.id.address);
        updateUserBtn = (Button) findViewById(R.id.update_user_btn);
        updateUserBtn.setOnClickListener(this);
        resultView = (TextView) findViewById(R.id.result);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        if (user.getName() != null) {
            nameView.setText(user.getName());
        }
        if (user.getPhone() != null) {
            phoneView.setText(user.getPhone());
        }
        if (user.getAddress() != null) {
            addressView.setText(user.getAddress());
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.update_user_btn:
                updateUser();
                break;
            case R.id.home:
                finish();
            default:
                break;
        }
    }

    /**
     * 更新用户
     */
    private void updateUser() {
        String name = nameView.getText().toString().trim();
        String phone = phoneView.getText().toString().trim();
        String address = addressView.getText().toString().trim();
        if (name.equals(user.getName())
                && phone.equals(user.getPhone())
                && address.equals(user.getAddress())) {
            Toast.makeText(this, "数据没有发生改变", Toast.LENGTH_SHORT).show();
        } else {
            new UpdateUserAsyncTask(name, phone, address).execute();
            updateUserBtn.setOnClickListener(null);
            resultView.setText("");
        }
    }

    @Override
    public void finish() {
        Intent intent = new Intent();
        /**
         * 需要返回更新后的用户对象
         */
        intent.putExtra("user", user);
        setResult(UPDATE, intent);
        super.finish();
    }

    /**
     * 发送更新用户信息请求的异步操作类
     */
    class UpdateUserAsyncTask extends AsyncTask<Void, Void, String> {

        private String name;
        private String phone;
        private String address;

        public UpdateUserAsyncTask(String name, String phone, String address) {
            this.name = name;
            this.phone = phone;
            this.address = address;
        }

        @Override
        protected String doInBackground(Void... params) {
            HttpUtil.Holder holder = new HttpUtil.Holder() {
                @Override
                public void dealWithOutputStream(OutputStream outputStream) throws IOException {
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        User u = (User) user.clone();
                        u.setName(name);
                        u.setPhone(phone);
                        u.setAddress(address);
                        objectMapper.writeValue(outputStream, u);
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }

                }
            };
            HttpUtil.Header header = new HttpUtil.Header();
            header.put("Content-Type", "application/json");
            try {
                InputStream is = HttpUtil.execute(new URL(Const.BASE_URL + "/user/update"), header, "POST", holder);
                String json = HttpUtil.getContent(is);
                return json;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.d(TAG, "update user task return: " + s);
            if (s == null) {
                Toast.makeText(UpdateUserActivity.this, Const.ERROR_NETWORK, Toast.LENGTH_SHORT).show();
                updateUserBtn.setOnClickListener(UpdateUserActivity.this);
                return;
            }
            try {
                JSONObject jsonObject = new JSONObject(s);
                String resultStr = jsonObject.getString("result");
                ObjectMapper objectMapper = new ObjectMapper();
                Result result = objectMapper.readValue(resultStr, Result.class);
                if (result.isSuccess()) {
                    String userStr = jsonObject.getString("user");
                    user = objectMapper.readValue(userStr, User.class);
                    Toast.makeText(UpdateUserActivity.this, "更新信息成功", Toast.LENGTH_SHORT).show();
                    updateUserBtn.setOnClickListener(UpdateUserActivity.this);
                    resultView.setText("更新信息成功");
                    toolBarFragment.setUser(user);
                    return;
                }
            } catch (JsonParseException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Toast.makeText(UpdateUserActivity.this, "更新信息失败", Toast.LENGTH_SHORT).show();
            resultView.setText("更新信息失败");
            updateUserBtn.setOnClickListener(UpdateUserActivity.this);
        }
    }
}
