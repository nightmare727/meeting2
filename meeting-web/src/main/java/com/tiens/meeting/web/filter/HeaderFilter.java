package com.tiens.meeting.web.filter;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @Description: 过滤器
 * @Author: hh
 * @version:1.0
 */
@Slf4j
public class HeaderFilter implements Filter {


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
        whiteListSet.add("/meeting/web/callback/receive/image");
        whiteListSet.add("/meeting/web/callback/receive/videoSolution");
        whiteListSet.add("/meeting/web/ping");

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

        chain.doFilter(wrapperRequest, response);
    }
}
