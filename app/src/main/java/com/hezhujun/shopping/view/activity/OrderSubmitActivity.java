package com.hezhujun.shopping.view.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hezhujun.shopping.R;
import com.hezhujun.shopping.common.Const;
import com.hezhujun.shopping.common.HttpQueryBuilder;
import com.hezhujun.shopping.common.HttpUtil;
import com.hezhujun.shopping.model.Order;
import com.hezhujun.shopping.model.Product;
import com.hezhujun.shopping.model.Result;
import com.hezhujun.shopping.model.User;
import com.hezhujun.shopping.view.fragment.ToolBarFragment;
import com.hezhujun.shopping.view.task.DownloadImageAsyncTask;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

/**
 * 提交订单页面
 */
public class OrderSubmitActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "OrderSubmitActivity";

    private ToolBarFragment toolBarFragment;
//    private View goBackView;
//    private TextView titleView;
    private ImageView productImageView;
    private TextView productNameView;
    private TextView productNowPriceView;
    private TextView productPriceView;
    private TextView productDiscountView;
    private TextView productCountView;
    private TextView orderPriceView;
    private EditText orderAddresseeView;
    private EditText orderPhoneView;
    private EditText orderAddressView;
    private View submitBtn;

    private User user;
    private Product product;
    private Order order;

    /**
     * 启动活动需要调用的函数
     * @param context
     * @param user
     * @param product
     * @return
     */
    public static Intent startActivity(Context context, User user, Product product) {
        Intent intent = new Intent(context, OrderSubmitActivity.class);
        intent.putExtra("user", user);
        intent.putExtra("product", product);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_submit);

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
        orderPriceView = (TextView) findViewById(R.id.order_price);
        orderAddresseeView = (EditText) findViewById(R.id.order_addressee);
        orderPhoneView = (EditText) findViewById(R.id.order_phone);
        orderAddressView = (EditText) findViewById(R.id.order_address);
        submitBtn = findViewById(R.id.submit);
        submitBtn.setOnClickListener(this);
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
        orderPriceView.setText(String.format("%5.2f", nowPrice));
        if (user.getName() != null) {
            orderAddresseeView.setText(user.getName());
        }
        if (user.getPhone() != null) {
            orderPhoneView.setText(user.getPhone());
        }
        if (user.getAddress() != null) {
            orderAddressView.setText(user.getAddress());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.go_back:
                finish();
                break;
            case R.id.submit:
                submit();
                break;
        }
    }

    /**
     * 提交订单
     */
    private void submit() {
        String addressee = orderAddresseeView.getText().toString().trim();
        if ("".equals(addressee)) {
            Toast.makeText(this, "请输入收货人", Toast.LENGTH_SHORT).show();
            return;
        }
        String phone = orderPhoneView.getText().toString().trim();
        if ("".equals(phone)) {
            Toast.makeText(this, "请输入电话号码", Toast.LENGTH_SHORT).show();
            return;
        }
        String address = orderAddressView.getText().toString().trim();
        if ("".equals(address)) {
            Toast.makeText(this, "请输入收货地址", Toast.LENGTH_SHORT).show();
            return;
        }

        submitBtn.setOnClickListener(null);

        order = new Order();
        order.setTime(new Date());
        order.setPrice(product.getPrice().multiply(product.getRegular().getDiscount()));
        order.setUser(user);
        order.setProduct(product);
        order.setAddressee(addressee);
        order.setPhone(phone);
        order.setAddress(address);

        // 弹出对话框，要求输入密码确认
        final EditText passwordView = new EditText(this);
        passwordView.setHint("请输入密码");
        // 注意，密码输入栏如下设置
        passwordView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        new AlertDialog.Builder(this).setTitle("确认购买")
                .setView(passwordView)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new ConfirmPasswordAsyncTask(user.getUsername(), passwordView.getText().toString())
                                .execute();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        submitBtn.setOnClickListener(OrderSubmitActivity.this);
                    }
                })
                .show();
    }

    /**
     * 输入密码确认身份的异步操作类
     */
    class ConfirmPasswordAsyncTask extends AsyncTask<Void, Void, String> {

        private String username;
        private String password;

        public ConfirmPasswordAsyncTask(String username, String password) {
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
                Toast.makeText(OrderSubmitActivity.this, Const.ERROR_NETWORK, Toast.LENGTH_SHORT).show();
                submitBtn.setOnClickListener(OrderSubmitActivity.this);
                return;
            }
            try {
                JSONObject jsonObject = new JSONObject(s);
                String resultStr = jsonObject.getString("result");
                ObjectMapper objectMapper = new ObjectMapper();
                Result result = objectMapper.readValue(resultStr, Result.class);
                if (result.isSuccess()) {
                    if (order == null) {
                        Toast.makeText(OrderSubmitActivity.this, "订单未初始化", Toast.LENGTH_SHORT).show();
                    } else {
                        new SubmitOrderAsyncTask(order).execute();
                    }
                } else {
                    Toast.makeText(OrderSubmitActivity.this, "密码不正确", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(OrderSubmitActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            submitBtn.setOnClickListener(OrderSubmitActivity.this);
        }
    }

    /**
     * 发送提交订单请求的异步操作类
     */
    class SubmitOrderAsyncTask extends AsyncTask<Void, Void, String> {

        private Order order;

        public SubmitOrderAsyncTask(Order order) {
            this.order = order;
        }

        @Override
        protected String doInBackground(Void... params) {
            HttpUtil.Holder holder = new HttpUtil.Holder() {
                @Override
                public void dealWithOutputStream(OutputStream outputStream) throws IOException {
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.writeValue(outputStream, order);
                }
            };
            HttpUtil.Header header = new HttpUtil.Header();
            header.put("Content-type", "application/json");
            try {
                InputStream is = HttpUtil.execute(new URL(Const.BASE_URL + "/order/submit"), header, "POST", holder);
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
            Log.d(TAG, "task return: " + s);
            if (s == null) {
                Toast.makeText(OrderSubmitActivity.this, Const.ERROR_NETWORK, Toast.LENGTH_SHORT).show();
                submitBtn.setOnClickListener(OrderSubmitActivity.this);
                return;
            }
            try {
                JSONObject jsonObject = new JSONObject(s);
                String resultStr = jsonObject.getString("result");
                ObjectMapper objectMapper = new ObjectMapper();
                Result result = objectMapper.readValue(resultStr, Result.class);
                if (result.isSuccess()) {
                    String orderStr = jsonObject.getString("order");
                    Order order = objectMapper.readValue(orderStr, Order.class);
                    Intent intent = OrderDetailActivity.startActivity(OrderSubmitActivity.this, user, order);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(OrderSubmitActivity.this, "密码不正确", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(OrderSubmitActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            submitBtn.setOnClickListener(OrderSubmitActivity.this);
        }
    }
}
