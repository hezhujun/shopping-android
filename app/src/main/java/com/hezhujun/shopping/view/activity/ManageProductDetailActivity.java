package com.hezhujun.shopping.view.activity;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hezhujun.shopping.R;
import com.hezhujun.shopping.common.Const;
import com.hezhujun.shopping.common.HttpUtil;
import com.hezhujun.shopping.model.Category;
import com.hezhujun.shopping.model.Product;
import com.hezhujun.shopping.model.Regular;
import com.hezhujun.shopping.model.Repertory;
import com.hezhujun.shopping.model.Result;
import com.hezhujun.shopping.model.User;
import com.hezhujun.shopping.view.fragment.ToolBarFragment;
import com.hezhujun.shopping.view.task.DownloadImageAsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 产品销售商修改或添加商品页面
 */
public class ManageProductDetailActivity extends BaseActivity implements View.OnClickListener, View.OnFocusChangeListener, TextWatcher {
    private static final String TAG = "ManageProductDetail";
    private static final int RETURN_IMAGE = 10086;
    public static final int ADD = 1;
    public static final int UPDATE = 2;

    private ToolBarFragment toolBarFragment;
    //    private View goBackView;
//    private TextView titleView;
    private EditText productNameView;
    private EditText productPriceView;
    private TextView productNowPriceView;
    private EditText productDiscountView;
    private EditText productCountView;
    private EditText productDescriptionView;
    private EditText productImageUrlView;
    private TextView addOrUpdateBtn;
    private TextView resultView;
    private ImageView productImageView;
    private Button selectImageBtn;
    private Button uploadButton;

    private Product product;
    private Category category;
    private int which;
    private User user;
    private String currentImagePath;

    /**
     * 启动此活动需要调用的
     * @param context
     * @param user
     * @param product
     * @param category
     * @param which 是更新还是修改
     * @return
     */
    public static Intent startActivity(Context context, User user,
                                       Product product, Category category, int which) {
        Intent intent = new Intent(context, ManageProductDetailActivity.class);
        intent.putExtra("product", product);
        intent.putExtra("category", category);
        intent.putExtra("which", which);
        intent.putExtra("user", user);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_product_detail);

