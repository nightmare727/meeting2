package com.tiens.meeting.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * @Author: 蔚文杰
 * @Date: 2024/1/10
 * @Version 1.0
 * @Company: tiens
 */
public interface CommonMapper<T> extends BaseMapper<T> {
    /**
     * 真正的批量插入
     *
     * @param entityList
     * @return
     */
    int insertBatchSomeColumn(List<T> entityList);
}
