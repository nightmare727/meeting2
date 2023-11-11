//package com.tiens.meeting.mgr.request;
//
//import common.enums.FileTypeEnum;
//import common.validation.InEnum;
//import io.swagger.v3.oas.annotations.media.Schema;
//import lombok.*;
//
//import javax.validation.Valid;
//import javax.validation.constraints.NotBlank;
//import javax.validation.constraints.NotEmpty;
//import java.io.Serializable;
//import java.util.List;
//
///**
// * @Author: 蔚文杰
// * @Date: 2023/3/6
// * @Version 1.0
// */
//@Data
//@ToString
//@AllArgsConstructor
//@NoArgsConstructor
//@Builder
//@Schema(description = "批量添加文件")
//public class BatchAddFileRequest implements Serializable {
//
//    private static final long serialVersionUID = -1L;
//    /**
//     * 文件类型 1：视频 2：文件  3：其他
//     */
//    @InEnum(FileTypeEnum.class)
//    @Schema(description = "文件类型 1：视频 2：图片  3：其他")
//    private Integer fileType;
//
//    /**
//     * 创建人id
//     */
//    @Schema(description = "创建人id")
//    private String createUserId;
//    /**
//     * 更新人id
//     */
//    @Schema(description = "更新人id")
//    private String updateUserId;
//    /**
//     * 创建人名称
//     */
//    @Schema(description = "创建人名称")
//    private String createUserName;
//
//    /**
//     * 国家编码集合
//     */
//    private List<String> countrys;
//    @Schema(description = "批量文件信息")
//
//    @Valid
//    @NotEmpty(message = "文件集合不允许为空")
//    private List<BatchAddFileInner> fileInnerList;
//
//    @Data
//    @AllArgsConstructor
//    @NoArgsConstructor
//    @Builder
//    public static class BatchAddFileInner implements Serializable {
//        /**
//         * 素材名称
//         */
//        @Schema(description = "素材名称")
//        @NotBlank(message = "素材名称不能为空")
//        private String fileName;
//
//        @Schema(description = "素材路径")
//        @NotBlank(message = "素材路径不能为空")
//        private String fileUrl;
//        /**
//         * 分辨率
//         */
//        @Schema(description = "分辨率")
//        private String resolution;
//        /**
//         * 文件格式
//         */
//        @Schema(description = "文件格式")
//        private String fileFormat;
//
//        /**
//         * 文件md5值
//         */
//        @Schema(description = "文件md5值")
//        private String fileMd5;
//        /**
//         * 文件大小 （MB）
//         */
//        @Schema(description = "文件大小 （MB）")
//        @NotBlank(message = "文件大小不能为空")
//        private String fileSize;
//
//        /**
//         * 封面图
//         */
//        @Schema(description = "封面图")
//        private String coverPic;
//
//        /**
//         * 持续时长
//         */
//        private Integer duration;
//    }
//}
//
