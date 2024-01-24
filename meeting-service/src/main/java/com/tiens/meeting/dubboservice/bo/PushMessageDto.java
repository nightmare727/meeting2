package com.tiens.meeting.dubboservice.bo;

import lombok.Data;

import java.util.List;

@Data
public class PushMessageDto {
    /**
     * 发送人ID
     */
    private String accId;
    /**
     * 1:会议，2：认证
     */
    private Integer msgType;
    /**
     * 收信人ID
     */
    private List<String> to;
    /**
     * 标题
     */
    private String title;
    /**
     * 内容
     */
    private Object content;
    /**
     * 消息体
     */
    private Object body;
    private Object payload;
    /**
     * 推送内容
     */
    private String pushContent;
    /**
     * 自定义内容
     */
    private Object ext;
}
