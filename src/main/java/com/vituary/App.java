package com.vituary;

import com.vituary.model.Item;
import com.vituary.repository.ItemRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;

@SpringBootApplication
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ItemRepository itemRepository) {
        // Pre-populate the database with some test listings
        return args -> {
            itemRepository.saveAll(Arrays.asList(
                    new Item("iPhone 7", "Old iPhone", 99, 1),
                    new Item("iPhone 11", "New iPhone", 1099, 300),
                    new Item("Pixel 2", "Google's Phone", 799, 3)
            ));
        };
    }
}
