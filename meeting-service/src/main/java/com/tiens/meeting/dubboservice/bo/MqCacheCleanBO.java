package com.tiens.meeting.dubboservice.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.redisson.api.RType;

import java.io.Serializable;

/**
 * @Author: 蔚文杰
 * @Date: 2023/11/6
 * @Version 1.0
 * @Company: tiens
 *
 *     缓存清除
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MqCacheCleanBO implements Serializable {

    private String topic;

    /**
     * redis key的类型 1：string  2:hash 。。。。。。。
     */
    private RType keyType;
    /**
     * redis  key
     */
    private String key;
    /**
     * hash 结构中field的值
     */
    private String field;

}
