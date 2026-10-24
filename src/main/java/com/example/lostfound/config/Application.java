package com.example.lostfound.config;

import com.example.lostfound.domain.CompositeLostItemRule;
import com.example.lostfound.domain.LostItemRule;
import com.example.lostfound.domain.LostItemStatus;
import com.example.lostfound.domain.ReturnedItemIsFinalRule;
import com.example.lostfound.domain.TransitionRule;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication(scanBasePackages = "com.example.lostfound")
@EntityScan("com.example.lostfound.persistence")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    LostItemRule lostItemRules() {
        return new CompositeLostItemRule(List.of(
                new ReturnedItemIsFinalRule(),
                new TransitionRule()
        ));
    }

    @Bean
    CommandLineRunner demonstrate(LostItemService service) {
        return args -> {
            LostItemStatus status = service.move(LostItemStatus.REPORTED, LostItemStatus.FOUND);
            System.out.println("Campus Lost & Found started: REPORTED -> " + status);
        };
    }
}
