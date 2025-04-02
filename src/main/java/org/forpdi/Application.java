package org.forpdi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"org.forpdi.*", "org.forrisco.*"}, exclude = {JacksonAutoConfiguration.class})
@EntityScan(basePackages = {"org.forpdi.*", "org.forrisco.*"})
@EnableJpaRepositories(basePackages = {"org.forpdi.*", "org.forrisco.*"})
@ComponentScan(basePackages = {"org.forpdi.*", "org.forrisco.*"})
@EnableScheduling
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
