package ru.veretennikov.foolwebsocket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.veretennikov.foolwebsocket.common.config.AppProperty;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class GreetingServiceImpl implements GreetingService {

    private final AppProperty appProperty;

    @Override
    public String getGreeting(String sender) {

        String greeting = "К нам присоединяется %s. Поприветствуем!";
        Random random = new Random();

        List<String> greetings = appProperty.getGreetings();

        if (!greetings.isEmpty())
            greeting = greetings.get(random.nextInt(greetings.size()));

        return String.format(greeting, sender);

    }

}
