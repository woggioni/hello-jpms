package net.corda.jpms.loader;

import java.util.Iterator;
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
}
