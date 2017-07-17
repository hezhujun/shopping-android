package com.hezhujun.shopping.model;

/**
 * Created by hezhujun on 2017/6/24.
 */
public class Result {
    private static final int NO_ERROR_CODE = 0;

    private int errCode = NO_ERROR_CODE;
    private String err = "";
    private boolean success = true;

    public Result() {
    }

    public Result(int errCode, String err, boolean success) {
        this.errCode = errCode;
        this.err = err;
        this.success = success;
    }

    @Override
    public String toString() {
        return "Result{" +
                "errCode=" + errCode +
                ", err='" + err + '\'' +
                ", success=" + success +
                '}';
    }

    public int getErrCode() {
        return errCode;
    }

    public void setErrCode(int errCode) {
        this.errCode = errCode;
    }

    public String getErr() {
        return err;
    }

    public void setErr(String err) {
        this.err = err;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
