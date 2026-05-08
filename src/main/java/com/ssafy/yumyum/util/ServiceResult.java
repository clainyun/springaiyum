package com.ssafy.yumyum.util;

public class ServiceResult<T> {

    private final boolean ok;
    private final String message;
    private final T data;

    private ServiceResult(boolean ok, String message, T data) {
        this.ok = ok;
        this.message = message;
        this.data = data;
    }

    public static <T> ServiceResult<T> success(String message, T data) {
        return new ServiceResult<>(true, message, data);
    }

    public static <T> ServiceResult<T> failure(String message) {
        return new ServiceResult<>(false, message, null);
    }

    public boolean isOk() {
        return ok;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
