package com.tiens.meeting.repository.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisConfig {

    @Bean
    public MybatisPlusInterceptor paginationInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    @Bean
    public PaginationInnerInterceptor paginationInnerInterceptor() {
        return new PaginationInnerInterceptor();
    }





/*    @Bean
    public SqlSessionTemplate sqlSessionTemplate() throws Exception {
        SqlSessionTemplate sqlSessionTemplate =
            new SqlSessionTemplate(sqlSessionFactoryBean().getObject(), ExecutorType.BATCH);
        return sqlSessionTemplate;
    }*/

  /*  @Primary // 表示这个数据源是默认数据源, 这个注解必须要加，因为不加的话spring将分不清楚那个为主数据源（默认数据源）
    @Bean("db1DataSource")
    @ConfigurationProperties(prefix = "spring.shardingsphere.datasource.master") //读取application.yml中的配置参数映射成为一个对象
    public DataSource getDb1DataSource() {
        final DruidDataSource druidDataSource = DruidDataSourceBuilder.create().build();
        return druidDataSource;
    }*/

   /* @Primary
    @Bean("db1SqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource, GlobalConfig globalConfig) throws Exception {
        //使用 mybatis plus 配置
        MybatisSqlSessionFactoryBean mybatisSqlSessionFactoryBean = new MybatisSqlSessionFactoryBean();
        mybatisSqlSessionFactoryBean.setDataSource(dataSource);
        // mapper的xml形式文件位置必须要配置，不然将报错：no statement （这种错误也可能是mapper的xml中，namespace与项目的路径不一致导致）
        mybatisSqlSessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources(
            "classpath:mappers/*.xml"));
        mybatisSqlSessionFactoryBean.setGlobalConfig(globalConfig);
        final SqlSessionFactory sqlSessionFactory = mybatisSqlSessionFactoryBean.getObject();
        return sqlSessionFactory;
    }*/

  /*  @Bean
    public GlobalConfig globalConfig(MPJSqlInjector mpjSqlInjector) {
        GlobalConfig config = new GlobalConfig();
        //绑定 mpjSqlInjector
        config.setSqlInjector(mpjSqlInjector);
        //其他配置 略
        return config;
    }*/
}
