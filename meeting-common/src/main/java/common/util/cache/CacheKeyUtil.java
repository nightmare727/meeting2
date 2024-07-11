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
    private static final String IM_USER_KEY_PREFIX = "im-meeting-user:";
    private static final String IM_LOGIN_USER_KEY_PREFIX = "im-login-meeting-user:";
    private static final String IM_NEW_COMER_USER_KEY_PREFIX = "im-meeting-new_comer_user:";
    private static final String HW_USER_KEY_PREFIX = "im-hw-user-flag";

    private static final String PROFIT_COMMON_CONFIG = "profit-common-config";

    private static final String MEETING_KEY_PREFIX = "meetingCache";
    private static final String HW_ROOM_EVENT_SYNC_PREFIX = "hw_room_event_count:";
    private static final String LANGUAGE_ID_PREFIX = "language-word:";

    private static final String HW_ROOM_RESOURCE_LOCK_PREFIX = "room_resource_lock:";

    private static final String HW_ROOM_STOP_LOCK_PREFIX = "room_stop_lock:";

    private static final String MEMBER_PROFIT_CONFIG = "memberProfitConfig";
    private static final String PROFIT_PRODUCT_LIST = "profitProductList";

    public static String getUserInfoKey(String imUserId) {
        return new StringBuilder(BASE_CACHE_PREFIX).append(IM_USER_KEY_PREFIX).append(imUserId).toString();
    }

    public static String getLoginUserInfoKey(String imUserId) {
        return new StringBuilder(BASE_CACHE_PREFIX).append(IM_LOGIN_USER_KEY_PREFIX).append(imUserId).toString();
    }

    public static String getBaseLoginUserInfoKey() {
        return new StringBuilder(BASE_CACHE_PREFIX).append(IM_LOGIN_USER_KEY_PREFIX).toString();
    }

    public static String getNewComerTaskUserKey(String imUserId) {
        return new StringBuilder(BASE_CACHE_PREFIX).append(IM_NEW_COMER_USER_KEY_PREFIX).append(imUserId).toString();
    }

    /**
     * 华为用户同步
     *
     * @param
     * @return
     */
    public static String getHwUserSyncKey() {
        return new StringBuilder(BASE_CACHE_PREFIX).append(HW_USER_KEY_PREFIX).toString();
    }

    public static String getProfitCommonConfigKey() {
        return new StringBuilder(BASE_CACHE_PREFIX).append(PROFIT_COMMON_CONFIG).toString();
    }

    /**
     * 会议缓存
     *
     * @param
     * @return
     */
    public static String getMeetingCacheKey() {
        return new StringBuilder(BASE_CACHE_PREFIX).append(MEETING_KEY_PREFIX).toString();
    }

    /**
     * 资源分布式锁
     *
     * @param
     * @return
     */
    public static String getResourceLockKey(Integer resourceId) {
        return new StringBuilder(BASE_CACHE_PREFIX).append(HW_ROOM_RESOURCE_LOCK_PREFIX).append(resourceId).toString();
    }

    /**
     * 会议结束分布式锁
     *
     * @param
     * @return
     */
    public static String getMeetingStopLockKey(String meetingCode) {
        return new StringBuilder(BASE_CACHE_PREFIX).append(HW_ROOM_STOP_LOCK_PREFIX).append(meetingCode).toString();
    }

    /**
     * 华为会议事件同步异常次数记录
     *
     * @param meetingRoomCode
     * @return
     */
    public static String getHwMeetingRoomMaxSyncKey(String meetingRoomCode) {
        return new StringBuilder(BASE_CACHE_PREFIX).append(HW_ROOM_EVENT_SYNC_PREFIX).append(meetingRoomCode)
            .toString();
    }

    /**
     * 查询多语言词条
     *
     * @param languageId
     * @param languageKey
     * @return
     */
    public static String getLanguageKey(String languageId, String languageKey) {
        return new StringBuilder(BASE_CACHE_PREFIX).append(LANGUAGE_ID_PREFIX).append(languageId).append(SPIT)
            .append(languageKey).toString();
    }

    public static String getMemberProfitConfigKey() {
        return new StringBuilder(BASE_CACHE_PREFIX).append(MEMBER_PROFIT_CONFIG).toString();
    }

    public static String getProfitProductListKey() {
        return new StringBuilder(BASE_CACHE_PREFIX).append(PROFIT_PRODUCT_LIST).toString();
    }
}
