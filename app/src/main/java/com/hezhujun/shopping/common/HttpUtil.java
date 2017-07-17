package com.hezhujun.shopping.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by hezhujun on 2017/6/14.
 * 发送http请求工具类
 */
public class HttpUtil {

    public static final String CHARSET = "UTF-8";

    /**
     * 获取输入流的内容
     * @param inputStream 输入流
     * @return 字符串内容
     * @throws IOException
     */
    public static String getContent(InputStream inputStream) throws IOException {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(inputStream, CHARSET));
            StringBuilder result = new StringBuilder();
            char[] buffer = new char[1024];
            int length = 0;
            while ((length = br.read(buffer, 0, 1024)) != -1) {
                result.append(buffer, 0, length);
            }
            return result.toString();
        } catch (IOException e) {
            throw e;
        } finally {
            if (br != null) {
                br.close();
            }
            inputStream.close();
        }
    }

    public static InputStream execute(URL url, String method) throws IOException {
        return execute(url, null, null, method);
    }

    public static InputStream execute(URL url, Header headers, String method) throws IOException {
        return execute(url, headers, null, method);
    }

    public static InputStream execute(URL url, Param params, String method) throws IOException {
        return execute(url, null, params, method);
    }

    public static InputStream execute(URL url, Header headers, Param params, String method) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        if (headers != null && headers.size() > 0) {
            for (Map.Entry<String, String> entry :
                    headers.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        if (params != null && params.size() > 0) {
            connection.setDoOutput(true);
            connection.setDoInput(true);
            OutputStream os = connection.getOutputStream();
            PrintWriter pw = new PrintWriter(os);
            Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                pw.write(entry.getKey() + "=" + entry.getValue());
                if (iterator.hasNext()) {
                    pw.write("&");
                }
            }
            pw.flush();
            pw.close();
            os.close();
        } else {
            connection.setDoInput(true);
        }
        return connection.getInputStream();
    }

    public static InputStream execute(URL url, Header headers, String method, Holder holder) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        if (headers != null && headers.size() > 0) {
            for (Map.Entry<String, String> entry :
                    headers.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        connection.setDoOutput(true);
        OutputStream os = connection.getOutputStream();
        holder.dealWithOutputStream(os);
        os.flush();
        os.close();
        return connection.getInputStream();
    }

    public static String post(URL url, Header header, Param params) throws IOException {
        InputStream is = execute(url, header, params, "POST");
        BufferedReader br = new BufferedReader(new InputStreamReader(is, CHARSET));
        StringBuilder result = new StringBuilder();
        char[] buffer = new char[1024];
        int length = 0;
        while ((length = br.read(buffer, 0, 1024)) != -1) {
            result.append(buffer, 0, length);
        }
        br.close();
        is.close();
        return result.toString();
    }

    public static String get(URL url) throws IOException {
        InputStream is = execute(url, "GET");
        BufferedReader br = new BufferedReader(new InputStreamReader(is, CHARSET));
        StringBuilder result = new StringBuilder();
        char[] buffer = new char[1024];
        int length = 0;
        while ((length = br.read(buffer, 0, 1024)) != -1) {
            result.append(buffer, 0, length);
        }
        br.close();
        is.close();
        return result.toString();
    }

    /**
     * 封装的header头
     */
    public static class Header extends HashMap<String, String> {

    }

    /**
     * 封装的post参数
     */
    public static class Param extends HashMap<String, String> {

    }

    /**
     * 处理复杂的输出
     */
    public interface Holder {

        public void dealWithOutputStream(OutputStream outputStream) throws IOException;

    }

}
