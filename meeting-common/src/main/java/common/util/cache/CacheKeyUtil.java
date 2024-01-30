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
    private static final String HW_USER_KEY_PREFIX = "im-hw-user-flag";
    private static final String HW_ROOM_EVENT_SYNC_PREFIX = "hw_room_event_count:";
    private static final String LANGUAGE_ID_PREFIX = "language-word:";

    private static final String HW_ROOM_RESOURCE_LOCK_PREFIX = "room_resource_lock:";

    private static final String HW_ROOM_STOP_LOCK_PREFIX = "room_stop_lock:";

    public static String getUserInfoKey(String imUserId) {
        return new StringBuilder(BASE_CACHE_PREFIX).append(IM_USER_KEY_PREFIX).append(imUserId).toString();
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

}
