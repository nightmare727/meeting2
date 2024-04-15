package com.tiens.meeting.dubboservice.bo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author hh
 * @date 2021/6/9 10:24
 */
@Data
@ApiModel(value = "VM主播信息request", description = "VM主播信息request")
public class UpdateVMAnchorInfoRequest implements Serializable {

    private static final long serialVersionUID = 7540106642239470182L;

    /**
     * 头像
     */
    @ApiModelProperty(value = "头像")
    private String headUrl;

    /**
     * 主播昵称
     */
    @ApiModelProperty(value = "主播昵称")
    private String anchorName;
    /**
     * 卓越卡号
     */
    private String joyoCode;

}
