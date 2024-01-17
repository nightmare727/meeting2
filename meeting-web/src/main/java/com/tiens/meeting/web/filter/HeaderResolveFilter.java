package com.tiens.meeting.web.filter;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.tiens.api.service.RpcMeetingUserService;
import com.tiens.api.vo.VMUserVO;
import common.pojo.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.redisson.api.RedissonClient;
import org.slf4j.MDC;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static common.exception.enums.GlobalErrorCodeConstants.INVALID_ACC_ID;

/**
 * @Description: 过滤器
 * @Author: hh
 * @version:1.0
 */
@Slf4j
public class HeaderResolveFilter implements Filter {
    @Reference
    RpcMeetingUserService rpcMeetingUserService;

    @Autowired
    RedissonClient redissonClient;

    @Override
    public void init(FilterConfig filterConfig) {

    }

    @Override
    public void destroy() {
    }

    /**
     * 白名单配置
     */
    private static final Set<String> whiteListSet = new HashSet<>();

    static {
        whiteListSet.add("/vmeeting/web/room/openapi/meetingevent");
        whiteListSet.add("/vmeeting/web/mtuser/queryLiveVMUser");
        whiteListSet.add("/vmeeting/web/mtuser/getCredential");
        whiteListSet.add("/vmeeting/web/mtuser/queryMeetingHostUser");
        whiteListSet.add("/vmeeting/web/ping");
        whiteListSet.add("/vmeeting/web/mtuser/addMeetingHostUser");

        //临时增加白名单，注意后期删除
//        whiteListSet.add("/meeting/web/courses/increCoursePv");
//        whiteListSet.add("/meeting/web/courses/subCoursesDetail");
//        whiteListSet.add("/meeting/web/courses/seriesBrief");
//        whiteListSet.add("/meeting/web/courses/seriesSubCoursesList");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest)request;
        HeaderMapRequestWrapper wrapperRequest = new HeaderMapRequestWrapper(httpServletRequest);
        ///meeting/web/callback/receive/image
        final String requestURI = httpServletRequest.getRequestURI();
        if (whiteListSet.contains(requestURI)) {
            chain.doFilter(wrapperRequest, response);
            return;
        }
        for (String s : whiteListSet) {
            if (requestURI.contains(s)) {
                chain.doFilter(wrapperRequest, response);
                return;
            }
        }
        String finalUserId = wrapperRequest.getHeader("finalUserId");
        if (StringUtils.isBlank(finalUserId)) {
            //此参数accid必传
            log.warn("finalUserId:{}，不存在", finalUserId);
            response.getWriter().write(JSON.toJSONString(CommonResult.error(INVALID_ACC_ID)));
            response.flushBuffer();
            return;
        }
        CommonResult<VMUserVO> vmUserVOCommonResult = rpcMeetingUserService.queryVMUser("", finalUserId);
        VMUserVO vmUserVO = vmUserVOCommonResult.getData();
        if (ObjectUtil.isEmpty(vmUserVO)) {
            //仍为null
            log.error("VM数据查询异常，accid:{}", finalUserId);
            response.getWriter().write(JSON.toJSONString(CommonResult.error(INVALID_ACC_ID)));
            response.flushBuffer();
            return;
        }
        Integer levelCode = vmUserVO.getLevelCode();
        String joyoCode = vmUserVO.getJoyoCode();
        String nickName = vmUserVO.getNickName();
        wrapperRequest.addHeader("levelCode", String.valueOf(levelCode));
        wrapperRequest.addHeader("joyoCode", joyoCode);
        wrapperRequest.addHeader("userName", nickName);
        chain.doFilter(wrapperRequest, response);
    }
}
