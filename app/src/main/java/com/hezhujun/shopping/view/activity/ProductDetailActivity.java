package com.hezhujun.shopping.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hezhujun.shopping.R;
import com.hezhujun.shopping.common.Const;
import com.hezhujun.shopping.model.Product;
import com.hezhujun.shopping.model.User;
import com.hezhujun.shopping.view.fragment.ToolBarFragment;
import com.hezhujun.shopping.view.task.DownloadImageAsyncTask;

/**
 * 商品详情页面
 */
public class ProductDetailActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "ProductDetailActivity";

    private ToolBarFragment toolBarFragment;
//    private View goBackView;
//    private TextView titleView;
    private ImageView productImageView;
    private TextView productNameView;
    private TextView productNowPriceView;
    private TextView productPriceView;
    private TextView productDiscountView;
    private TextView productCountView;
    private TextView productDescriptionView;
    private View buyBtn;

    private User user;
    private Product product;

    /**
     * 启动活动需要调用的函数
     * @param context
     * @param user
     * @param product
     * @return
     */
    public static Intent startActivity(Context context, User user, Product product) {
        Intent intent = new Intent(context, ProductDetailActivity.class);
        intent.putExtra("user", user);
        intent.putExtra("product", product);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        Intent intent = getIntent();
        user = (User) intent.getSerializableExtra("user");
        if (user == null) {
            Toast.makeText(this, "user为null", Toast.LENGTH_SHORT).show();
            System.exit(1);
        }
        product = (Product) intent.getSerializableExtra("product");
        if (product == null) {
            Toast.makeText(this, "product为null", Toast.LENGTH_SHORT).show();
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
        productImageView = (ImageView) findViewById(R.id.product_image);
        productNameView = (TextView) findViewById(R.id.product_name);
        productNowPriceView = (TextView) findViewById(R.id.product_now_price);
        productPriceView = (TextView) findViewById(R.id.product_price);
        productDiscountView = (TextView) findViewById(R.id.product_discount);
        productCountView = (TextView) findViewById(R.id.product_count);
        productDescriptionView = (TextView) findViewById(R.id.product_description);
        buyBtn = findViewById(R.id.buy);
        buyBtn.setOnClickListener(this);
    }

    /**
     * 初始化数据
     */
    private void initData() {
//        titleView.setText("产品详情");
        toolBarFragment.setTitle("产品详情");
        if (product.getImgUrl() != null || !"".equals(product.getImgUrl())) {
            new DownloadImageAsyncTask(Const.BASE_URL + "/" + product.getImgUrl(),
                    productImageView).execute();
        }
        productNameView.setText(product.getName());
        double price = product.getPrice().doubleValue();
        productPriceView.setText(String.format("%5.2f", price));
        double discount = product.getRegular().getDiscount().doubleValue();
        productDiscountView.setText(String.format("%1.2f", discount));
        double nowPrice = product.getRegular().getDiscount().multiply(product.getPrice()).doubleValue();
        productNowPriceView.setText(String.format("%5.2f", nowPrice));
        productCountView.setText(String.valueOf(product.getRepertory().getCount()));
        productDescriptionView.setText(product.getDescription());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.go_back:
                finish();
                break;
            case R.id.buy:
                Intent intent = OrderSubmitActivity.startActivity(this, user, product);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
