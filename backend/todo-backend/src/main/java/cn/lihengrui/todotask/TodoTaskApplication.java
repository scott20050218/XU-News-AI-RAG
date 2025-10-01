package cn.lihengrui.todotask;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * TodoTask应用程序主启动类
 * 
 * 这是一个智能知识内容采集和管理系统的Spring Boot应用程序。
 * 主要功能包括：
 * 1. 自动化RSS源数据采集 - 定时从配置的RSS源获取最新内容
 * 2. 网页内容爬取 - 定时爬取指定网站的内容信息
 * 3. 知识内容存储和管理 - 将采集的内容统一存储到数据库
 * 4. RESTful API接口 - 提供数据查询和管理接口
 * 5. 智能内容处理 - 预留接口支持AI智能代理对内容进行深度加工
 * 
 * 技术栈：
 * - Spring Boot 3.2.0 (Web框架)
 * - Spring Data JPA (数据访问层)
 * - MySQL 8.0 (数据存储)
 * - ROME (RSS解析)
 * - Jsoup (网页解析)
 * - SpringDoc OpenAPI (API文档)
 * - Spring Boot Actuator (监控)
 * 
 * @author HA72开发团队
 * @version 0.0.1-SNAPSHOT
 * @since 2025-09-26
 */
@SpringBootApplication  // Spring Boot主配置注解，包含@Configuration、@EnableAutoConfiguration、@ComponentScan
@EnableScheduling       // 启用Spring的定时任务功能，支持@Scheduled注解
public class TodoTaskApplication {

	/**
	 * 应用程序入口点
	 * 
	 * 启动Spring Boot应用容器，初始化以下组件：
	 * 1. Web服务器(Tomcat) - 监听8080端口
	 * 2. 数据库连接池 - 连接MySQL数据库
	 * 3. JPA实体管理器 - 管理数据库实体映射
	 * 4. 定时任务调度器 - 执行RSS采集和网页爬取任务
	 * 5. REST API控制器 - 提供HTTP接口服务
	 * 6. Swagger文档生成器 - 自动生成API文档
	 * 
	 * @param args 命令行参数，可以用于传递配置参数
	 */
	public static void main(String[] args) {
		SpringApplication.run(TodoTaskApplication.class, args);
	}

}
