package com.tiens.api.dto;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: 蔚文杰
 * @Date: 2024/1/17
 * @Version 1.0
 * @Company: tiens 推送payload配置
 *
 *     https://doc.yunxin.163.com/messaging/docs/DQyNjc5NjE?platform=server#apns%E6%8E%A8%E9%80%81%E6%B6%88%E6%81%AF
 */
@Data
public class MessagePayloadDTO implements Serializable {
    /**
     * 公用参数，指定推送消息的标题
     */
    private String pushTitle;

    /**
     * APNs 推送消息中的 aps 参数
     */
    private HashMap<String, Object> apsField;
    /**
     * hwPassThroughField
     */
    private HashMap<String, Object> hwPassThroughField;
    /**
     * 华为通知栏消息中的 android.notification 参数
     */
    private HashMap<String, Object> hwField;
    /**
     * 荣耀透传消息中的 Message 参数
     */
    private HashMap<String, Object> honorPassThroughField;
    /**
     * 荣耀通知栏消息中的 Message.android 参数
     */
    private HashMap<String, Object> honorField;
    /**
     * vivo 推送消息的请求体参数
     */
    private HashMap<String, Object> vivoField;
    /**
     * OPPO 推送消息中的 message 参数
     */
    private HashMap<String, Object> oppoField;
    /**
     * 旧版谷歌 FCM 推送消息中的 notification 参数
     */
    private HashMap<String, Object> fcmField;
    /**
     * 新版谷歌 FCM 推送消息中的 notification 参数
     */
    private HashMap<String, Object> fcmFieldV1;

    public MessagePayloadDTO(Map<String, Object> attach) {
        String pushTitle = "V-Moment";

        JSONObject pushData = JSONUtil.createObj().set("landingUrl", "TencentMeetingPage").set("landingType", "2")
            .set("landingArgument", JSONUtil.createObj().set("a", "b"));

        this.setPushTitle(pushTitle);
        /**
         * 苹果离线推送信息 组装
         */
        HashMap<String, Object> apsFieldMap = new HashMap<String, Object>();
        HashMap<String, Object> apsField = new HashMap<String, Object>();
        HashMap<String, Object> alert = new HashMap<String, Object>();

        alert.put("title", pushTitle);
        alert.put("body", attach);

        apsField.put("alert", alert);
        apsField.put("category", "GAME_INVITATION");
        apsField.put("push_data", pushData);
        apsFieldMap.put("apsField", apsField);
        apsFieldMap.put("channel_id", "high_system");
        apsFieldMap.put("pushTitle", pushTitle);

        this.setApsField(apsField);

        /**
         * 华为离线推送信息 组装
         */
        HashMap<String, Object> hwFieldMap = new HashMap<String, Object>();
        HashMap<String, Object> hwField = new HashMap<String, Object>();
        HashMap<String, Object> click_action = new HashMap<String, Object>();
        HashMap<String, Object> badge = new HashMap<String, Object>();
        HashMap<String, Object> androidConfig = new HashMap<String, Object>();
        HashMap<String, Object> hwPushData = new HashMap<String, Object>();

        hwPushData.put("push_data", pushData);

        // 用户设备离线时，Push 服务器对离线消息缓存机制，默认为-1，详见官方文档 AndroidConfig.collapse_key
        androidConfig.put("collapse_key", -1);
        // 透传消息投递优先级，详见官方文档AndroidConfig.urgency
        androidConfig.put("urgency", "NORMAL");
        // 标识消息类型，用于标识高优先级透传场景，详见官方文档 AndroidConfig.category
        androidConfig.put("category", "IM");
        // 自定义消息负载，详见官方文档AndroidConfig.data

        androidConfig.put("data", JSON.toJSONString(hwPushData));

        badge.put("badge", new HashMap<String, Object>());
        // 消息点击行为。type为1：打开应用自定义页面，type为2：点击后打开特定URL，type为3：点击后打开应用
        click_action.put("type", 3);
        // 消息点击行为。type为1：打开应用自定义页面，type为2：点击后打开特定URL，type为3：点击后打开应用
        hwField.put("click_action", click_action);
        // 通知栏样式，取值如下：0：默认样式1：大文本样式 3：Inbox样式
        hwField.put("style", 0);
        // Android通知消息角标控制
        hwField.put("badge", badge);
        hwField.put("androidConfig", androidConfig);
        // 标题
        hwFieldMap.put("pushTitle", pushTitle);
        hwFieldMap.put("hwField", hwField);
        hwFieldMap.put("channel_id", "high_system");

        this.setHwField(hwFieldMap);

        /**
         * vivoField离线推送信息 组装
         */

        HashMap<String, Object> vivoFieldMap = new HashMap<String, Object>();
        HashMap<String, Object> vivoField = new HashMap<String, Object>();
        // 点击跳转类型 1：打开APP首页 2：打开链接 3：自定义 4:打开app内指定页面，默认为1
        vivoField.put("skipType", 1);
        // 跳转内容,与skipType对应
        vivoField.put("skipContent", "");
        // 消息类型 0：运营类消息，1：系统类消息。默认为0
        vivoField.put("classification", "1");
        // 网络方式 -1：不限，1：wifi下发送，不填默认为-1
        vivoField.put("networkType", -1);
        // 推送模式 0：正式推送；1：测试推送，不填默认为0
        vivoField.put("pushMode", "0");
        // 二级分类
        vivoField.put("category", "IM");

        vivoFieldMap.put("channel_id", "high_system");
        vivoFieldMap.put("pushTitle", "V-Moment");
        vivoFieldMap.put("vivoField", vivoField);
        vivoFieldMap.put("push_data", pushData);

        this.setVivoField(vivoFieldMap);

        /**
         * oppo离线推送信息 组装
         */

        HashMap<String, Object> oppoFieldMap = new HashMap<String, Object>();
        HashMap<String, Object> oppoField = new HashMap<String, Object>();
        // 指定下发的通道ID
        oppoField.put("channel_id", "high_system");
        // 点击通知栏后触发的动作类型。0（默认0.启动应用；1.跳转指定应用内页（action标签名）；2.跳转网页；4.跳转指定应用内页（全路径类名）；5.跳转Intent scheme URL: ""
        oppoField.put("click_action_type", "0");
        oppoField.put("click_action_activity", "");
        oppoField.put("click_action_url", "");
        oppoField.put("action_parameters", hwPushData);
        // 通知栏样式
        oppoField.put("style", 1);
        // 子标题
        oppoField.put("sub_title", "");
        // 推送的网络环境类型
        oppoField.put("network_type", 0);

        oppoFieldMap.put("channel_id", "high_system");
        oppoFieldMap.put("pushTitle", "V-Moment");
        oppoFieldMap.put("oppoField", oppoField);

        this.setOppoField(oppoFieldMap);

        /**
         * 谷歌FCM推送消息
         */
        HashMap<String, Object> fcmFieldMap = new HashMap<String, Object>();
        HashMap<String, Object> fcmField = new HashMap<String, Object>();
        fcmField.put("channel_id", "vmoment_im");
        fcmFieldMap.put("pushTitle", "V-Moment");
        fcmFieldMap.put("fcmField", fcmField);
        fcmFieldMap.put("push_data", pushData);
        this.setFcmFieldV1(fcmFieldMap);
    }

}
