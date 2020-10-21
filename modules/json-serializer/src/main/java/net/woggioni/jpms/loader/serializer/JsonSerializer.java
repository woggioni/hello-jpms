package net.woggioni.jpms.loader.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import net.woggioni.jpms.loader.serialization.Serializer;

import java.net.URL;

public class JsonSerializer implements Serializer {
    private ObjectMapper om = new ObjectMapper();

    @SneakyThrows
    public JsonSerializer() {
        String className = ObjectMapper.class.getName();
        Module m = JsonSerializer.class.getModule();
        Class<?> cls = Class.forName(className, false, JsonSerializer.class.getClassLoader());
        ClassLoader loader = cls.getClassLoader();
        URL url = loader.getResource(className.replace('.', '/') + ".class");
        if(url == null) {
            System.out.printf("Class '%s' of module '%s' cannot see class '%s'\n", JsonSerializer.class.getName(), m.getName(), className);
        } else {
            System.out.printf("Class '%s' of module '%s' sees '%s' from '%s'\n", JsonSerializer.class.getName(), m.getName(),  className, url);
        }
    }

    @Override
    @SneakyThrows
    public String serialize(Object o) {
        return om.writeValueAsString(o);
    }
}
