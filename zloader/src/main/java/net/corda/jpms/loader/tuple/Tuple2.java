package net.corda.jpms.loader.tuple;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;

@EqualsAndHashCode
@RequiredArgsConstructor
public class Tuple2<T, U> {
    public final T _1;
    public final U _2;

    public static <X extends Comparable<X>, Y extends Comparable<Y>> Comparator<Tuple2<X, Y>> getComparator(Class<X> cls1, Class<Y> cls2) {
        return Comparator
                .comparing((Tuple2<X, Y> t) -> t._1)
                .thenComparing((Tuple2<X, Y> t) -> t._2);
    }

    public static <X extends Comparable<X>, Y extends Comparable<Y>> Comparator<Tuple2<X, Y>> getComparator(Tuple2<X, Y> tuple) {
        return Comparator
                .comparing((Tuple2<X, Y> t) -> t._1)
                .thenComparing((Tuple2<X, Y> t) -> t._2);
    }
}