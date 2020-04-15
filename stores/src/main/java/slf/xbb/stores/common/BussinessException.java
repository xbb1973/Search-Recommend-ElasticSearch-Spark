package slf.xbb.stores.common;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author ：xbb
 * @date ：Created in 2020/3/31 4:55 上午
 * @description：包装器业务异常类实现
 * @modifiedBy：
 * @version:
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BussinessException extends Exception{

    private CommonError commonError;

    /**
     * 直接接收commonError的传参用于构造业务异常
     * @param commonError
     */
    public BussinessException(CommonError commonError) {
        super();
        this.commonError = commonError;
    }

    /**
     * 自定义errMsg构造业务异常
     * @param commonError
     */
    public BussinessException(CommonError commonError, String errMsg) {
        super();
        this.commonError = commonError;
        this.commonError.setErrMsg(errMsg);
    }

}
