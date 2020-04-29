package slf.xbb.stores.common;

import lombok.Data;

import java.util.List;

/**
 * @author ：xbb
 * @date ：Created in 2020/3/31 4:03 上午
 * @description：归一化返回给前端的数据格式{status,data}
 * @modifiedBy：
 * @version:
 */
@Data
public class CommonReturnType{
    /**
     * status表示返回的结果
     * {success, fail}
     */
    private String status;
    /**
     * status==success, data=VoJson
     * status==fail, data=CommonReturntype
     */
    private Object data;

    // private List<Object> dataList;

    public static CommonReturnType create(Object result) {
        return CommonReturnType.create(result, "success");
    }

    // public static CommonReturnType create(List<Object> result) {
    //     return CommonReturnType.create(result, "success");
    // }

    public static CommonReturnType create(Object result, String status) {
        CommonReturnType type = new CommonReturnType();
        type.setStatus(status);
        type.setData(result);
        return type;
    }

    // public static CommonReturnType create(List<Object> result, String status) {
    //     CommonReturnType type = new CommonReturnType();
    //     type.setStatus(status);
    //     type.setDataList(result);
    //     return type;
    // }
}
