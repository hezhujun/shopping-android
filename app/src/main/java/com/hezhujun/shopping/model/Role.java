package com.hezhujun.shopping.model;

import java.io.Serializable;

public class Role implements Serializable {

    public static final String USER_ROLE_COMMON = "普通用户";
    public static final String USER_ROLE_PRODUCER = "产品销售商";
    public static final String USER_ROLE_ADMIN = "系统管理员";


    private Integer id;

    private String roleName;

    public Role() {
    }

    public Role(String roleName) {
        this.roleName = roleName;
    }

    public Role(Integer id, String roleName) {
        this.id = id;
        this.roleName = roleName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName == null ? null : roleName.trim();
    }

    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", roleName='" + roleName + '\'' +
                '}';
    }
}