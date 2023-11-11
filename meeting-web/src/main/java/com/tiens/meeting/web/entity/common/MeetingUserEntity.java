package com.tiens.meeting.web.entity.common;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: 蔚文杰
 * @Date: 2023/10/30
 * @Version 1.0
 */
@Data
public class MeetingUserEntity implements Serializable {

    /**
     * 事件操作者id（同企业用户才返回企业用户唯一id，OAuth用户返回openId,rooms返回roomsId）
     */
    @Expose
    @SerializedName("userid")
    private String userId;
    /**
     *
     */
    @Expose
    @SerializedName("open_id")
    private String openId;
    /**
     * 操作者名称
     */
    @Expose
    @SerializedName("user_name")
    private String userName;
    /**
     * 用户身份ID
     */
    @Expose
    @SerializedName("uuid")
    private String uuId;
    /**
     *
     */
    @Expose
    @SerializedName("ms_open_id")
    private String msOpenId;
    /**
     * 用户的终端设备类型
     */
    @Expose
    @SerializedName("instance_id")
    private String instanceId;
}
