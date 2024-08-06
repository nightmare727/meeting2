package com.tiens.api.vo;

import common.enums.CheckGroupEnum;
import lombok.Data;

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
    @NotNull(message = "VM币不可以为空")
    private Integer vmCoin;

    /** 金额 */
    @NotNull(message = "金额不可以为空")
    private Double money;

    /** 基础时限 */
    @NotNull(message = "基础时限不可以为空")
    private Double baseLimitTime;

    /** 单次上限 */
    @NotNull(message = "单次上限不可以为空")
    private Double onceLimit;
}
