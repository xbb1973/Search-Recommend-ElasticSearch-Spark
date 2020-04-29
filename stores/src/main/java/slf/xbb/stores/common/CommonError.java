package slf.xbb.stores.common;

/**
 * @author ：xbb
 * @date ：Created in 2020/3/31 4:45 上午
 * @description：CommonError
 * @modifiedBy：
 * @version:
 */
public interface CommonError {
    public Integer getErrCode();
    public String getErrMsg();
    public CommonError setErrMsg(String errMsg);
}
