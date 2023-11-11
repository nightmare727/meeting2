//package com.tiens.meeting.mgr.response;
//
//import lombok.*;
//
//import java.io.Serializable;
//import java.util.Date;
//
///**
// * @author generator
// * @version 1.0
// * @Title: FileResponse
// * @Description: 文件表
// * @date 2023-02-25 09:34:43
// */
//@Data
//@ToString
//@AllArgsConstructor
//@NoArgsConstructor
//@Builder
//public class FileResponse implements Serializable {
//
//    private static final long serialVersionUID = -1L;
//    private Long id;
//    /**
//     * 素材名称
//     */
//    private String fileName;
//    /**
//     * 分辨率
//     */
//    private String resolution;
//    /**
//     * 文件格式
//     */
//    private String fileFormat;
//    /**
//     * 文件类型 1：视频 2：文件  3：其他
//     */
//    private Integer fileType;
//    /**
//     * 文件md5值
//     */
//    private String fileMd5;
//    /**
//     * 文件大小 （MB）
//     */
//    private String fileSize;
//    /**
//     * 审批状态 1：待提审 2：审核中 3：审核通过 4：审核驳回
//     */
//    private Integer approveStatus;
//    /**
//     * 下载次数
//     */
//    private Integer downloadCount;
//    /**
//     * 权限等级
//     */
//    private Integer authLevel;
//    /**
//     * 封面图
//     */
//    private String coverPic;
//    /**
//     * 是否被删除
//     */
//    private Integer isDeleted;
//    /**
//     * 更新时间
//     */
//    private Date updateTime;
//    /**
//     * 创建时间
//     */
//    private Date createTime;
//    /**
//     * 创建人id
//     */
//    private String createUserId;
//    /**
//     * 更新人id
//     */
//    private String updateUserId;
//    /**
//     * 创建人名称
//     */
//    private String createUserName;
//
//}
