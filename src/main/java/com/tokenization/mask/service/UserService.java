package com.tokenization.mask.service;

import com.tokenization.mask.dto.UserPayload;
import com.tokenization.mask.exception.UserNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {

    private final Map<Long, UserPayload> store = new ConcurrentHashMap<>();

    public UserPayload save(UserPayload payload) {
        store.put(payload.userId(), payload);
        return payload;
    }

    public UserPayload getById(Long id) {
        UserPayload payload = store.get(id);
        if (payload == null) {
            throw new UserNotFoundException(id);
        }
        return payload;
    }
}
