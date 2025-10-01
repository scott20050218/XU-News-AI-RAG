package cn.lihengrui.langchain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication  // Spring Boot主配置注解，包含@Configuration、@EnableAutoConfiguration、@ComponentScan
@EnableScheduling       // 启用Spring的定时任务功能，支持@Scheduled注解
public class LangchainApplication {
	public static void main(String[] args) {
		SpringApplication.run(LangchainApplication.class, args);
	}
}
