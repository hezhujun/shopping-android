package com.hezhujun.shopping.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hezhujun.shopping.R;
import com.hezhujun.shopping.common.Const;
import com.hezhujun.shopping.common.HttpQueryBuilder;
import com.hezhujun.shopping.model.PageBean;
import com.hezhujun.shopping.model.Result;
import com.hezhujun.shopping.model.User;
import com.hezhujun.shopping.view.adapter.UserAdapter;
import com.hezhujun.shopping.view.fragment.ToolBarFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 管理用户列表的页面
 */
public class ManageUserActivity extends BaseActivity implements View.OnClickListener, AbsListView.OnScrollListener {
    private static final String TAG = "ManageUserActivity";

    private ToolBarFragment toolBarFragment;
//    private View goBackView;
//    private TextView titleView;
    private ListView userListView;
    private List<User> userList = new ArrayList<>();
    private UserAdapter userAdapter;
    private View addUserBtn;

    private User user;
    private int updatePage;
    private boolean isGetAllUser;

    private UserAdapter.DeleteUserListener deleteUserListener = new UserAdapter.DeleteUserListener() {
        @Override
        public void delete(User user) {
            if (user.getId().equals(ManageUserActivity.this.user.getId())) {
                Toast.makeText(ManageUserActivity.this, "不能删除自己", Toast.LENGTH_SHORT).show();
            } else {
                new DeleteUserAsyncTask(user.getId()).execute();
            }
        }
    };

    /**
     * 启动此活动时调用
     * @param context
     * @param user
     * @return
     */
    public static Intent startActivity(Context context, User user) {
        Intent intent = new Intent(context, ManageUserActivity.class);
        intent.putExtra("user", user);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_manage_user);

        Intent intent = getIntent();
        user = (User) intent.getSerializableExtra("user");
        if (user == null) {
            Toast.makeText(this, "user为null", Toast.LENGTH_SHORT).show();
            System.exit(1);
        }

        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 刷新数据
        initData();
    }

    /**
     * 初始化页面控件
     */
    private void initView() {
//        goBackView = findViewById(R.id.go_back);
//        goBackView.setVisibility(View.INVISIBLE);
//        titleView = (TextView) findViewById(R.id.title);
//        titleView.setText("用户管理");
        toolBarFragment = (ToolBarFragment) getFragmentManager().findFragmentById(R.id.tool_bar);
//        toolBarFragment.setGoBackViewVisibility(View.INVISIBLE);
        toolBarFragment.setTitle("用户管理");
        toolBarFragment.setUser(user);
        userListView = (ListView) findViewById(R.id.user_list);
        userAdapter = new UserAdapter(this, R.layout.user_list_item, userList,
                deleteUserListener);
        userListView.setAdapter(userAdapter);
        userListView.setOnScrollListener(this);
        addUserBtn = findViewById(R.id.add_user);
        addUserBtn.setOnClickListener(this);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        updatePage = 0;
        isGetAllUser = false;
        userList.clear();
        userAdapter.notifyDataSetChanged();
        new GetUserListAsyncTask().execute();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_user:
                Intent intent = ManageUserDetailActivity.startActivity(this, user);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        /**
         * 加载更多的用户
         */
        // 当不滚动时
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            // 判断是否滚动到底部
            if (view.getLastVisiblePosition() == view.getCount() - 1) {
                if (!isGetAllUser) {
                    new GetUserListAsyncTask().execute();
                }
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    /**
     * 获取用户列表的异步操作类
     */
    class GetUserListAsyncTask extends AsyncTask<Void, Void, PageBean<User>> {

        @Override
        protected PageBean<User> doInBackground(Void... params) {
            HttpQueryBuilder builder = new HttpQueryBuilder();
            builder.get().url(Const.BASE_URL + "/user/list?page=" + (updatePage + 1));
            try {
                String json = builder.connect();
                Log.d(TAG, "task return: " + json);
                JSONObject jsonObject = new JSONObject(json);
                String resultStr = jsonObject.getString("result");
                ObjectMapper objectMapper = new ObjectMapper();
                Result result = objectMapper.readValue(resultStr, Result.class);
                if (result.isSuccess()) {
                    String userListStr = jsonObject.getString("users");
                    PageBean<User> userPageBean = objectMapper.readValue(userListStr,
                            new TypeReference<PageBean<User>>() {
                            });
                    return userPageBean;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(PageBean<User> userPageBean) {
            if (userPageBean == null) {
                Toast.makeText(ManageUserActivity.this, Const.ERROR_NETWORK, Toast.LENGTH_SHORT).show();
                return;
            }
            if (userPageBean.size() > 0) {
                updatePage++;
                userList.addAll(userPageBean.getBeans());
                userAdapter.notifyDataSetChanged();
            }
            if (userList.size() == userPageBean.getTotalRows()) {
                isGetAllUser = true;
            }
            /**
             * 第一次加载10个用户，界面没有填充满
             * 需要继续加载用户，直到没有用户或listview出现上下滚动条
             */
            if (updatePage == 1 && !isGetAllUser) {
                new GetUserListAsyncTask().execute();
            }
        }
    }

    /**
     * 删除用户的异步操作类
     */
    class DeleteUserAsyncTask extends AsyncTask<Void, Void, Result> {

        private Integer userId;

        public DeleteUserAsyncTask(Integer userId) {
            this.userId = userId;
        }

        @Override
        protected Result doInBackground(Void... params) {
            HttpQueryBuilder builder = new HttpQueryBuilder();
            builder.addHeader("Content-Type", "application/x-www-form-urlencoded");
            builder.addParam("userId", String.valueOf(userId));
            builder.url(Const.BASE_URL + "/user/remove");
            builder.post();
            try {
                String json = builder.connect();
                Log.d(TAG, "task return: " + json);
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
                Toast.makeText(ManageUserActivity.this, Const.ERROR_NETWORK, Toast.LENGTH_SHORT).show();
                return;
            }
            if (result.isSuccess()) {
                Iterator<User> iterator = userList.iterator();
                User user;
                while (iterator.hasNext()) {
                    user = iterator.next();
                    if (user.getId().equals(userId)) {
                        iterator.remove();
                        userAdapter.notifyDataSetChanged();
                        return;
                    }
                }
            } else {
                Toast.makeText(ManageUserActivity.this, "删除失败\n" + result.getErr(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
