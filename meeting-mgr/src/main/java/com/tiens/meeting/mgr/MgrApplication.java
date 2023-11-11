package com.tiens.meeting.mgr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(value = {
		"com.tiens.meeting.mgr",
})
public class MgrApplication {
	public static void main(String[] args) {
		SpringApplication.run(MgrApplication.class, args);
	}
}
