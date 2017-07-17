package com.hezhujun.shopping.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class Regular implements Serializable {
    private Integer id;

    private BigDecimal discount;

    public Regular() {
    }

    public Regular(BigDecimal discount) {
        this.discount = discount;
    }

    public Regular(Integer id, BigDecimal discount) {
        this.id = id;
        this.discount = discount;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    @Override
    public String toString() {
        return "Regular{" +
                "id=" + id +
                ", discount=" + discount +
                '}';
    }
}