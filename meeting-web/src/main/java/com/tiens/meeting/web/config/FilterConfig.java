package com.tiens.meeting.web.config;

import com.tiens.meeting.web.filter.HeaderFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;

@Configuration
public class FilterConfig {

    /**
     * web应用启动的顺序是：listener->filter->servlet，先初始化listener，然后再来就filter的初始化，再接着才到我们的dispathServlet的初始化，因此，当我们需要在filter
     * 里注入一个注解的bean时，就会注入失败，因为filter初始化时，注解的bean还没初始化，没法注入。
     * 所以需要创建filter的时候 需要@Bean初始化一下
     *
     * @return
     */

    @Bean
    public FilterRegistrationBean registrationBean() {
        FilterRegistrationBean<Filter> filter = new FilterRegistrationBean();
        filter.setFilter(headerFilter());
        filter.addUrlPatterns("/*");
        filter.setName("headerFilter");
        filter.setOrder(FilterRegistrationBean.HIGHEST_PRECEDENCE);
        return filter;
    }

    @Bean
    public HeaderFilter headerFilter() {
        return new HeaderFilter();
    }

}
