package common.constants;

import com.google.common.collect.ImmutableMap;

/**
 * @Author: 蔚文杰
 * @Date: 2023/10/20
 * @Version 1.0
 */
public class YunxingCallBackConstants {
    public static ImmutableMap<Integer, String> IMG_CHECK_RESULT =
        ImmutableMap.<Integer, String>builder().put(100, "色情").put(110, "性感低俗").put(200, "广告")
            .put(210, "二维码").put(260, "广告法").put(300, "暴恐").put(400, "违禁").put(500, "涉政").put(800, "恶心类")
            .put(900, "其他").put(1100, "涉价值观").build();
    public static ImmutableMap<Integer, String> VIDEO_AUDIO_CHECK_RESULT =
        ImmutableMap.<Integer, String>builder().put(0, "正常").put(100, "色情").put(110, "性感低俗").put(200, "广告")
            .put(210, "二维码").put(260, "广告法").put(300, "暴恐").put(400, "违禁").put(500, "涉政").put(600, "谩骂")
            .put(700, "灌水").put(800, "恶心类").put(900, "其他").put(1020, "黑屏").put(1030, "挂机").put(1050, "噪声")
            .put(1100, "涉价值观").build();

    public static void main(String[] args) {
        System.out.println(IMG_CHECK_RESULT);

    }
}
