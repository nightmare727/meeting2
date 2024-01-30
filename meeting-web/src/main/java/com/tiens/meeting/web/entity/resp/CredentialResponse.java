package com.tiens.meeting.web.entity.resp;

import com.huaweicloud.sdk.iam.v3.model.Credential;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * @Author: 蔚文杰
 * @Date: 2023/3/9
 * @Version 1.0
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "权限返回")
public class CredentialResponse extends Credential {
    private String endPoint;

    private String bucket;
    private String accessDomain;

    private String acceleratedDomain;

}
