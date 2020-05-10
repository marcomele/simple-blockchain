package me.blockchain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class BlockchainApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlockchainApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        final ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        return new RestTemplate(requestFactory);
    }

}
