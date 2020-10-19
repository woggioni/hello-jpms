package net.woggioni.jpms.loader.zloader;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

public class CollectionUtils {

    @SafeVarargs
    public static <T> ArrayList<T> newArrayList(T... args) {
        return new ArrayList<>(Arrays.asList(args));
    }

    public static <T> Collector<T, ?, List<T>> toUnmodifiableList() {
        return Collector.of(
                ArrayList::new,
                List::add,
                (ArrayList<T> l1, ArrayList<T> l2) -> {
                    l1.addAll(l2);
                    return l1;
                },
                Collections::unmodifiableList);
    }

    @SafeVarargs
    public static <T> List<T> immutableList(T... elements) {
        return Stream.of(elements).collect(toUnmodifiableList());
    }

    public static <T> Collector<T, ?, Set<T>> toUnmodifiableSet() {
        return Collector.of(
                HashSet::new,
                Set::add,
                (Set<T> s1, Set<T> s2) -> {
                    s1.addAll(s2);
                    return s1;
                },
                Collections::unmodifiableSet);
    }

    @SafeVarargs
    public static <T> Set<T> immutableSet(T... elements) {
        return Stream.of(elements).collect(toUnmodifiableSet());
    }

    private static <T> Collector<T, ?, Set<T>> createTreeSetCollector(Function<Set<T>, Set<T>> finalizer, Comparator<? super T> comparator) {
        return Collector.of(
                () -> new TreeSet<>(comparator),
                Set::add,
                (Set<T> s1, Set<T> s2) -> {
                    s1.addAll(s2);
                    return s1;
                },
                finalizer);
    }

    private static <T extends Comparable<T>> Collector<T, ?, Set<T>>
    createTreeSetCollector(Function<Set<T>, Set<T>> finalizer) {
        return Collector.of(
                TreeSet::new,
                Set::add,
                (Set<T> s1, Set<T> s2) -> {
                    s1.addAll(s2);
                    return s1;
                },
                finalizer);
    }

    public static <T extends Comparable<T>> Collector<T, ?, Set<T>> toTreeSet() {
        return createTreeSetCollector(Function.identity());
    }

    public static <T> Collector<T, ?, Set<T>> toTreeSet(Comparator<? super T> comparator) {
        return createTreeSetCollector(Function.identity(), comparator);
    }

    public static <T> Collector<T, ?, Set<T>> toUnmodifiableTreeSet(Comparator<? super T> comparator) {
        return createTreeSetCollector(Collections::unmodifiableSet, comparator);
    }

    public static <T extends Comparable<T>> Collector<T, ?, Set<T>> toUnmodifiableTreeSet() {
        return createTreeSetCollector(Collections::unmodifiableSet);
    }

    @SafeVarargs
    public static <T> Set<T> immutableTreeSet(Comparator<? super T> comparator, T... elements) {
        return Stream.of(elements).collect(toUnmodifiableTreeSet(comparator));
    }

    @SafeVarargs
    public static <T extends Comparable<T>> Set<T> immutableTreeSet(T... elements) {
        return Stream.of(elements).collect(toUnmodifiableTreeSet());
    }

    private static <K, V, M extends Map<K, V>> BinaryOperator<M> mapMerger(BinaryOperator<V> var0) {
        return (m1, m2) -> {
            Iterator<Map.Entry<K, V>> it = m2.entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry<K, V> entry = it.next();
                m1.merge(entry.getKey(), entry.getValue(), var0);
            }

            return m1;
        };
    }

    private static <T> BinaryOperator<T> throwingMerger() {
        return (v1, v2) -> {
            throw new IllegalStateException(String.format("Duplicate key %s", v1));
        };
    }

    public static <T, K, V> Collector<T, ?, Map<K, V>> toUnmodifiableHashMap(
            Function<T, K> keyExtractor,
            Function<T, V> valueExtractor) {
        return toUnmodifiableMap(HashMap::new, keyExtractor, valueExtractor);
    }

    public static <T, K, V> Collector<T, ?, Map<K, V>> toUnmodifiableTreeMap(
            Function<T, K> keyExtractor,
            Function<T, V> valueExtractor) {
        return toUnmodifiableMap(TreeMap::new, keyExtractor, valueExtractor);
    }

    public static <T, K, V> Collector<T, ?, Map<K, V>> toUnmodifiableMap(
            Supplier<Map<K, V>> constructor,
            Function<T, K> keyExtractor,
            Function<T, V> valueExtractor) {
        BiConsumer<Map<K, V>, T> accumulator = (map, streamElement) -> {
            map.merge(keyExtractor.apply(streamElement), valueExtractor.apply(streamElement), throwingMerger());
        };
        return Collector.of(
                constructor,
                accumulator,
                mapMerger(throwingMerger()),
                Collections::unmodifiableMap
        );
    }
}