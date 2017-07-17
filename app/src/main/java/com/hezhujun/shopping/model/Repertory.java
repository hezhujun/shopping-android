package com.hezhujun.shopping.model;

import java.io.Serializable;

public class Repertory implements Serializable {
    private Integer id;

    private Integer count;

    public Repertory() {
    }

    public Repertory(Integer count) {
        this.count = count;
    }

    public Repertory(Integer id, Integer count) {
        this.id = id;
        this.count = count;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "Repertory{" +
                "id=" + id +
                ", count=" + count +
                '}';
    }
}