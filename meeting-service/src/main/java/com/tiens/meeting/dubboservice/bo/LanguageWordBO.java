package com.tiens.meeting.dubboservice.bo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: 蔚文杰
 * @Date: 2024/1/24
 * @Version 1.0
 * @Company: tiens
 */
@Data
@AllArgsConstructor
public class LanguageWordBO implements Serializable {
    /**
     * 词条key
     */
    private String wordKey;
    /**
     * 词条value
     */
    private String wordValue;

}
