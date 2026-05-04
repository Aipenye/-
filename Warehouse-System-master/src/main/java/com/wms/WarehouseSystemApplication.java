package com.wms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.wms", "com.xlrit.boxoptimization"})
public class WarehouseSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(WarehouseSystemApplication.class, args);
	}

}
