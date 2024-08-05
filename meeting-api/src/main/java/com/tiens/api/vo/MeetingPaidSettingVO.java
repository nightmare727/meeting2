package com.tiens.api.vo;

import common.enums.CheckGroupEnum;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;


@Data
public class MeetingPaidSettingVO implements Serializable {

    @NotNull(message = "资源类型不可以为空", groups = CheckGroupEnum.Modify.class)
    private Long id;

    /** 资源类型 */
    @NotNull(message = "资源类型不可以为空")
    private Integer resourceType;

    /** VM币 */
    @Min(value = 1, message = "VM币最小值为 1")
    private Integer vmCoin;

    /** 金额 */
    @Min(value = 1, message = "金额最小值为 1")
    private Double money;

    /** 基础时限 */
    @Min(value = 1, message = "基础时限最小值为 1")
    private Integer baseLimitTime;

    /** 单次上限 */
    @Min(value = 1, message = "单次上限最小值为 1")
    private Integer onceLimit;
}
