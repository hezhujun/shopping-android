package com.hezhujun.shopping.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hezhujun.shopping.R;
import com.hezhujun.shopping.common.Const;
import com.hezhujun.shopping.common.HttpQueryBuilder;
import com.hezhujun.shopping.model.Category;
import com.hezhujun.shopping.model.Result;
import com.hezhujun.shopping.model.User;
import com.hezhujun.shopping.view.adapter.CategoryAdapter;
import com.hezhujun.shopping.view.fragment.ToolBarFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 显示商品类别的活动
 */
public class CategoryActivity extends BaseActivity
        implements View.OnClickListener, AdapterView.OnItemClickListener {
    private static final String TAG = "CategoryActivity";

    private ToolBarFragment toolBarFragment;
    //    private View goBackView;
    private ListView categoryListView;

    private User user;
    private List<Category> categoryList = new ArrayList<>();
    private CategoryAdapter categoryAdapter;

    /**
     * 启动此活动时调用
     * @param context
     * @param user
     * @return
     */
    public static Intent startActivity(Context context, User user) {
        Intent intent = new Intent(context, CategoryActivity.class);
        intent.putExtra("user", user);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_category);

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
     * 页面控件初始化
     */
    private void initView() {
//        goBackView = findViewById(R.id.go_back);
        toolBarFragment = (ToolBarFragment) getFragmentManager().findFragmentById(R.id.tool_bar);
        toolBarFragment.setUser(user);
        categoryListView = (ListView) findViewById(R.id.category_list);

//        goBackView.setOnClickListener(this);

        categoryAdapter = new CategoryAdapter(this, R.layout.category_list_item, categoryList);
        categoryListView.setAdapter(categoryAdapter);
        categoryListView.setOnItemClickListener(this);
    }

    /**
     * 数据初始化
     */
    private void initData() {
        new GetCategoryListAsyncTask().execute();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.go_back:
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Category category = categoryList.get(position);
        Intent intent = ProductListActivity.startActivity(this, user, category);
        startActivity(intent);
    }

    /**
     * 获取类别列表的异步查询类
     */
    class GetCategoryListAsyncTask extends AsyncTask<Void, Void, List<Category>> {
        @Override
        protected List<Category> doInBackground(Void... params) {
            HttpQueryBuilder builder = new HttpQueryBuilder();
            builder.url(Const.BASE_URL + "/category/list");
            builder.get();
            try {
                String json = builder.connect();
                Log.d(TAG, "get category list task return: " + json);
                JSONObject jsonObject = new JSONObject(json);
                String resultStr = jsonObject.getString("result");
                ObjectMapper objectMapper = new ObjectMapper();
                Result result = objectMapper.readValue(resultStr, Result.class);
                if (result.isSuccess()) {
                    String categoryListStr = jsonObject.getString("categories");
                    List<Category> categoryList = objectMapper.readValue(categoryListStr, new TypeReference<List<Category>>() {
                    });
                    return categoryList;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Category> categories) {
            if (categories != null && categories.size() > 0) {
                categoryList.addAll(categories);
                categoryAdapter.notifyDataSetChanged();
            }
        }
    }
}
