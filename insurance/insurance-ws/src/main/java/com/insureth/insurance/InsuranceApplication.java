package com.insureth.insurance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication()
@EnableFeignClients
public class InsuranceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InsuranceApplication.class, args);
    }

}
