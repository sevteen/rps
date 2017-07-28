package com.example.rps;

import com.example.rps.DefaultRpsClient;
import com.example.rps.RpsClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Beka Tsotsoria
 */
@Configuration
public class Config {

    @Bean
    public RpsClient client() {
        return new DefaultRpsClient();
    }
}
