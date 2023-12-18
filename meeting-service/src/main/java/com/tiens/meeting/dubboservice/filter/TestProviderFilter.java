package com.tiens.meeting.dubboservice.filter;

import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

import java.util.Map;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/18
 * @Version 1.0
 * @Company: tiens
 */
@Activate(group = {CommonConstants.PROVIDER})
public class TestProviderFilter implements Filter {
    /**
     * Does not need to override/implement this method.
     *
     * @param invoker
     * @param invocation
     */
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // 在这里获取Dubbo上下文中的自定义数据
        Map<String, String> context = RpcContext.getContext().getAttachments();

        System.out.println(context);
        // 调用下一个过滤器或服务提供者/消费者
        Result result = invoker.invoke(invocation);

        return result;
    }
}
