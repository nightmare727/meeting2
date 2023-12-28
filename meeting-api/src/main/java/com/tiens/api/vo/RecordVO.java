package com.tiens.api.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/11
 * @Version 1.0
 * @Company: tiens
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecordVO implements Serializable {
    /**
     * 下载鉴权token，下载文件时，使用该token鉴权。（一小时内有效，使用后立即失效）。
     */
    private String token;
    /**
     * 文件类型。
     *
     * Aux：辅流（会议中的共享画面；分辨率为720p） Hd：高清（会议中的视频画面；分辨率和会议中视频画面的分辨率一致，1080p或者720p）
     * Sd：标清（会议中视频画面和共享画面的合成画面，视频画面是大画面，共享画面是小画面，共享画面布局在右下方；分辨率为4CIF） 单个MP4文件大小不超过1GB。
     */
    private String fileType;
    /**
     * 文件下载url，最大1000个字符。
     */
    private String url;
}
