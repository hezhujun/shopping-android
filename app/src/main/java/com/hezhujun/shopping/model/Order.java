package com.hezhujun.shopping.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class Order implements Serializable {

    public static final String ORDER_STATE_NEW = "等待商家确认";
    public static final String ORDER_STATE_CONFIRM = "商家已确认";
    public static final String ORDER_STATE_REJECT = "商家已拒绝";
    public static final String ORDER_STATE_CLOSE = "用户已关闭";

    public static final byte ORDER_SUCCESS = 1;
    public static final byte ORDER_FAIL = 0;


    private Integer id;

    private Date time;

    private BigDecimal price;

    private String state = ORDER_STATE_NEW;

    private User user;

    private Product product;

    private String userRemark;

    private String businessmanRemark;

    private Byte success = ORDER_FAIL;

    private String addressee;

    private String phone;

    private String address;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state == null ? null : state.trim();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getUserRemark() {
        return userRemark;
    }

    public void setUserRemark(String userRemark) {
        this.userRemark = userRemark == null ? null : userRemark.trim();
    }

    public String getBusinessmanRemark() {
        return businessmanRemark;
    }

    public void setBusinessmanRemark(String businessmanRemark) {
        this.businessmanRemark = businessmanRemark == null ? null : businessmanRemark.trim();
    }

    public Byte getSuccess() {
        return success;
    }

    public void setSuccess(Byte success) {
        this.success = success;
    }

    public String getAddressee() {
        return addressee;
    }

    public void setAddressee(String addressee) {
        this.addressee = addressee == null ? null : addressee.trim();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone == null ? null : phone.trim();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address == null ? null : address.trim();
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", time=" + time +
                ", price=" + price +
                ", state='" + state + '\'' +
                ", user=" + user +
                ", product=" + product +
                ", userRemark='" + userRemark + '\'' +
                ", businessmanRemark='" + businessmanRemark + '\'' +
                ", success=" + success +
                ", addressee='" + addressee + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}