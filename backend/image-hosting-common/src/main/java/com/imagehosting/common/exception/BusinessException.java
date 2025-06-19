package com.imagehosting.common.exception;

import com.imagehosting.common.result.IResultCode;
import com.imagehosting.common.result.ResultCode;
import lombok.Getter;

/**
 * 业务异常
 * 用于封装业务逻辑中的错误情况，便于统一处理业务异常
 * 支持通过错误码和错误消息构造，提供多种便捷的创建方法
 */
@Getter
public class BusinessException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     * 保存业务异常对应的错误码信息
     */
    private final IResultCode resultCode;

    /**
     * 默认构造方法
     * 使用默认的失败错误码
     */
    public BusinessException() {
        super(ResultCode.FAILED.getMessage());
        this.resultCode = ResultCode.FAILED;
    }

    /**
     * 指定错误消息的构造方法
     * 使用默认的失败错误码和自定义错误消息
     * 
     * @param message 自定义错误消息
     */
    public BusinessException(String message) {
        super(message);
        this.resultCode = ResultCode.FAILED;
    }

    /**
     * 指定原因的构造方法
     * 使用默认的失败错误码和原异常
     * 
     * @param cause 原始异常
     */
    public BusinessException(Throwable cause) {
        super(cause);
        this.resultCode = ResultCode.FAILED;
    }

    /**
     * 指定消息和原因的构造方法
     * 使用默认的失败错误码、自定义消息和原异常
     * 
     * @param message 自定义错误消息
     * @param cause   原始异常
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.resultCode = ResultCode.FAILED;
    }

    /**
     * 指定结果码的构造方法
     * 使用自定义结果码对象
     * 
     * @param resultCode 结果码对象
     */
    public BusinessException(IResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }

    /**
     * 指定结果码和原因的构造方法
     * 使用自定义结果码和原异常
     * 
     * @param resultCode 结果码对象
     * @param cause      原始异常
     */
    public BusinessException(IResultCode resultCode, Throwable cause) {
        super(resultCode.getMessage(), cause);
        this.resultCode = resultCode;
    }

    /**
     * 构造方法
     * 使用自定义结果码和消息
     *
     * @param resultCode 结果码对象
     * @param message    自定义错误消息
     */
    public BusinessException(IResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }

    /**
     * 构造方法
     * 使用自定义错误码和消息
     *
     * @param code    自定义错误码
     * @param message 自定义错误消息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.resultCode = new IResultCode() {
            @Override
            public int getCode() {
                return code;
            }

            @Override
            public String getMessage() {
                return message;
            }
        };
    }

    /**
     * 参数错误
     * 创建参数错误类型的业务异常
     *
     * @param message 错误消息
     * @return 业务异常
     */
    public static BusinessException paramError(String message) {
        return new BusinessException(ResultCode.PARAM_ERROR, message);
    }

    /**
     * 数据不存在
     * 创建数据不存在类型的业务异常
     *
     * @param message 错误消息
     * @return 业务异常
     */
    public static BusinessException dataNotFound(String message) {
        return new BusinessException(ResultCode.DATA_NOT_FOUND, message);
    }

    /**
     * 无权限
     * 创建禁止访问类型的业务异常
     *
     * @param message 错误消息
     * @return 业务异常
     */
    public static BusinessException forbidden(String message) {
        return new BusinessException(ResultCode.FORBIDDEN, message);
    }

    /**
     * 未授权
     * 创建未授权类型的业务异常
     *
     * @param message 错误消息
     * @return 业务异常
     */
    public static BusinessException unauthorized(String message) {
        return new BusinessException(ResultCode.UNAUTHORIZED, message);
    }

    /**
     * 系统错误
     * 创建系统错误类型的业务异常
     *
     * @param message 错误消息
     * @return 业务异常
     */
    public static BusinessException systemError(String message) {
        return new BusinessException(ResultCode.SYSTEM_ERROR, message);
    }
} 