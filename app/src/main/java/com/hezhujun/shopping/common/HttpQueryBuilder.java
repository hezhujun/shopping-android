package com.hezhujun.shopping.common;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by hezhujun on 2017/7/10.
 *
 * 负责发起请求并获取数据
 */
public class HttpQueryBuilder {
    private HttpUtil.Param param;
    private HttpUtil.Header header;
    private HttpUtil.Holder holder;
    private String method;
    private String url;

    /**
     * 添加post的请求参数
     * @param key
     * @param value
     * @return
     */
    public HttpQueryBuilder addParam(String key, String value){
        if (param == null) {
            param = new HttpUtil.Param();
        }
        param.put(key, value);
        return this;
    }

    /**
     * 添加header
     * @param key
     * @param value
     * @return
     */
    public HttpQueryBuilder addHeader(String key, String value) {
        if (header == null) {
            header = new HttpUtil.Header();
        }
        header.put(key, value);
        return this;
    }

    /**
     * 设置请求方法为post
     * @return
     */
    public HttpQueryBuilder post() {
        method = "POST";
        return this;
    }

    /**
     * 设置请求方法为get
     * @return
     */
    public HttpQueryBuilder get() {
        method = "GET";
        return this;
    }

    /**
     * 设置请求的url
     * @param url
     * @return
     */
    public HttpQueryBuilder url(String url) {
        this.url = url;
        return this;
    }

    /**
     * 建立连接
     * @return
     * @throws IOException
     */
    public String connect() throws IOException {
        System.out.println("-------------" + method + " " + url);
        InputStream is = HttpUtil.execute(new URL(url), header, param, method);
        return HttpUtil.getContent(is);
    }
}
