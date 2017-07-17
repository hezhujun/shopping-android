package com.hezhujun.shopping.view.activity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hezhujun.shopping.R;
import com.hezhujun.shopping.common.Const;
import com.hezhujun.shopping.common.HttpQueryBuilder;
import com.hezhujun.shopping.model.Order;
import com.hezhujun.shopping.model.PageBean;
import com.hezhujun.shopping.model.Result;
import com.hezhujun.shopping.model.User;
import com.hezhujun.shopping.view.adapter.OrderAdapter;
import com.hezhujun.shopping.view.fragment.ToolBarFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 订单列表页面，可以条件查询
 */
public class OrderListActivity extends BaseActivity implements View.OnClickListener, AbsListView.OnScrollListener, AdapterView.OnItemClickListener {
    private static final String TAG = "OrderListActivity";
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat sdfOfShort = new SimpleDateFormat("yyyy-MM-dd");
    private static final Calendar calendar = Calendar.getInstance();
    ;

    private User user;

    private ToolBarFragment toolBarFragment;
//    private View goBackView;
//    private TextView titleView;
    private ListView orderListView;
    private OrderAdapter orderAdapter;
    private EditText queryOrderIdView;
    private Spinner queryOrderStateView;
    private TextView queryOrderFromView;
    private TextView queryOrderToView;
    private Button cleanBtn;
    private Button queryBtn;

    /**
     * 下面的是查询条件
     */
    private Integer orderId;
    private String orderState;
    private Date orderFrom;
    private Date orderTo;

    private List<Order> orderList = new ArrayList<>();
    /**
     * 当前更新到的页数
     */
    private int updatePage;
    /**
     * 是否已经加载所有订单
     */
    private boolean isGetAllOrder;

