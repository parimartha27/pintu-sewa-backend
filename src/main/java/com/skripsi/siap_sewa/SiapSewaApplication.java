package com.skripsi.siap_sewa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class SiapSewaApplication {

	public static void main(String[] args) {
		SpringApplication.run(SiapSewaApplication.class, args);
	}

}
