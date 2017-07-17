package com.hezhujun.shopping.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hezhujun.shopping.R;
import com.hezhujun.shopping.common.Const;
import com.hezhujun.shopping.common.HttpQueryBuilder;
import com.hezhujun.shopping.model.Category;
import com.hezhujun.shopping.model.PageBean;
import com.hezhujun.shopping.model.Product;
import com.hezhujun.shopping.model.Result;
import com.hezhujun.shopping.model.User;
import com.hezhujun.shopping.view.adapter.ProductAdapter;
import com.hezhujun.shopping.view.fragment.ToolBarFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 显示商品列表的页面
 */
public class ProductListActivity extends BaseActivity
        implements AdapterView.OnItemClickListener,
        View.OnClickListener, AbsListView.OnScrollListener {
    private static final String TAG = "ProductListActivity";

    private ToolBarFragment toolBarFragment;
    //    private View goBackView;
//    private TextView titleView;
    private ListView productListView;
    private ProductAdapter productAdapter;

    private List<Product> productList = new ArrayList<>();
    private User user;
    private Category category;
    /**
     * 最新更新的产品页数
     */
    private int updatePage;
    /**
     * 是否加载了所有的商品
     */
    private boolean isGetAllProduct;

    /**
     * 启动此活动需要调用的函数
     * @param context
     * @param user
     * @param category
     * @return
     */
    public static Intent startActivity(Context context, User user, Category category) {
        Intent intent = new Intent(context, ProductListActivity.class);
        intent.putExtra("user", user);
        intent.putExtra("category", category);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_product_list);

        Intent intent = getIntent();
        user = (User) intent.getSerializableExtra("user");
        if (user == null) {
            Toast.makeText(this, "user为null", Toast.LENGTH_SHORT).show();
            System.exit(1);
        }
        category = (Category) intent.getSerializableExtra("category");
        if (user == null) {
            Toast.makeText(this, "category为null", Toast.LENGTH_SHORT).show();
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
        toolBarFragment.setUser(user);
//        goBackView = findViewById(R.id.go_back);
//        goBackView.setOnClickListener(this);
//        titleView = (TextView) findViewById(R.id.title);
        productListView = (ListView) findViewById(R.id.product_list);
        productAdapter = new ProductAdapter(this, R.layout.product_list_item, productList);
        productListView.setAdapter(productAdapter);
        productListView.setOnItemClickListener(this);
        productListView.setOnScrollListener(this);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        toolBarFragment.setTitle(category.getName());
//        titleView.setText(category.getName());
        updatePage = 0;
        isGetAllProduct = false;
        productList.clear();
        productAdapter.notifyDataSetChanged();
        new GetProductListAsyncTask().execute();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Product product = productList.get(position);
        Intent intent = ProductDetailActivity.startActivity(this, user, product);
        startActivity(intent);
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
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        /**
         * 加载更多的商品
         */
        // 当不滚动时
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            // 判断是否滚动到底部
            if (view.getLastVisiblePosition() == view.getCount() - 1) {
                if (!isGetAllProduct) {
                    new GetProductListAsyncTask().execute();
                }
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    /**
     * 查询商品的异步操作类
     */
    class GetProductListAsyncTask extends AsyncTask<Void, Void, PageBean<Product>> {

        @Override
        protected PageBean<Product> doInBackground(Void... params) {
            HttpQueryBuilder builder = new HttpQueryBuilder();
            builder.get();
            builder.url(Const.BASE_URL + "/product/list?categoryId=" + category.getId() +
                    "&page=" + String.valueOf(updatePage + 1));
            try {
                String json = builder.connect();
                Log.d(TAG, "get product list task return: " + json);
                JSONObject jsonObject = new JSONObject(json);
                ObjectMapper objectMapper = new ObjectMapper();
                String resultStr = jsonObject.getString("result");
                Result result = objectMapper.readValue(resultStr, Result.class);
                if (result.isSuccess()) {
                    String productListStr = jsonObject.getString("products");
                    PageBean<Product> productPageBean = objectMapper.readValue(productListStr,
                            new TypeReference<PageBean<Product>>() {
                            });
                    return productPageBean;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(PageBean<Product> productPageBean) {
            if (productPageBean != null) {
                if (productPageBean.size() > 0) {
                    updatePage++;
                    productList.addAll(productPageBean.getBeans());
                    productAdapter.notifyDataSetChanged();
                }
                if (productList.size() == productPageBean.getTotalRows()) {
                    isGetAllProduct = true;
                }
            }
        }
    }
}
