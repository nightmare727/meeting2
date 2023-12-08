package common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/7
 * @Version 1.0
 * @Company: tiens
 */
@Getter
@AllArgsConstructor
public enum MeetingRoomHandlerEnum {
    /**
     * 云会议
     */
    CLOUD(1, "cloudMeetingRoomHandler"),
    /**
     * 研讨会
     */
    SEMINAR(2, "seminarMeetingHandler"),
    ;
    private Integer vmrMode;

    private String handlerName;

    public static String getHandlerNameByVmrMode(Integer vmrMode) {
        for (MeetingRoomHandlerEnum value : MeetingRoomHandlerEnum.values()) {
            if (value.getVmrMode().equals(vmrMode)) {
                return value.getHandlerName();
            }
        }
        return null;
    }

    public static void main(String[] args) {

        String handlerNameByVmrMode = getHandlerNameByVmrMode(1);

        System.out.println(handlerNameByVmrMode);
    }
}
