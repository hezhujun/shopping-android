package com.hezhujun.shopping.view.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.hezhujun.shopping.common.Global;

import java.net.URL;

/**
 * Created by hezhujun on 2017/7/2.
 * <p>
 * 下载图片，需要传入图片的url和显示图片的ImageView
 */
public class DownloadImageAsyncTask extends AsyncTask<Void, Void, Bitmap> {

    private String url;
    private ImageView imageView;

    public DownloadImageAsyncTask(String url, ImageView imageView) {
        this.url = url;
        this.imageView = imageView;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        }
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        if (url != null) {
            Bitmap bitmap = Global.imgBuffer.get(url);
            if (bitmap != null) {
                return bitmap;
            } else {
                try {
                    System.out.println("download image: " + url);
                    URL picUrl = new URL(url);
                    Bitmap pngBM = BitmapFactory.decodeStream(picUrl.openStream());
                    Global.imgBuffer.put(url, pngBM);
                    return pngBM;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
