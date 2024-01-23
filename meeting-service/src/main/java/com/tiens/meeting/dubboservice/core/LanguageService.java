package com.tiens.meeting.dubboservice.core;

/**
 * @Author: 蔚文杰
 * @Date: 2024/1/23
 * @Version 1.0
 * @Company: tiens
 */

public interface LanguageService {
    /**
     * 查询词条value
     *
     * @param languageId
     * @param languageKey
     * @return
     */
    public String getLanguageValue(String languageId, String languageKey);
}
