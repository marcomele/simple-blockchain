package me.blockchain.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

/**
 * Created by marcomele on 5/10/20
 */

public abstract class JsonUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    @SneakyThrows
    public static final <T> String serialize(T t) {
        return mapper.writeValueAsString(t);
    }

}
