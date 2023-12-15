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
     * 华为会议事件同步异常次数记录
     *
     * @param meetingRoomCode
     * @return
     */
    public static String getHwMeetingRoomMaxSyncKey(String meetingRoomCode) {
        return new StringBuilder(BASE_CACHE_PREFIX).append(HW_ROOM_EVENT_SYNC_PREFIX).append(meetingRoomCode)
            .toString();
    }

}
