package com.minidouban.cachemgr.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.minidouban.cachemgr.pojo.MQDelCacheMsg;
import com.minidouban.cachemgr.producer.CacheDelMsgProducer;
import com.minidouban.cachemgr.util.CacheKeyGenerator;
import com.minidouban.cachemgr.util.JedisUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class CacheDelMsgConsumer {
    private static final String TOPIC = "cache_delete";
    private static final String GROUP_ID = "cache-delete-msg-consumer";
    @Resource
    private JedisUtils jedisUtils;
    @Resource
    private CacheDelMsgProducer cacheDelMsgProducer;
    @Resource
    private CacheKeyGenerator cacheKeyGenerator;

    @KafkaListener (topics = { TOPIC }, groupId = GROUP_ID)
    public void consume(ConsumerRecord<String, String> consumerRecord, Acknowledgment acknowledgment) {
        MQDelCacheMsg mqDelCacheMsg;
        try {
            mqDelCacheMsg = JSON.parseObject(consumerRecord.value(), MQDelCacheMsg.class);
        } catch (JSONException e) {
            e.printStackTrace();
            acknowledgment.acknowledge();
            return;
        }
        if (mqDelCacheMsg.getOperation().equals(MQDelCacheMsg.CacheOperation.DELETE)) {
            String key = cacheKeyGenerator.getRedisKey(mqDelCacheMsg.getBusinessName().name(),
                                                       mqDelCacheMsg.getEntryId());
            switch (mqDelCacheMsg.getBusinessName()) {
                case READING_LIST:
                case READING_LIST_BOOK:
                case USER:
                    if (jedisUtils.delIfExisting(key) == 0) {
                        return;
                    }
                    break;
                default: // not defined
                    break;
            }
        } else { // not defined
        }
        acknowledgment.acknowledge();
    }
}
