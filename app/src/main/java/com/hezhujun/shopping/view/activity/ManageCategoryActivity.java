package com.hezhujun.shopping.view.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
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
 * 管理类别的活动
 */
public class ManageCategoryActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private static final String TAG = "CategoryActivity";

    private ToolBarFragment toolBarFragment;
    //    private View goBackView;
    private ListView categoryListView;
    private View addCategoryBtn;

    private User user;
    private List<Category> categoryList = new ArrayList<>();
    private CategoryAdapter categoryAdapter;

    public static Intent startActivity(Context context, User user) {
        Intent intent = new Intent(context, ManageCategoryActivity.class);
        intent.putExtra("user", user);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_manage_category);

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
        initData();
    }

    /**
     * 页面控件初始化
     */
    private void initView() {
        toolBarFragment = (ToolBarFragment) getFragmentManager().findFragmentById(R.id.tool_bar);
        toolBarFragment.setUser(user);
        toolBarFragment.setTitle("商品类别");
//        goBackView = findViewById(R.id.go_back);
        categoryListView = (ListView) findViewById(R.id.category_list);

//        goBackView.setOnClickListener(this);

        categoryAdapter = new CategoryAdapter(this, R.layout.category_list_item, categoryList);
        categoryListView.setAdapter(categoryAdapter);
        categoryListView.setOnItemClickListener(this);

        addCategoryBtn = findViewById(R.id.add_category);
        addCategoryBtn.setOnClickListener(this);
    }

    /**
     * 数据初始化
     */
    private void initData() {
        categoryList.clear();
        categoryAdapter.notifyDataSetChanged();
        new GetCategoryListAsyncTask().execute();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.go_back:
                finish();
                break;
            case R.id.add_category:
                addCategory();
            default:
                break;
        }
    }

    /**
     * 添加类别函数
     */
    private void addCategory() {
        final EditText input = new EditText(this);
        input.setHint("请输入类别名称");
        new AlertDialog.Builder(this).setTitle("添加类别")
                .setView(input)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String category = input.getText().toString().trim();
                        if ("".equals(category)) {
                            Toast.makeText(ManageCategoryActivity.this, "请输入数据", Toast.LENGTH_SHORT).show();
                            addCategory();
                            return;
                        }
                        new AddCategoryAsyncTask(category).execute();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Category category = categoryList.get(position);
        Intent intent = ManageProductListActivity.startActivity(this, user, category);
        startActivity(intent);
    }

    /**
     * 获取类别的异步操作类
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

    /**
     * 添加类别的异步操作类
     */
    class AddCategoryAsyncTask extends AsyncTask<Void, Void, String> {

        private String category;

        public AddCategoryAsyncTask(String category) {
            this.category = category;
        }

        @Override
        protected String doInBackground(Void... params) {
            HttpQueryBuilder builder = new HttpQueryBuilder();
            builder.addHeader("Content-Type", "application/x-www-form-urlencoded");
            builder.addParam("category", category);
            builder.url(Const.BASE_URL + "/category/save");
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
            if (s == null) {
                Toast.makeText(ManageCategoryActivity.this, Const.ERROR_NETWORK, Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                JSONObject jsonObject = new JSONObject(s);
                String resultStr = jsonObject.getString("result");
                ObjectMapper objectMapper = new ObjectMapper();
                Result result = objectMapper.readValue(resultStr, Result.class);
                if (result.isSuccess()) {
                    String categoryStr = jsonObject.getString("category");
                    Category category = objectMapper.readValue(categoryStr, Category.class);
                    if (category != null) {
                        categoryList.add(category);
                        categoryAdapter.notifyDataSetChanged();
                        Toast.makeText(ManageCategoryActivity.this, "添加成功",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                else {
                    Toast.makeText(ManageCategoryActivity.this, "添加失败\n" + result.getErr(),
                            Toast.LENGTH_SHORT).show();
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
            Toast.makeText(ManageCategoryActivity.this, "添加失败", Toast.LENGTH_SHORT).show();
        }
    }
}
