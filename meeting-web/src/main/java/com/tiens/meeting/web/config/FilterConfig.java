package com.tiens.meeting.web.config;

import com.tiens.meeting.web.filter.AuthFilter;
import com.tiens.meeting.web.filter.HeaderResolveFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    /**
     * web应用启动的顺序是：listener->filter->servlet，先初始化listener，然后再来就filter的初始化，再接着才到我们的dispathServlet的初始化，因此，当我们需要在filter
     * 里注入一个注解的bean时，就会注入失败，因为filter初始化时，注解的bean还没初始化，没法注入。 所以需要创建filter的时候 需要@Bean初始化一下
     *
     * @return
     */

    @Bean
    public FilterRegistrationBean<HeaderResolveFilter> filterRegistrationBeanHeaderResolveFilter() {
        FilterRegistrationBean<HeaderResolveFilter> filter = new FilterRegistrationBean();
        filter.setFilter(headerResolveFilter());
        filter.addUrlPatterns("/*");
        filter.setName("headerResolveFilter");
        filter.setOrder(FilterRegistrationBean.HIGHEST_PRECEDENCE);
        return filter;
    }

    @Bean
    public FilterRegistrationBean<AuthFilter> filterRegistrationBeanAuthFilter() {
        FilterRegistrationBean<AuthFilter> filter = new FilterRegistrationBean();
        filter.setFilter(authFilter());
        filter.addUrlPatterns("/mtuser/addMeetingHostUser");
        filter.setName("authFilter");
        filter.setOrder(1);
        return filter;
    }

    @Bean("authFilter")
    AuthFilter authFilter() {
        return new AuthFilter();
    }

    @Bean("headerResolveFilter")
    HeaderResolveFilter headerResolveFilter() {
        return new HeaderResolveFilter();
    }


    @Bean("headerResolveFilter")
    HeaderResolveFilter headerResolveFilter() {
        return new HeaderResolveFilter();
    }

}
