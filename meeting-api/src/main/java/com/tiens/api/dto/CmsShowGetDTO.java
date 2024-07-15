package com.tiens.api.dto;

import common.enums.TerminalEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: 蔚文杰
 * @Date: 2024/7/4
 * @Version 1.0
 * @Company: tiens
 */
@Data
public class CmsShowGetDTO implements Serializable {
    /**
     * 设备类型 1:安卓 2：IOS 3: Windows 4:MAC
     *
     * @see TerminalEnum
     */
    private Integer deviceType;


    private String nationId;
}