        Intent intent = getIntent();
        which = intent.getIntExtra("which", ADD);
        if (which == UPDATE) {
            product = (Product) intent.getSerializableExtra("product");
            if (product == null) {
                Toast.makeText(this, "product为null", Toast.LENGTH_SHORT).show();
                System.exit(1);
            }
        }
        category = (Category) intent.getSerializableExtra("category");
        if (category == null) {
            Toast.makeText(this, "category为null", Toast.LENGTH_SHORT).show();
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
     * 初始化页面控件
     */
    private void initView() {
        toolBarFragment = (ToolBarFragment) getFragmentManager().findFragmentById(R.id.tool_bar);
        toolBarFragment.setUser(user);
//        goBackView = findViewById(R.id.go_back);
//        goBackView.setOnClickListener(this);
//        titleView = (TextView) findViewById(R.id.title);
        productNameView = (EditText) findViewById(R.id.product_name);
        productPriceView = (EditText) findViewById(R.id.product_price);
        productPriceView.setOnFocusChangeListener(this);
        productNowPriceView = (TextView) findViewById(R.id.product_now_price);
        productDiscountView = (EditText) findViewById(R.id.product_discount);
        productDiscountView.setOnFocusChangeListener(this);
        productDiscountView.addTextChangedListener(this);
        productCountView = (EditText) findViewById(R.id.product_count);
        productDescriptionView = (EditText) findViewById(R.id.product_description);
        productImageUrlView = (EditText) findViewById(R.id.product_image_url);
        addOrUpdateBtn = (TextView) findViewById(R.id.add_or_update);
        addOrUpdateBtn.setOnClickListener(this);
        resultView = (TextView) findViewById(R.id.result);
        productImageView = (ImageView) findViewById(R.id.product_image);
        selectImageBtn = (Button) findViewById(R.id.select_image);
        selectImageBtn.setOnClickListener(this);
        uploadButton = (Button) findViewById(R.id.upload_img);
        uploadButton.setOnClickListener(this);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        switch (which) {
            case ADD:
                toolBarFragment.setTitle("添加商品");
//                titleView.setText("添加商品");
                addOrUpdateBtn.setText("添加");
                break;
            case UPDATE:
                toolBarFragment.setTitle("修改商品");
//                titleView.setText("修改商品");
                addOrUpdateBtn.setText("修改");
                productNameView.setText(product.getName());
                productNameView.setFocusableInTouchMode(false);
                double price = product.getPrice().doubleValue();
                productPriceView.setText(String.format("%5.2f", price));
                double discount = product.getRegular().getDiscount().doubleValue();
                productDiscountView.setText(String.format("%1.2f", discount));
                double nowPrice = product.getRegular().getDiscount()
                        .multiply(product.getPrice()).doubleValue();
                productNowPriceView.setText(String.format("%5.2f", nowPrice));
                productCountView.setText(String.valueOf(product.getRepertory().getCount()));
                productDescriptionView.setText(product.getDescription());
                productImageUrlView.setText(product.getImgUrl());
                if (product.getImgUrl() != null || !"".equals(product.getImgUrl())) {
                    new DownloadImageAsyncTask(Const.BASE_URL + "/" + product.getImgUrl(),
                            productImageView).execute();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.go_back:
                finish();
                break;
            case R.id.add_or_update:
                addOrUpdate();
                break;
            case R.id.select_image:
                //开启选择呢绒界面
                Intent intent = new Intent("android.intent.action.GET_CONTENT");
                //设置可以缩放
                intent.putExtra("scale", true);
                //设置可以裁剪
                intent.putExtra("crop", true);
                intent.setType("image/*");
                //开始选择
                startActivityForResult(intent, RETURN_IMAGE);
                break;
            case R.id.upload_img:
                if (currentImagePath == null) {
                    Toast.makeText(this, "请选择图片", Toast.LENGTH_SHORT).show();
                    return;
                }
                String type = currentImagePath.substring(currentImagePath.lastIndexOf(".") + 1);
                if ("jpg".equals(type) || "png".equals(type)) {
                    Bitmap bitmap = BitmapFactory.decodeFile(currentImagePath);
                    uploadButton.setOnClickListener(null);
                    new UploadImageTask(bitmap, type).execute();
                }
                else {
                    Toast.makeText(this, "请选择jpg或png格式的图片", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 选择图片后显示图片
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        if (resultCode == RESULT_OK) {
            handleImageOnKitkat(data);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void handleImageOnKitkat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            // 如果是document类型的Uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri
                    .getAuthority())) {
                String id = docId.split(":")[1]; // 解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri
                    .getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果不是document类型的Uri，则使用普通方式处理
            imagePath = getImagePath(uri, null);
        }
        displayImage(imagePath); // 根据图片路径显示图片
        System.err.println(imagePath);
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        // 通过uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null,
                null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void displayImage(String imagePath) {
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            productImageView.setImageBitmap(bitmap);
            currentImagePath = imagePath;
        } else {
            Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    /**
     * 添加或更新操作
     */
    private void addOrUpdate() {
        String productName = productNameView.getText().toString().trim();
        if ("".equals(productName)) {
            Toast.makeText(this, "请输入商品名称", Toast.LENGTH_SHORT).show();
            return;
        }
        String productPriceStr = productPriceView.getText().toString();
        if ("".equals(productPriceStr)) {
            Toast.makeText(this, "请输入商品价格", Toast.LENGTH_SHORT).show();
            return;
        }
        double productPrice = Double.parseDouble(productPriceStr);
        String productDiscountStr = productDiscountView.getText().toString();
        if ("".equals(productDiscountStr)) {
            Toast.makeText(this, "请输入商品折扣", Toast.LENGTH_SHORT).show();
            return;
        }
        double productDiscount = Double.parseDouble(productDiscountStr);
        int discount100 = (int) (productDiscount * 100);
        if (discount100 > 100 || discount100 <= 0) {
            Toast.makeText(this, "折扣的范围是1.00-0.01", Toast.LENGTH_SHORT).show();
            return;
        }
        String productCountStr = productCountView.getText().toString();
        if ("".equals(productCountStr)) {
            Toast.makeText(this, "请输入商品库存", Toast.LENGTH_SHORT).show();
            return;
        }
        int productCount = Integer.parseInt(productCountStr);
        String productDescription = productDescriptionView.getText().toString();
        if ("".equals(productDescription)) {
            Toast.makeText(this, "请输入商品描述信息", Toast.LENGTH_SHORT).show();
            return;
        }
        String productImageUrl = productImageUrlView.getText().toString();

        Product newProduct = new Product();
        newProduct.setName(productName);
        newProduct.setDescription(productDescription);
        newProduct.setImgUrl(productImageUrl);
        newProduct.setPrice(new BigDecimal(productPriceStr));
        newProduct.setCategory(category);
        newProduct.setImgUrl(productImageUrl);
        Regular regular;
        Repertory repertory;

        addOrUpdateBtn.setOnClickListener(null);
        switch (which) {
            case ADD:
                regular = new Regular(new BigDecimal(productDiscountStr));
                repertory = new Repertory(productCount);
                newProduct.setRegular(regular);
                newProduct.setRepertory(repertory);
                new AddProductAsyncTask(newProduct).execute();
                break;
            case UPDATE:
                newProduct.setId(product.getId());
                regular = new Regular(product.getRegular().getId(),
                        new BigDecimal(productDiscountStr));
                repertory = new Repertory(product.getRepertory().getId(),
                        productCount);
                newProduct.setRegular(regular);
                newProduct.setRepertory(repertory);
                Log.d(TAG, "更新：" + newProduct);
                new UpdateProductAsyncTask(newProduct).execute();
                break;
            default:
                break;
        }

    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            String productPriceStr = productPriceView.getText().toString();
            if ("".equals(productPriceStr)) {
                productNowPriceView.setText("0.00");
                return;
            }
            String productDiscountStr = productDiscountView.getText().toString();
            if ("".equals(productDiscountStr)) {
                productDiscountStr = "1";
            }
            BigDecimal productPrice = new BigDecimal(productPriceStr);
            BigDecimal productDiscount = new BigDecimal(productDiscountStr);
            double productNowPrice = productPrice.multiply(productDiscount).doubleValue();
            productNowPriceView.setText(String.format("%5.2f", productNowPrice));
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    /**
     * 处理折扣，使得折扣的范围为1.00-0.00
     * @param s
     */
    @Override
    public void afterTextChanged(Editable s) {
        String discountStr = s.toString();
        if ("".equals(discountStr)) {
            return;
        }
        double discount = Double.parseDouble(discountStr);
        if (discount > 1) {
            productDiscountView.setText("1.00");
            return;
        }
        if (discountStr.length() > 4) {
            productDiscountView.setText(discountStr.substring(0, 4));
        }

    }

    /**
     * 添加商品的异步操作
     */
    class AddProductAsyncTask extends AsyncTask<Void, Void, String> {

        private Product product;

        public AddProductAsyncTask(Product product) {
            this.product = product;
        }

        @Override
        protected String doInBackground(Void... params) {
            HttpUtil.Holder holder = new HttpUtil.Holder() {
                @Override
                public void dealWithOutputStream(OutputStream outputStream) throws IOException {
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.writeValue(outputStream, product);
                }
            };
            HttpUtil.Header header = new HttpUtil.Header();
            header.put("Content-type", "application/json");
            try {
                InputStream is = HttpUtil.execute(new URL(Const.BASE_URL + "/product/save"), header, "POST", holder);
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
                Toast.makeText(ManageProductDetailActivity.this, Const.ERROR_NETWORK, Toast.LENGTH_SHORT).show();
                addOrUpdateBtn.setOnClickListener(ManageProductDetailActivity.this);
                return;
            }
            try {
                JSONObject jsonObject = new JSONObject(s);
                String resultStr = jsonObject.getString("result");
                ObjectMapper objectMapper = new ObjectMapper();
                Result result = objectMapper.readValue(resultStr, Result.class);
                if (result.isSuccess()) {
                    resultView.setText("添加成功！");
                    Toast.makeText(ManageProductDetailActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ManageProductDetailActivity.this, result.getErr(), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(ManageProductDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            addOrUpdateBtn.setOnClickListener(ManageProductDetailActivity.this);
        }
    }

    /**
     * 更新商品的异步操作
     */
    class UpdateProductAsyncTask extends AsyncTask<Void, Void, String> {

        private Product product;

        public UpdateProductAsyncTask(Product product) {
            this.product = product;
        }

        @Override
        protected String doInBackground(Void... params) {
            HttpUtil.Holder holder = new HttpUtil.Holder() {
                @Override
                public void dealWithOutputStream(OutputStream outputStream) throws IOException {
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.writeValue(outputStream, product);
                }
            };
            HttpUtil.Header header = new HttpUtil.Header();
            header.put("Content-type", "application/json");
            try {
                InputStream is = HttpUtil.execute(new URL(Const.BASE_URL + "/product/update"), header, "POST", holder);
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
                Toast.makeText(ManageProductDetailActivity.this, Const.ERROR_NETWORK, Toast.LENGTH_SHORT).show();
                addOrUpdateBtn.setOnClickListener(ManageProductDetailActivity.this);
                return;
            }
            try {
                JSONObject jsonObject = new JSONObject(s);
                String resultStr = jsonObject.getString("result");
                ObjectMapper objectMapper = new ObjectMapper();
                Result result = objectMapper.readValue(resultStr, Result.class);
                if (result.isSuccess()) {
                    resultView.setText("更新成功！");
                    Toast.makeText(ManageProductDetailActivity.this, "更新成功", Toast.LENGTH_SHORT).show();
                    String productStr = jsonObject.getString("product");
                    Product p = objectMapper.readValue(productStr, Product.class);
                    if (p != null) {
                        this.product = p;
                        Log.d(TAG, "更新了product");
                    }
                }
                else {
                    Toast.makeText(ManageProductDetailActivity.this, result.getErr(), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(ManageProductDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            addOrUpdateBtn.setOnClickListener(ManageProductDetailActivity.this);
        }
    }

    /**
     * 上传图片的异步操作
     */
    class UploadImageTask extends AsyncTask<Void, Void, String> {
        private static final String BOUNDARY = "------FormBoundary15d402eb3ed";
        private Bitmap bitmap;
        private String type;

        public UploadImageTask(Bitmap bitmap, String type) {
            this.bitmap = bitmap;
            this.type = type;
        }

        @Override
        protected String doInBackground(Void... params) {

            HttpUtil.Holder holder = new HttpUtil.Holder() {
                @Override
                public void dealWithOutputStream(OutputStream outputStream) throws IOException {
                    outputStream.write(("--" + BOUNDARY + "\r\n").getBytes("UTF-8"));
                    if ("jpg".equals(type)) {
                        type = "jpeg";
                    }
                    outputStream.write(("Content-Disposition: form-data; " +
                            "name=\"file\"; filename=\"temp\"\r\n").getBytes("UTF-8"));
                    outputStream.write(("Content-Type: image/" + type +
                            "\r\n\r\n").getBytes("UTF-8"));
                    if ("jpeg".equals(type)) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    } else if ("png".equals(type)) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    }
                    outputStream.write(("\r\n--" + BOUNDARY + "--\r\n").getBytes("UTF-8"));
                }
            };
            HttpUtil.Header header = new HttpUtil.Header();
            header.put("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);
            try {
                InputStream is = HttpUtil.execute(new URL(Const.BASE_URL + "/upload"),
                        header, "POST", holder);

                String result = HttpUtil.getContent(is);
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.d(TAG, "上传图片返回值: " + s);
            if (s == null) {
                Toast.makeText(ManageProductDetailActivity.this, Const.ERROR_NETWORK, Toast.LENGTH_SHORT).show();
                uploadButton.setOnClickListener(ManageProductDetailActivity.this);
                return;
            }
            try {
                JSONObject jsonObject = new JSONObject(s);
                String resultStr = jsonObject.getString("result");
                ObjectMapper objectMapper = new ObjectMapper();
                Result result = objectMapper.readValue(resultStr, Result.class);
                if (result.isSuccess()) {
                    String imageUrl = jsonObject.getString("image");
                    productImageUrlView.setText(imageUrl);
                    Toast.makeText(ManageProductDetailActivity.this, "上传成功", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(ManageProductDetailActivity.this, result.getErr(), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (JsonParseException e) {
                e.printStackTrace();
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            uploadButton.setOnClickListener(ManageProductDetailActivity.this);
        }
    }
}
