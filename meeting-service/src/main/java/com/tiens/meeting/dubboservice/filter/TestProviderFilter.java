package com.tiens.meeting.dubboservice.filter;

import lombok.Data;
import org.apache.dubbo.rpc.*;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/18
 * @Version 1.0
 * @Company: tiens
 */
@Data
public class TestProviderFilter implements Filter {
    /**
     * Does not need to override/implement this method.
     *
     * @param invoker
     * @param invocation
     */
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        return null;
    }
}
