package com.tiens.meeting.dubboservice.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Redission配置类
 */
@Configuration
@ConditionalOnClass(Redisson.class)
public class RedissonConfig {

    @Autowired
    private Environment environment;

    @Value("${spring.redis.host}")
    private String address;
    @Value("${spring.redis.port}")
    private String port;
    @Value("${spring.redis.database}")
    private int database;

    Logger log = LoggerFactory.getLogger(RedissonConfig.class);

    /**
     * 单机模式自动装配
     *
     * @return
     */
    @Bean
    @ConditionalOnProperty(name = "spring.redis.host")
    RedissonClient redissonSingle() {
        Config config = new Config();
        SingleServerConfig serverConfig =
            config.useSingleServer().setAddress("redis://" + address + ":" + port).setDatabase(database)
                .setTimeout(3000).setConnectionPoolSize(200).setConnectionMinimumIdleSize(50)
                .setSubscriptionConnectionPoolSize(300);
        return Redisson.create(config);
    }

    @Bean
    @ConditionalOnProperty(name = "spring.redis.cluster")
    public RedissonClient redissonClient() {
        RedissonClient redissonClient;

        Config config = new Config();
//        config.setCodec(new SerializationCodec());
        config.setCodec(new org.redisson.client.codec.StringCodec());
        String nodes = environment.getProperty("spring.redis.cluster.nodes");
        if (nodes == null || nodes.equals("")) {
            log.error("redisson 初始化失败，无法获取redis集群节点");
            return null;
        }
        String[] nodeArray = nodes.split(",");
        String[] redisAddrs = new String[nodeArray.length];
        for (int i = 0; i < nodeArray.length; i++) {
            redisAddrs[i] = "redis://" + nodeArray[i];
        }
        ClusterServersConfig clusterServersConfig =
            config.useClusterServers().setScanInterval(2000).addNodeAddress(redisAddrs);

        clusterServersConfig.setMasterConnectionPoolSize(200);//设置对于master节点的连接池中连接数最大为500
        clusterServersConfig.setSlaveConnectionPoolSize(200);//设置对于slave节点的连接池中连接数最大为500
        clusterServersConfig.setIdleConnectionTimeout(
            3000);//如果当前连接池里的连接数量超过了最小空闲连接数，而同时有连接空闲时间超过了该数值，那么这些连接将会自动被关闭，并从连接池里去掉。时间单位是毫秒。
        clusterServersConfig.setConnectTimeout(30000);//同任何节点建立连接时的等待超时。时间单位是毫秒。
        clusterServersConfig.setTimeout(3000);//等待节点回复命令的时间。该时间从命令发送成功时开始计时。

        try {
            redissonClient = Redisson.create(config);
            return redissonClient;
        } catch (Exception e) {
            log.error("RedissonClient初始化异常", e);
            return null;
        }
    }
}
