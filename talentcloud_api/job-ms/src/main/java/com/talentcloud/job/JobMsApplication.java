package com.talentcloud.job;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.kafka.annotation.EnableKafka;
@SpringBootApplication
//@EnableDiscoveryClient
@EnableFeignClients
@EnableKafka  // Add this annotation to enable Kafka
public class JobMsApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobMsApplication.class, args);
	}

}
