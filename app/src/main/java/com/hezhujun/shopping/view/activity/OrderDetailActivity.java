package com.hezhujun.shopping.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hezhujun.shopping.R;
import com.hezhujun.shopping.common.Const;
import com.hezhujun.shopping.common.HttpQueryBuilder;
import com.hezhujun.shopping.model.Order;
import com.hezhujun.shopping.model.Product;
import com.hezhujun.shopping.model.Result;
import com.hezhujun.shopping.model.User;
import com.hezhujun.shopping.view.fragment.ToolBarFragment;
import com.hezhujun.shopping.view.task.DownloadImageAsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * 用户查看订单详情的页面
 */
public class OrderDetailActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "OrderDetailActivity";

    private ToolBarFragment toolBarFragment;
//    private View goBackView;
//    private TextView titleView;
    private ImageView productImageView;
    private TextView productNameView;
    private TextView productNowPriceView;
    private TextView productPriceView;
    private TextView productDiscountView;
    private TextView productCountView;
    private TextView productIdView;
    private TextView orderPriceView;
    private TextView orderAddresseeView;
    private TextView orderPhoneView;
    private TextView orderAddressView;
    private TextView orderStateView;
    private TextView orderRemarkView;
    private EditText orderRemarkInputView;
    private View buttonView;
    private TextView btn;

    private User user;
    private Order order;

    /**
     * 启动此活动需要调用的函数
     * @param context
     * @param user
     * @param order
     * @return
     */
    public static Intent startActivity(Context context, User user, Order order) {
        Intent intent = new Intent(context, OrderDetailActivity.class);
        intent.putExtra("user", user);
        intent.putExtra("order", order);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        Intent intent = getIntent();
        user = (User) intent.getSerializableExtra("user");
        if (user == null) {
            Toast.makeText(this, "user为null", Toast.LENGTH_SHORT).show();
            System.exit(1);
        }
        order = (Order) intent.getSerializableExtra("order");
        if (order == null) {
            Toast.makeText(this, "order为null", Toast.LENGTH_SHORT).show();
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
        productIdView = (TextView) findViewById(R.id.order_id);
        orderPriceView = (TextView) findViewById(R.id.order_price);
        orderAddresseeView = (TextView) findViewById(R.id.order_addressee);
        orderPhoneView = (TextView) findViewById(R.id.order_phone);
        orderAddressView = (TextView) findViewById(R.id.order_address);
        orderStateView = (TextView) findViewById(R.id.order_state);
        orderRemarkView = (TextView) findViewById(R.id.order_remark);
        orderRemarkInputView = (EditText) findViewById(R.id.order_remark_input);
        buttonView = findViewById(R.id.button_view);
        btn = (TextView) findViewById(R.id.btn);
    }

    /**
     * 初始化数据
     */
    private void initData() {
//        titleView.setText("订单详情");
        toolBarFragment.setTitle("订单详情");
        Product product = order.getProduct();
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
        productIdView.setText(String.valueOf(order.getId()));
        orderPriceView.setText(String.format("%5.2f", order.getPrice().doubleValue()));
        orderAddresseeView.setText(order.getAddressee());
        orderPhoneView.setText(order.getPhone());
        orderAddressView.setText(order.getAddress());

        if (order.getState().equals(Order.ORDER_STATE_NEW)) {
            orderStateView.setText(order.getState());
            btn.setText("等待");
            btn.setOnClickListener(null);
            orderRemarkView.setVisibility(View.VISIBLE);
            orderRemarkInputView.setVisibility(View.GONE);
            buttonView.setVisibility(View.GONE);
        } else if (order.getState().equals(Order.ORDER_STATE_CONFIRM)) {
            orderStateView.setText(order.getState());
            btn.setText("收到货，完成");
            btn.setOnClickListener(this);
            orderRemarkView.setVisibility(View.GONE);
            orderRemarkInputView.setVisibility(View.VISIBLE);
        } else if (order.getState().equals(Order.ORDER_STATE_REJECT)) {
            orderStateView.setText(order.getState());
            btn.setText("关闭");
            btn.setOnClickListener(this);
            orderRemarkView.setVisibility(View.GONE);
            orderRemarkInputView.setVisibility(View.VISIBLE);
        } else if (order.getState().equals(Order.ORDER_STATE_CLOSE)) {
            if (order.getSuccess().equals(Order.ORDER_SUCCESS)) {
                orderStateView.setText("交易成功 订单完成");
            } else if (order.getSuccess().equals(Order.ORDER_FAIL)) {
                orderStateView.setText("交易失败 订单结束");
            }
            orderRemarkView.setText(order.getUserRemark());
            orderRemarkView.setVisibility(View.VISIBLE);
            orderRemarkInputView.setVisibility(View.GONE);
            buttonView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.go_back:
                finish();
                break;
            case R.id.btn:
                dealWithButton();
                break;
            default:
                break;
        }
    }

    /**
     * 关闭订单
     */
    private void dealWithButton() {
        if (order.getState().equals(Order.ORDER_STATE_CONFIRM)
                || order.getState().equals(Order.ORDER_STATE_REJECT)) {
            String remark = orderRemarkInputView.getText().toString();
            btn.setOnClickListener(null);
            new CloseOrderAsyncTask(order.getId(), remark).execute();
        }
    }

    /**
     * 发送关闭订单请求的异步操作类
     */
    class CloseOrderAsyncTask extends AsyncTask<Void, Void, Result> {

        private Integer orderId;
        private String remark;

        public CloseOrderAsyncTask(Integer orderId, String remark) {
            this.orderId = orderId;
            this.remark = remark;
        }

        @Override
        protected Result doInBackground(Void... params) {
            HttpQueryBuilder builder = new HttpQueryBuilder();
            builder.addHeader("Content-Type", "application/x-www-form-urlencoded");
            builder.addParam("orderId", String.valueOf(orderId));
            builder.addParam("remark", remark);
            builder.url(Const.BASE_URL + "/order/close");
            builder.post();
            try {
                String json = builder.connect();
                Log.d(TAG, "task return: " + json);
                JSONObject jsonObject = new JSONObject(json);
                String resultStr = jsonObject.getString("result");
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readValue(resultStr, Result.class);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Result result) {
            if (result == null || !result.isSuccess()) {
                Toast.makeText(OrderDetailActivity.this, "操作失败", Toast.LENGTH_SHORT).show();
                btn.setOnClickListener(OrderDetailActivity.this);
            }
            else {
                order.setUserRemark(remark);
                order.setState(Order.ORDER_STATE_CLOSE);
                initData();
            }
        }
    }
}
