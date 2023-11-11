package common.util.cache;

public class CacheKeyUtil {

    /**
     * 基础缓存前缀
     */
    public static final String BASE_CACHE_PREFIX = "vmmoment-meeting:";

    /**
     * 分隔符
     */
    private static final String SPIT = ":";

    /**
     * 单课学习pv
     */
    public static final String SUB_COURSE_LEARN_COUNT_PREFIX = "sub-course-learn-count:";
    /**
     * 单课课程详情
     */
    public static final String SUB_COURSE_DETAIL_PREFIX = "course-detail:";
    /**
     * 系列单课列表
     */
    public static final String SERIES_SUB_COURSE_LIST_PREFIX = "series-sub-course-list:";
    /**
     * 系列课简介
     */
    public static final String SERIES_KEY_PREFIX = "series-brief:";

    /**
     * im 用户缓存
     */
    public static final String IM_USER_KEY_PREFIX = "im-meeting-user:";

    /**
     * 获取学习次数
     *
     * @param courseId
     * @param courseType
     * @return
     */
    public static String getLearnCountKey(Long courseId, Integer courseType) {
        return new StringBuilder(BASE_CACHE_PREFIX).append(SUB_COURSE_LEARN_COUNT_PREFIX).append(courseType)
            .append(SPIT)
            .append(courseId).toString();
    }

    /**
     * 获取单课详情
     *
     * @param courseId
     * @param courseType
     * @return
     */
    public static String getSubCourseDetailKey(Long courseId, Integer courseType) {

        return new StringBuilder(BASE_CACHE_PREFIX).append(SUB_COURSE_DETAIL_PREFIX).append(courseType).append(SPIT)
            .append(courseId).toString();
    }

    /**
     * 获取序列单课列表
     *
     * @param courseId
     * @return
     */
    public static String getSeriesSubCoursesListKey(Long courseId) {
        return new StringBuilder(BASE_CACHE_PREFIX).append(SERIES_SUB_COURSE_LIST_PREFIX)
            .append(courseId).toString();

    }

    /**
     * 获取序列课简介key
     *
     * @param courseId
     * @return
     */
    public static String getSeriesBriefKey(Long courseId) {
        return new StringBuilder(BASE_CACHE_PREFIX).append(SERIES_KEY_PREFIX)
            .append(courseId).toString();

    }

    public static String getUserInfoKey(String imUserId) {
        return new StringBuilder(BASE_CACHE_PREFIX).append(IM_USER_KEY_PREFIX)
            .append(imUserId).toString();
    }
}
