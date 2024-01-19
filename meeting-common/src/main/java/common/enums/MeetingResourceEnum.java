package common.enums;

import lombok.Getter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: 谷守丙
 * @Date: 2023/12/5
 * @Version 1.0 1:10方 2:50方 3:100方 4:200方 5:500方 6:1000方 7:3000方
 */
public enum MeetingResourceEnum implements Serializable {

    MEETING_RESOURCE_0(0, 0, "无", ""), MEETING_RESOURCE_10(1, 10, "普通会议(适用10人以下)", "moment_meeting_back_020"),
    MEETING_RESOURCE_50(2, 50, "中型会议(适用50人以下)", "moment_meeting_back_021"),
    MEETING_RESOURCE_100(3, 100, "中型会议(适用100人以下)", "moment_meeting_back_022"),
//    MEETING_RESOURCE_200(4, 200, "大型会议(适用200人以下)", "moment_meeting_back_023"),
    MEETING_RESOURCE_500(5, 500, "大型会议(适用500人以下)", "moment_meeting_back_024"),
    MEETING_RESOURCE_1000(6, 1000, "超大型会议(适用1000人以下)", "moment_meeting_back_025"),
    MEETING_RESOURCE_3000(7, 3000, "超大型会议(适用3000人以下)", "moment_meeting_back_026");
    private static Map<Integer, MeetingResourceEnum> CodesMap = new HashMap<>(4);

    static {
        for (MeetingResourceEnum status : MeetingResourceEnum.values()) {
            CodesMap.put(status.code, status);
        }
    }

    public static String specialResourceKey = "moment_meeting_back_027";
    @Getter
    private int code;
    @Getter
    private int value;
    @Getter
    private String desc;
    @Getter
    private String wordKey;

    MeetingResourceEnum(int code, int value, String desc, String wordKey) {
        this.code = code;
        this.value = value;
        this.desc = desc;
        this.wordKey = wordKey;
    }

    public static MeetingResourceEnum getByCode(int code) {
        return CodesMap.get(code);
    }

    public static MeetingResourceEnum getBySize(int size) {
        for (MeetingResourceEnum value : values()) {
            if (value.getValue() == size) {
                return value;
            }
        }
        return null;
    }
}
