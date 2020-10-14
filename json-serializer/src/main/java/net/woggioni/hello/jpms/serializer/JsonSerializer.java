package net.woggioni.hello.jpms.serializer;

import lombok.SneakyThrows;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.woggioni.hello.jpms.serialization.Serializer;

public class JsonSerializer implements Serializer {
    private ObjectMapper om = new ObjectMapper();

    @Override
    @SneakyThrows
    public String serialize(Object o) {
        return om.writeValueAsString(o);
    }
}
