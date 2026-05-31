package com.notification_hub.notif;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NotifApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotifApplication.class, args);
	}

}
