package com.hezhujun.shopping.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
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
 * 唱片销售商处理订单页面
 */
public class OrderActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "OrderActivity";

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
    private TextView confirmBtn;
    private TextView rejectBtn;

    private Order order;
    private User user;

    /**
     * 启动活动需要调用的函数
     * @param context
     * @param order
     * @param user
     * @return
     */
    public static Intent startActivity(Context context, Order order, User user) {
        Intent intent = new Intent(context, OrderActivity.class);
        intent.putExtra("order", order);
        intent.putExtra("user", user);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_order);

        Intent intent = getIntent();
        order = (Order) intent.getSerializableExtra("order");
        if (order == null) {
            Toast.makeText(this, "order为null", Toast.LENGTH_SHORT).show();
            System.exit(1);
        }
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
        toolBarFragment = (ToolBarFragment) getFragmentManager().findFragmentById(R.id.tool_bar);
//        goBackView = findViewById(R.id.go_back);
//        goBackView.setOnClickListener(this);
//        titleView = (TextView) findViewById(R.id.title);
//        titleView.setText("订单详情");
        toolBarFragment.setTitle("订单详情");
        toolBarFragment.setUser(user);
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
        confirmBtn = (TextView) findViewById(R.id.confirm);
        confirmBtn.setOnClickListener(this);
        rejectBtn = (TextView) findViewById(R.id.reject);
        rejectBtn.setOnClickListener(this);
    }

    /**
     * 数据初始化
     */
    private void initData() {
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
        orderStateView.setText(order.getState());

        if (order.getState().equals(Order.ORDER_STATE_NEW)) {
            buttonView.setVisibility(View.VISIBLE);
            orderRemarkInputView.setVisibility(View.VISIBLE);
            orderRemarkView.setVisibility(View.GONE);
        } else {
            buttonView.setVisibility(View.GONE);
            orderRemarkInputView.setVisibility(View.GONE);
            orderRemarkView.setVisibility(View.VISIBLE);
            orderRemarkView.setText(order.getBusinessmanRemark());
            if (order.getState().equals(Order.ORDER_STATE_CLOSE)) {
                if (order.getSuccess().equals(Order.ORDER_SUCCESS)) {
                    orderStateView.setText("交易成功 订单完成");
                } else if (order.getSuccess().equals(Order.ORDER_FAIL)) {
                    orderStateView.setText("交易失败 订单结束");
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.go_back:
                finish();
                break;
            case R.id.confirm:
                confirmOrder();
                break;
            case R.id.reject:
                rejectOrder();
                ;
                break;
            default:
                break;
        }
    }

    /**
     * 确认订单
     */
    private void confirmOrder() {
        String remark = orderRemarkInputView.getText().toString().trim();
        confirmBtn.setOnClickListener(null);
        rejectBtn.setOnClickListener(null);
        new ConfirmOrderAsyncTask(order.getId(), remark).execute();
    }

    /**
     * 拒绝订单
     */
    private void rejectOrder() {
        String remark = orderRemarkInputView.getText().toString().trim();
        confirmBtn.setOnClickListener(null);
        rejectBtn.setOnClickListener(null);
        new RejectOrderAsyncTask(order.getId(), remark).execute();
    }

    /**
     * 发送确认订单请求的异步操作类
     */
    class ConfirmOrderAsyncTask extends AsyncTask<Void, Void, Result> {

        private Integer orderId;
        private String remark;

        public ConfirmOrderAsyncTask(Integer orderId, String remark) {
            this.orderId = orderId;
            this.remark = remark;
        }

        @Override
        protected Result doInBackground(Void... params) {
            HttpQueryBuilder builder = new HttpQueryBuilder();
            builder.post();
            builder.url(Const.BASE_URL + "/order/confirm");
            builder.addHeader("Content-Type", "application/x-www-form-urlencoded");
            builder.addParam("orderId", String.valueOf(orderId));
            builder.addParam("remark", remark);
            try {
                String json = builder.connect();
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
                Toast.makeText(OrderActivity.this, Const.ERROR_NETWORK, Toast.LENGTH_SHORT).show();
                confirmBtn.setOnClickListener(OrderActivity.this);
                rejectBtn.setOnClickListener(OrderActivity.this);
                return;
            }
            if (result.isSuccess()) {
                order.setState(Order.ORDER_STATE_CONFIRM);
                order.setBusinessmanRemark(remark);
                order.setSuccess(Order.ORDER_SUCCESS);
                initData();
            } else {
                Toast.makeText(OrderActivity.this, result.getErr(), Toast.LENGTH_SHORT).show();
            }
            confirmBtn.setOnClickListener(OrderActivity.this);
            rejectBtn.setOnClickListener(OrderActivity.this);
        }
    }

    /**
     * 发送拒绝订单请求的异步操作类
     */
    class RejectOrderAsyncTask extends AsyncTask<Void, Void, Result> {

        private Integer orderId;
        private String remark;

        public RejectOrderAsyncTask(Integer orderId, String remark) {
            this.orderId = orderId;
            this.remark = remark;
        }

        @Override
        protected Result doInBackground(Void... params) {
            HttpQueryBuilder builder = new HttpQueryBuilder();
            builder.post();
            builder.url(Const.BASE_URL + "/order/reject");
            builder.addHeader("Content-Type", "application/x-www-form-urlencoded");
            builder.addParam("orderId", String.valueOf(orderId));
            builder.addParam("remark", remark);
            try {
                String json = builder.connect();
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
                Toast.makeText(OrderActivity.this, Const.ERROR_NETWORK, Toast.LENGTH_SHORT).show();
                confirmBtn.setOnClickListener(OrderActivity.this);
                rejectBtn.setOnClickListener(OrderActivity.this);
                return;
            }
            if (result.isSuccess()) {
                order.setState(Order.ORDER_STATE_REJECT);
                order.setBusinessmanRemark(remark);
                order.setSuccess(Order.ORDER_FAIL);
                initData();
            } else {
                Toast.makeText(OrderActivity.this, result.getErr(), Toast.LENGTH_SHORT).show();
            }
            confirmBtn.setOnClickListener(OrderActivity.this);
            rejectBtn.setOnClickListener(OrderActivity.this);
        }
    }
}
