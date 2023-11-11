package com.tiens.meeting.struct;

import java.util.List;

/**
 * @Author: 蔚文杰
 * @Date: 2023/10/20
 * @Version 1.0
 */
public interface BaseConverter<S, T> {
    T toTarget(S var1);

    S toSource(T var1);

    List<T> toTargetList(List<S> var1);

    List<S> toSourceList(List<T> var1);
}