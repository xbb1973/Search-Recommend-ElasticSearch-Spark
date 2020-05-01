package slf.xbb.stores.common;

import lombok.Data;

/**
 * @author ：xbb
 * @date ：Created in 2020/3/31 4:48 上午
 * @description：枚举业务error
 * @modifiedBy：
 * @version:
 */
public enum EmBusinessError implements CommonError {

    //通用的错误类型10000开头
    NO_OBJECT_FOUND(10001, "请求对象不存在"),
    // UNKNOWN_ERROR(10002,"未知错误"),
    NO_HANDLER_FOUND(10003, "找不到执行的路径操作"),
    BIND_EXCEPTION_ERROR(10004, "请求参数错误"),

    // PARAMETER_VALIDATION_ERROR(10005,"请求参数校验失败"),

    //用户服务相关的错误类型20000开头
    REGISTER_DUP_FAIL(20001, "用户已存在"),

    LOGIN_FAIL(20002, "手机号或密码错误"),

    // 通用错误
    PARAMETER_VALIDATION_ERROR(500, "参数不合法"),
    // 用户信息错误
    USER_NOT_EXIST(501, "用户不存在"),
    // 未知错误
    UNKNOWN_ERROR(502, "未知错误"),
    //
    USER_LOGIN_ERROR(503, "用户手机号或者密码错误"),
    STOCK_NOT_ENOUGH(504, "库存不足"),
    USER_NOT_LOGIN(505, "用户未登陆"),
    CATEGORY_DUP_ERROR(506, "品类已存在");

    private Integer errCode;
    private String errMsg;

    private EmBusinessError(Integer errCode, String errMsg) {
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    @Override
    public Integer getErrCode() {
        return this.errCode;
    }

    @Override
    public String getErrMsg() {
        return this.errMsg;
    }

    @Override
    public CommonError setErrMsg(String errMsg) {
        this.errMsg = errMsg;
        return this;
    }
}
