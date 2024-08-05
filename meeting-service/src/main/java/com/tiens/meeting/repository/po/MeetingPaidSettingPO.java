package com.tiens.meeting.repository.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@TableName(value = "meeting_paid_setting")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MeetingPaidSettingPO implements Serializable {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 资源类型 */
    @TableField(value = "resource_type", updateStrategy = FieldStrategy.NEVER)
    private Integer resourceType;

    /** VM币 */
    @TableField(value = "vm_coin")
    private Integer vmCoin;

    /** 金额 */
    @TableField(value = "money")
    private Double money;

    /** 基础时限 */
    @TableField(value = "base_limit_time")
    private Integer baseLimitTime;

    /** 单次上限 */
    @TableField(value = "once_limit")
    private Integer onceLimit;
}
