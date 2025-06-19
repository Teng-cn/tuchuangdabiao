package com.imagehosting.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 结果码枚举
 * 定义系统中所有可能的响应状态码和对应的消息
 * 统一管理API返回的状态码，方便维护和扩展
 */
@Getter
@AllArgsConstructor
public enum ResultCode implements IResultCode {

    /**
     * 操作成功
     * 请求处理成功，服务器已成功处理了请求
     */
    SUCCESS(200, "操作成功"),

    /**
     * 操作失败
     * 请求无法被服务器理解，通常是因为请求格式错误
     */
    FAILED(400, "操作失败"),

    /**
     * 参数错误
     * 请求参数不符合API要求，如缺少必填参数或参数类型错误
     */
    PARAM_ERROR(400, "参数错误"),

    /**
     * 参数验证失败
     * 请求参数未通过验证规则，如长度不符、格式不正确等
     */
    VALIDATE_FAILED(400, "参数验证失败"),

    /**
     * 未授权
     * 请求要求身份验证，用户未提供有效的认证信息
     */
    UNAUTHORIZED(401, "未授权"),

    /**
     * 禁止访问
     * 服务器理解请求但拒绝执行，通常是权限不足
     */
    FORBIDDEN(403, "禁止访问"),

    /**
     * 数据不存在
     * 请求的资源未在服务器上找到
     */
    DATA_NOT_FOUND(404, "数据不存在"),

    /**
     * 系统错误
     * 服务器遇到了一个未曾预料的状况，无法完成对请求的处理
     */
    SYSTEM_ERROR(500, "系统错误");

    /**
     * 状态码
     * HTTP响应状态码
     */
    private final int code;

    /**
     * 消息
     * 对应状态码的详细描述信息
     */
    private final String message;

    /**
     * 获取状态码
     * 实现IResultCode接口方法
     * 
     * @return 状态码
     */
    @Override
    public int getCode() {
        return code;
    }

    /**
     * 获取消息
     * 实现IResultCode接口方法
     * 
     * @return 消息
     */
    @Override
    public String getMessage() {
        return message;
    }
} 