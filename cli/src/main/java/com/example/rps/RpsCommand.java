package com.example.rps;

import com.example.rps.RpsClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

/**
 * @author Beka Tsotsoria
 */
@Component
public class RpsCommand implements CommandMarker {

    @Autowired
    private RpsClient client;

    @CliAvailabilityIndicator("rps")
    public boolean isSimpleAvailable() {
        //always available
        return true;
    }

    @CliCommand("rps")
    public String connect(@CliOption(key = "host", mandatory = true) String host,
                                @CliOption(key = "port", mandatory = true) int port) {
        try {
            client.connect(host, port);
            return "Connected to RPS Server";
        } catch (Exception e) {
            return "Failed to connect to RPS server: " + e.getMessage();
        }

    }
}