    /**
     * 启动此活动需要调用的函数
     * @param context
     * @param user
     * @return
     */
    public static Intent startActivity(Context context, User user) {
        Intent intent = new Intent(context, OrderListActivity.class);
        intent.putExtra("user", user);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);

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
        toolBarFragment = (ToolBarFragment) getFragmentManager().findFragmentById(R.id.tool_bar);
        toolBarFragment.setUser(user);
//        goBackView = findViewById(R.id.go_back);
//        goBackView.setOnClickListener(this);
//        titleView = (TextView) findViewById(R.id.title);
        orderListView = (ListView) findViewById(R.id.order_list);
        orderAdapter = new OrderAdapter(this, R.layout.order_list_item, orderList);
        orderListView.setAdapter(orderAdapter);
        orderListView.setOnScrollListener(this);
        orderListView.setOnItemClickListener(this);
        queryOrderIdView = (EditText) findViewById(R.id.query_order_id);
        queryOrderStateView = (Spinner) findViewById(R.id.query_order_state);
        queryOrderFromView = (TextView) findViewById(R.id.query_order_from);
        queryOrderFromView.setOnClickListener(this);
        queryOrderToView = (TextView) findViewById(R.id.query_order_to);
        queryOrderToView.setOnClickListener(this);
        cleanBtn = (Button) findViewById(R.id.clean);
        cleanBtn.setOnClickListener(this);
        queryBtn = (Button) findViewById(R.id.query);
        queryBtn.setOnClickListener(this);
    }

    /**
     * 初始化数据
     */
    private void initData() {
//        titleView.setText("订单");
        toolBarFragment.setTitle("订单");
        orderList.clear();
        orderAdapter.notifyDataSetChanged();
        updatePage = 0;
        isGetAllOrder = false;

        clean();

        new GetOrderListAsyncTask(user.getId(), null, null, null, null).execute();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.go_back:
                finish();
                break;
            case R.id.query_order_from:
                showDateDialog(1);
                break;
            case R.id.query_order_to:
                showDateDialog(2);
                break;
            case R.id.query:
                query();
                break;
            case R.id.clean:
                clean();
                break;
            default:
                break;
        }
    }

    /**
     * 处理查询
     */
    private void query() {
        String orderIdStr = queryOrderIdView.getText().toString().trim();
        if ("".equals(orderIdStr)) {
            orderId = null;
        } else {
            orderId = Integer.parseInt(orderIdStr);
        }
        if ("...".equals(queryOrderStateView.getSelectedItem())) {
            orderState = null;
        } else {
            orderState = queryOrderStateView.getSelectedItem().toString();
        }
        if ("...".equals(queryOrderFromView.getText().toString())) {
            orderFrom = null;
        } else {
            try {
                orderFrom = sdf.parse(queryOrderFromView.getText().toString() + " 00:00:00");
            } catch (ParseException e) {
                e.printStackTrace();
                Toast.makeText(this, "转换时间错误", Toast.LENGTH_SHORT).show();
            }
        }
        if ("...".equals(queryOrderToView.getText().toString())) {
            orderTo = null;
        } else {
            try {
                orderTo = sdf.parse(queryOrderToView.getText().toString() + " 23:59:59");
            } catch (ParseException e) {
                e.printStackTrace();
                Toast.makeText(this, "转换时间错误", Toast.LENGTH_SHORT).show();
            }
        }
        updatePage = 0;
        isGetAllOrder = false;
        orderList.clear();
        orderAdapter.notifyDataSetChanged();
        new GetOrderListAsyncTask(user.getId(), orderId, orderFrom, orderTo, orderState).execute();
    }

    /**
     * 重置条件
     */
    private void clean() {
        queryOrderIdView.setText("");
        orderId = null;
        queryOrderStateView.setSelection(0);
        orderState = null;
        queryOrderFromView.setText("...");
        orderFrom = null;
        queryOrderToView.setText("...");
        orderTo = null;
    }

    /**
     * 打开日期选择对话窗
     *
     * @param which 哪个textview
     *              1: query_order_from
     *              2: query_order_to
     */
    private void showDateDialog(int which) {
        new DatePickerDialog(this, new MyOnDateSetListener(which),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Order order = orderList.get(position);
        Intent intent = OrderDetailActivity.startActivity(this, user, order);
        startActivity(intent);
    }

    /**
     * 选择日期后显示在TextView
     */
    class MyOnDateSetListener implements DatePickerDialog.OnDateSetListener {
        /**
         * 开始日期还是结束日期
         */
        private int which;

        public MyOnDateSetListener(int which) {
            this.which = which;
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            try {
                Date date = null;
                switch (which) {
                    case 1:
                        date = buildDate(year, month + 1, dayOfMonth, 0, 0, 0);
                        queryOrderFromView.setText(sdfOfShort.format(date));
                        break;
                    case 2:
                        date = buildDate(year, month + 1, dayOfMonth, 23, 59, 59);
                        queryOrderToView.setText(sdfOfShort.format(date));
                        break;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 根据具体时间创建Date对象
     * @param year
     * @param month
     * @param dayOfMonth
     * @param hour
     * @param minute
     * @param second
     * @return
     * @throws ParseException
     */
    private Date buildDate(int year, int month, int dayOfMonth, int hour, int minute, int second) throws ParseException {
        String time = String.format("%s-%s-%s %s:%s:%s",
                numberToString(year, 4),
                numberToString(month, 2),
                numberToString(dayOfMonth, 2),
                numberToString(hour, 2),
                numberToString(minute, 2),
                numberToString(second, 2));
        return sdf.parse(time);
    }

    /**
     * 格式化数字
     * @param num 数字
     * @param length 数字长度
     * @return
     */
    private String numberToString(int num, int length) {
        String res = String.valueOf(num);
        int l = res.length();
        if (l < length) {
            for (int i = 0; i < length - l; i++) {
                res = "0" + res;
            }
        }
        return res;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        /**
         * 加载更多的订单
         */
        // 当不滚动时
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            // 判断是否滚动到底部
            if (view.getLastVisiblePosition() == view.getCount() - 1) {
                if (!isGetAllOrder) {
                    new GetOrderListAsyncTask(user.getId(), null, null, null, null).execute();
                }
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    /**
     * 查询订单的异步操作类
     */
    class GetOrderListAsyncTask extends AsyncTask<Void, Void, PageBean<Order>> {
        private Integer userId;
        private Integer id;
        private Date from;
        private Date to;
        private String state;

        public GetOrderListAsyncTask(Integer userId, Integer id, Date from, Date to, String state) {
            this.userId = userId;
            this.id = id;
            this.from = from;
            this.to = to;
            this.state = state;
        }

        @Override
        protected PageBean<Order> doInBackground(Void... params) {
            HttpQueryBuilder builder = new HttpQueryBuilder();
            builder.post();
            builder.url(Const.BASE_URL + "/order/list");
            builder.addHeader("Content-Type", "application/x-www-form-urlencoded");
            if (userId != null) {
                builder.addParam("userId", String.valueOf(userId));
            }
            if (id != null) {
                builder.addParam("orderId", String.valueOf(id));
            }
            if (from != null) {
                builder.addParam("from", String.valueOf(from.getTime()));
            }
            if (to != null) {
                builder.addParam("to", String.valueOf(to.getTime()));
            }
            if (state != null) {
                builder.addParam("state", state);
            }
            builder.addParam("page", String.valueOf(updatePage + 1));
            try {
                String json = builder.connect();
                Log.d(TAG, "task return:" + json);
                JSONObject jsonObject = new JSONObject(json);
                String resultStr = jsonObject.getString("result");
                ObjectMapper objectMapper = new ObjectMapper();
                Result result = objectMapper.readValue(resultStr, Result.class);
                if (result.isSuccess()) {
                    String orderListStr = jsonObject.getString("orders");
                    PageBean<Order> orderPageBean = objectMapper.readValue(orderListStr,
                            new TypeReference<PageBean<Order>>() {
                            });
                    return orderPageBean;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(PageBean<Order> orderPageBean) {
            if (orderPageBean != null) {
                if (orderPageBean.size() > 0) {
                    updatePage++;
                    orderList.addAll(orderPageBean.getBeans());
                    orderAdapter.notifyDataSetChanged();
                }
                if (orderList.size() == orderPageBean.getTotalRows()) {
                    isGetAllOrder = true;
                }
            }
        }
    }
}
