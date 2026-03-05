package com.beyond.ordersystem.common.configs;

import com.beyond.ordersystem.common.service.SseAlarmService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private int port;
//    연결객체
    @Bean
//    @Qunlifier : 같은 Bean객체가 여러개 있을 경우 Bean객체를 구분하기 위한 어노테이션
    @Qualifier("rtInventory")
    public RedisConnectionFactory redisConnectionFactory(){
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(0);
        return new LettuceConnectionFactory(configuration);
    }
//    템플릿빈객체
    @Bean
    @Qualifier("rtInventory")
//    모든 template중에 무조건 redisTemplate라는 메서드명이 반드시 1개는 잇어야함
//    bean객체 생성시 bean객체간에 DI는 메서드 파라미터 주입이 가능
    public RedisTemplate<String, String> redisTemplate(@Qualifier("rtInventory") RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String,String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }
    @Bean
//    @Qualifier : 같은 Bean객체가 여러개 있을 경우 Bean객체를 구분하기 위한 어노테이션
    @Qualifier("stockInventory")
    public RedisConnectionFactory stockConnectionFactory(){
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(1);
        return new LettuceConnectionFactory(configuration);
    }
    //    템플릿빈객체
    @Bean
//                        KEY  :  VALUE
    @Qualifier("stockInventory")
    public RedisTemplate<String, String> stockredisTemplate(@Qualifier("stockInventory") RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String,String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    @Bean
    @Qualifier("ssePubSub")
    public RedisConnectionFactory ssePubSubConnectionFactory(){
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
//        pub/sub 기능은 db에 값을 저장하는 기능이 아니므로, 특정 db에 의존적이지 않음.
        return new LettuceConnectionFactory(configuration);
    }
    @Bean
    @Qualifier("ssePubSub")
    public RedisTemplate<String, String> ssePubSubredisTemplate(@Qualifier("ssePubSub") RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String,String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

//    redis 리스너(subscribe)객체
//    호출구조 : RedisMessageListenerContainer => MessageListenerAdapter => SseAlarmService(MessageListner)
    @Bean
    @Qualifier("ssePubSub")
    public RedisMessageListenerContainer redisMessageListenerContainer(@Qualifier("ssePubSub") RedisConnectionFactory redisConnectionFactory,
                                                                       @Qualifier("ssePubSub") MessageListenerAdapter messageListenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(messageListenerAdapter, new PatternTopic("order-channel"));
//        만약에 여러 채널을 구독해야 하는경우, 여러개의 pattern topic을 add하거나 별도의 Listner Bean객체 생성
        return container;
    }

//    redis에서 수신된 메시지를 처리하는 객체
    @Bean
    @Qualifier("ssePubSub")
    public MessageListenerAdapter messageListenerAdapter(SseAlarmService sseAlarmService){
//        채널로부터 수신되는 message처리를 SseAlarmService의 onMessage메서드로위임
        return new MessageListenerAdapter(sseAlarmService,"onMessage");
    }

}
