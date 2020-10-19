package net.woggioni.jpms.loader.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import net.woggioni.jpms.loader.serialization.Serializer;

import java.net.URL;

public class JsonSerializer implements Serializer {
    private ObjectMapper om = new ObjectMapper();

    public JsonSerializer() {
        Module m = getClass().getModule();
        URL url = getClass().getClassLoader().getResource(ObjectMapper.class.getName().replace(".", "/") + ".class");
        System.out.printf("Module '%s' sees ObjectMapper from '%s'\n", m.getName(),  url);
    }

    @Override
    @SneakyThrows
    public String serialize(Object o) {
        return om.writeValueAsString(o);
    }
}
