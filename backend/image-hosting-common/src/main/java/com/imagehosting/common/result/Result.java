package com.imagehosting.common.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一返回结果
 * 用于规范化API接口返回格式，提供统一的成功和失败处理方法
 * 支持泛型，可以返回任意类型的数据
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     * 200表示成功，其他值表示失败
     */
    private int code;

    /**
     * 消息
     * 成功或失败的提示信息
     */
    private String message;

    /**
     * 数据
     * API返回的具体数据
     */
    private T data;

    /**
     * 是否成功
     * true表示成功，false表示失败
     */
    private boolean success;

    /**
     * 私有构造方法
     * 禁止外部直接创建对象，必须通过静态方法创建
     */
    private Result() {
    }

    /**
     * 成功返回结果
     * 不带数据的成功响应
     *
     * @param <T> 泛型
     * @return 返回成功的结果对象
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 成功返回结果
     * 带数据的成功响应，使用默认成功消息
     *
     * @param data 返回的数据
     * @param <T>  泛型类型
     * @return 返回成功的结果对象
     */
    public static <T> Result<T> success(T data) {
        return success(ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功返回结果
     * 自定义消息和数据的成功响应
     *
     * @param message 自定义成功消息
     * @param data    返回的数据
     * @param <T>     泛型类型
     * @return 返回成功的结果对象
     */
    public static <T> Result<T> success(String message, T data) {
        return result(ResultCode.SUCCESS.getCode(), message, data, true);
    }

    /**
     * 失败返回结果
     * 使用默认错误码和消息的失败响应
     *
     * @param <T> 泛型
     * @return 返回失败的结果对象
     */
    public static <T> Result<T> failed() {
        return failed(ResultCode.SYSTEM_ERROR);
    }

    /**
     * 失败返回结果
     * 使用指定结果码的失败响应
     *
     * @param resultCode 结果码对象，包含错误码和错误消息
     * @param <T>        泛型
     * @return 返回失败的结果对象
     */
    public static <T> Result<T> failed(IResultCode resultCode) {
        return result(resultCode.getCode(), resultCode.getMessage(), null, false);
    }

    /**
     * 失败返回结果
     * 使用默认错误码和自定义消息的失败响应
     *
     * @param message 自定义错误消息
     * @param <T>     泛型
     * @return 返回失败的结果对象
     */
    public static <T> Result<T> failed(String message) {
        return result(ResultCode.SYSTEM_ERROR.getCode(), message, null, false);
    }

    /**
     * 失败返回结果
     * 使用自定义错误码和消息的失败响应
     *
     * @param code    自定义状态码
     * @param message 自定义错误消息
     * @param <T>     泛型
     * @return 返回失败的结果对象
     */
    public static <T> Result<T> failed(int code, String message) {
        return result(code, message, null, false);
    }

    /**
     * 参数验证失败返回结果
     * 用于表单验证等参数校验失败的情况
     *
     * @param message 错误消息
     * @param <T>     泛型
     * @return 返回参数验证失败的结果对象
     */
    public static <T> Result<T> validateFailed(String message) {
        return result(ResultCode.VALIDATE_FAILED.getCode(), message, null, false);
    }

    /**
     * 返回结果
     * 统一创建结果对象的内部方法
     *
     * @param code    状态码
     * @param message 消息
     * @param data    数据
     * @param success 是否成功
     * @param <T>     泛型
     * @return 返回结果对象
     */
    private static <T> Result<T> result(int code, String message, T data, boolean success) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(data);
        result.setSuccess(success);
        return result;
    }
} 