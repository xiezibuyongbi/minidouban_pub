package com.minidouban.configuration;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource ("classpath:application.yml")
public class ElasticsearchConfig {
    @Value ("${elasticsearch.host}")
    private String host;

    @Value ("${elasticsearch.port}")
    private int port;

    @Value ("${elasticsearch.scheme}")
    private String scheme;

    @Bean ("esClient")
    public RestHighLevelClient esClient() {
        return new RestHighLevelClient(RestClient.builder(new HttpHost(host, port, scheme)));
    }
}
