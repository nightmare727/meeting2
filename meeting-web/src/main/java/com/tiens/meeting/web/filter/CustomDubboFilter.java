package com.tiens.meeting.web.filter;

import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

import java.util.Map;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/15
 * @Version 1.0
 * @Company: tiens
 */
@Activate(group = {CommonConstants.CONSUMER})
public class CustomDubboFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // 在这里获取Dubbo上下文中的自定义数据
        Map<String, String> customData = RpcContext.getContext().getAttachments();

        // 使用自定义数据进行业务处理
        if (customData != null) {
            // ...
            customData.put("wenjie", "i am consumer1");
        }

        // 调用下一个过滤器或服务提供者/消费者
        Result result = invoker.invoke(invocation);

        return result;
    }

    @Override
    public Result onResponse(Result result, Invoker<?> invoker, Invocation invocation) throws RpcException {
        // 在这里处理响应结果，如果需要的话
        return result;
    }
}
