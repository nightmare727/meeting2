package com.tiens.api.vo;

import lombok.Data;
import org.apache.poi.hpsf.Decimal;

import java.io.Serializable;


@Data
public class MeetingPaidSettingVO implements Serializable {

    private Long id;

    /** 资源类型 */
    private Integer resourceType;

    /** VM币 */
    private Integer vmCoin;

    /** 金额 */
    private Decimal money;

    /** 基础时限 */
    private Integer baseLimitTime;

    /** 单次上限 */
    private Integer onceLimit;
}
