package com.hezhujun.shopping.model;

import java.util.List;

/**
 * Created by hezhujun on 2017/7/10.
 * 分页bean
 */
public class PageBean<T> {

    public static final int DEFAULT_ROWS = 10;

    int totalRows;
    int totalPages;
    int page;
    int rows = DEFAULT_ROWS;
    List<T> beans;

    public PageBean() {
    }

    public PageBean(int page, int rows) {
        this.totalRows = totalRows;
        this.page = page;
        this.rows = rows;
    }

    public static int getDefaultRows() {
        return DEFAULT_ROWS;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public int getPage() {
        return page;
    }

    public int getRows() {
        return rows;
    }

    public List<T> getBeans() {
        return beans;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
        if (totalRows % rows == 0) {
            this.totalPages = totalRows / rows;
        } else {
            this.totalPages = totalRows / rows + 1;
        }
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setBeans(List<T> beans) {
        this.beans = beans;
    }

    public int size() {
        if (beans == null) {
            return 0;
        } else {
            return beans.size();
        }
    }

    public boolean hasNext() {
        if (page < totalPages) {
            return true;
        }
        else {
            return false;
        }
    }

    public boolean hasBefore() {
        if (page > 0) {
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "PageBean{" +
                "totalRows=" + totalRows +
                ", page=" + page +
                ", rows=" + rows +
                ", beans=" + beans +
                '}';
    }
}
