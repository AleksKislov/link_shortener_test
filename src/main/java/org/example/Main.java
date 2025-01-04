package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.application.LinkService;
import org.example.config.RedisConfig;
import org.example.config.SystemConfig;
import org.example.controller.LinkController;
import org.example.infra.LinkRepository;
import org.example.view.Display;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class Main {
    public static void main(String[] args) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        String path = Objects.requireNonNull(Main.class.getClassLoader().getResource("conf.json")).getPath();;
        SystemConfig config = mapper.readValue(new File(path), SystemConfig.class);

        LinkController linkControl = new LinkController(
                new LinkService(new LinkRepository(RedisConfig.getPool()), config)
        );

        SwingUtilities.invokeLater(() -> {
            Display frame = new Display(linkControl, config.getLinkBaseUrl());
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}