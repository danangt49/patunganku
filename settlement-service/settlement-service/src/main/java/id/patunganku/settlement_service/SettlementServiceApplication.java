package id.patunganku.settlement_service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class SettlementServiceApplication {
	@Value("${app.version}")
	private String appVersion;

	@GetMapping("/")
	public String root() {
		return "Settlement Services : " + appVersion;
	}

	public static void main(String[] args) {
		SpringApplication.run(SettlementServiceApplication.class, args);
	}

}
