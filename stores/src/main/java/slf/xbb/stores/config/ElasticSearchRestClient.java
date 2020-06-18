package slf.xbb.stores.config;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpPost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ：xbb
 * @date ：Created in 2020/6/18 4:57 下午
 * @description：ES客户端配置
 * @modifiedBy：
 * @version:
 */

@Configuration
public class ElasticSearchRestClient {
    @Value("${elasticsearch.ip1}")
    String ipAddr_1;

    @Bean(name = "highLevelClient")
    public RestHighLevelClient highLevelClient() {
        String[] addrs = ipAddr_1.split(":");
        String ip = addrs[0];
        int port = Integer.valueOf(addrs[1]);
        HttpHost httpHost = new HttpHost(ip, port, "http");
        return new RestHighLevelClient(RestClient.builder(httpHost));
    }
}
