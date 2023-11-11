package common.util.object;

import common.pojo.PageParam;

/**
 * {@link PageParam} 工具类
 */
public class PageUtils {

    public static int getStart(PageParam pageParam) {
        return (pageParam.getPageNum() - 1) * pageParam.getPageSize();
    }

}
