package common.log;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.slf4j.MDC;

import java.util.Map;

import static common.log.LogInterceptor.TRACE_ID;

/**
 * @Author: 蔚文杰
 * @Date: 2023/12/15
 * @Version 1.0
 * @Company: tiens
 */
@Slf4j
@Activate(group = {CommonConstants.CONSUMER, CommonConstants.PROVIDER}, order = Integer.MAX_VALUE)
public class LogDubboFilter extends ListenableFilter {

    public LogDubboFilter() {
        super.listener = new LogListener();
    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // 在这里获取Dubbo上下文中的自定义数据
        RpcContext context = RpcContext.getContext();
        Map<String, String> customData = context.getAttachments();

        // 使用自定义数据进行业务处理
        if (context.isConsumerSide()) {
            //消费者端
            String traceId = ObjectUtil.defaultIfBlank(MDC.get(TRACE_ID), UUID.fastUUID().toString(true));

            customData.put(TRACE_ID, traceId);
        } else if (context.isProviderSide()) {
            String traceId = customData.get(TRACE_ID);

            MDC.put(TRACE_ID, traceId);

        }
        // 调用下一个过滤器或服务提供者/消费者
        Result result = invoker.invoke(invocation);

        return result;
    }

    static class LogListener implements Listener {
        @Override
        public void onResponse(Result appResponse, Invoker<?> invoker, Invocation invocation) {
            RpcContext context = RpcContext.getContext();
            MDC.remove(TRACE_ID);

            // 使用自定义数据进行业务处理
//            if (context.isProviderSide()) {
//            }

        }

        @Override
        public void onError(Throwable t, Invoker<?> invoker, Invocation invocation) {
            RpcContext context = RpcContext.getContext();
            // 使用自定义数据进行业务处理
          /*  if (context.isProviderSide()) {

            }*/

            String methodPath = invoker.getInterface().getName() + "." + invocation.getMethodName();
            log.error("[DUBBO调用异常],接口：{}，异常信息：{}", methodPath, t);
            MDC.remove(TRACE_ID);
        }
    }
}
