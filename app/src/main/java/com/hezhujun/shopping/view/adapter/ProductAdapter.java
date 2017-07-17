package com.hezhujun.shopping.view.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.PluralsRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hezhujun.shopping.R;
import com.hezhujun.shopping.common.Const;
import com.hezhujun.shopping.model.Product;
import com.hezhujun.shopping.view.task.DownloadImageAsyncTask;

import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by hezhujun on 2017/7/11.
 * 产品列表的适配器
 */
public class ProductAdapter extends ArrayAdapter<Product>{
    private int resource;
    public ProductAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Product> objects) {
        super(context, resource, objects);
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        ViewHolder holder;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resource, null);
            holder = new ViewHolder();
            holder.productImageView = (ImageView) view.findViewById(R.id.product_image);
            holder.productNameView = (TextView) view.findViewById(R.id.product_name);
            holder.productNowPriceView = (TextView) view.findViewById(R.id.product_now_price);
            holder.productPriceView = (TextView) view.findViewById(R.id.product_price);
            holder.productDiscountView = (TextView) view.findViewById(R.id.product_discount);
            holder.productCountView = (TextView) view.findViewById(R.id.product_count);
            view.setTag(holder);
        }
        else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }
        Product product = getItem(position);
//        System.out.println(product);
        if (product.getImgUrl() != null || !"".equals(product.getImgUrl())) {
            new DownloadImageAsyncTask(Const.BASE_URL + "/" + product.getImgUrl(),
                    holder.productImageView).execute();
        }
        holder.productNameView.setText(product.getName());
        double price = product.getPrice().doubleValue();
        holder.productPriceView.setText(String.format("%5.2f", price));
        double discount = product.getRegular().getDiscount().doubleValue();
        holder.productDiscountView.setText(String.format("%1.2f", discount));
        double nowPrice = product.getRegular().getDiscount().multiply(product.getPrice()).doubleValue();
        holder.productNowPriceView.setText(String.format("%5.2f", nowPrice));
        holder.productCountView.setText(String.valueOf(product.getRepertory().getCount()));
        return view;
    }

    class ViewHolder{
        ImageView productImageView;
        TextView productNameView;
        TextView productNowPriceView;
        TextView productPriceView;
        TextView productDiscountView;
        TextView productCountView;
    }
}
