package com.hezhujun.shopping.view.fragment;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hezhujun.shopping.R;
import com.hezhujun.shopping.common.Const;
import com.hezhujun.shopping.common.HttpQueryBuilder;
import com.hezhujun.shopping.model.Order;
import com.hezhujun.shopping.model.PageBean;
import com.hezhujun.shopping.model.Result;
import com.hezhujun.shopping.model.User;
import com.hezhujun.shopping.view.activity.OrderActivity;
import com.hezhujun.shopping.view.adapter.OrderAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 未处理订单的页面
 */
public class OrderTodoFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener, AbsListView.OnScrollListener {
    private static final String TAG = "OrderTodoFragment";

    private ToolBarFragment toolBarFragment;
    //    private View goBackView;
//    private TextView titleView;
    private ListView orderTodoListView;
    private List<Order> orderList = new ArrayList<>();
    private OrderAdapter orderAdapter;

    private User user;
    private int updatePage;
    private boolean isGetAllOrder;

    public OrderTodoFragment() {
    }

    /**
     * 生产fragment实例
     * @param user 用户对象
     * @return
     */
    public static OrderTodoFragment newInstance(User user) {
        OrderTodoFragment fragment = new OrderTodoFragment();
        fragment.user = user;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_todo, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 刷新页面
        initData();
    }

    /**
     * 初始化页面控件
     * @param view
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void initView(View view) {
        toolBarFragment = (ToolBarFragment) getChildFragmentManager().findFragmentById(R.id.tool_bar_todo);
//        goBackView = view.findViewById(R.id.go_back);
//        goBackView.setOnClickListener(this);
//        titleView = (TextView) view.findViewById(R.id.title);
//        titleView.setText("待处理订单");
        toolBarFragment.setTitle("待处理订单");
        orderTodoListView = (ListView) view.findViewById(R.id.order_todo_list);
        orderAdapter = new OrderAdapter(getActivity(), R.layout.order_list_item, orderList);
        orderTodoListView.setAdapter(orderAdapter);
        orderTodoListView.setOnItemClickListener(this);
        orderTodoListView.setOnScrollListener(this);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        updatePage = 0;
        isGetAllOrder = false;
        orderList.clear();
        orderAdapter.notifyDataSetChanged();
        new GetTodoOrderListAsyncTask().execute();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.go_back:
                getActivity().finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Order order = orderList.get(position);
        Intent intent = OrderActivity.startActivity(getActivity(), order, user);
        startActivity(intent);
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
                    new GetTodoOrderListAsyncTask().execute();
                }
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    /**
     * 获取为处理订单的异步操作类
     */
    class GetTodoOrderListAsyncTask extends AsyncTask<Void, Void, PageBean<Order>> {

        @Override
        protected PageBean<Order> doInBackground(Void... params) {
            HttpQueryBuilder builder = new HttpQueryBuilder();
            builder.post();
            builder.url(Const.BASE_URL + "/order/todo");
            builder.addHeader("Content-Type", "application/x-www-form-urlencoded");
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
