package net.corda.jpms.loader;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.SneakyThrows;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Utils {
    public static final <T> Stream<T> iterable2Stream(Iterable<T> iterable) {
        return iterable2Stream(iterable, false);
    }

    public static final <T> Stream<T> iterable2Stream(Iterable<T> iterable, boolean parallel) {
        return StreamSupport.stream(iterable.spliterator(), parallel);
    }

    public static final <T> Stream<T> iterator2Stream(Iterator<T> it, boolean parallel) {
        return iterable2Stream(() -> it, parallel);
    }
    public static final <T> Stream<T> iterator2Stream(Iterator<T> it) {
        return iterator2Stream(it, false);
    }

    public static Optional<JsonNode> jsonGet(JsonNode root, String... keys) {
        JsonNode result = root;
        for (String key : keys) {
            result = result.get(key);
            if (result == null) {
                break;
            }
        }
        return Optional.ofNullable(result);
    }

    @SneakyThrows
    public static <T extends Throwable> T newThrowable(Class<T> cls, String format, Object... args) {
        Constructor<T> constructor = cls.getDeclaredConstructor(String.class);
        constructor.setAccessible(true);
        return constructor.newInstance(String.format(format, args));
    }

    @SneakyThrows
    public static <T extends Throwable> T newThrowable(Class<T> cls, Throwable throwable, String format, Object... args) {
        Constructor<T> constructor = cls.getConstructor(String.class, Throwable.class);
        return constructor.newInstance(String.format(format, args), throwable);
    }

    public static <T> T tail(List<T> list, int negativeOffset) {
        return list.get(list.size() + negativeOffset);
    }

    public static <T> T tail(List<T> list) {
        return tail(list, -1);
    }


    public static <T> T pop(List<T> list) {
        return list.remove(list.size() - 1);
    }
}
