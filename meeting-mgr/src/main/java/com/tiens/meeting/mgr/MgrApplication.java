package com.tiens.meeting.mgr;

import com.jtmm.third.party.listener.ApplicationReadyEventListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@SpringBootApplication(scanBasePackages = {"com.tiens","com.jtmm"})
@EnableFeignClients(value = {
		"com.tiens.meeting.mgr"
})

public class MgrApplication {
	public static void main(String[] args) {
		SpringApplication.run(MgrApplication.class, args);
	}
}
