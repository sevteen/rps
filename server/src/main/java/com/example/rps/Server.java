package com.example.rps;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author Beka Tsotsoria
 */
@SpringBootApplication
public class Server {

    public static void main(String[] args) {
        start(args);
    }

    public static ConfigurableApplicationContext start(String[] args) {
        return SpringApplication.run(Server.class, args);
    }

}
