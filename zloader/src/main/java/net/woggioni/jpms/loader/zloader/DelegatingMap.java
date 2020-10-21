package net.woggioni.jpms.loader.zloader;

import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class DelegatingMap<K, V> implements Map<K, V> {
    private final Map<K, V> thisMap;
    private final Iterable<Map<K, V>> delegates;

    @Override
    public V get(Object key) {
        Map<K, V> containingMap = null;
        if(thisMap.containsKey(key))  {
            containingMap = thisMap;
        } else {
            for(Map<K, V> delegate : delegates) {
                if(delegate.containsKey(key)) {
                    containingMap = delegate;
                    break;
                }
            }
        }
        return containingMap == null ? null : containingMap.get(key);
    }

    @Override
    public V put(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsKey(Object key) {
        if (thisMap.containsKey(key)) {
            return true;
        } else {
            for(Map<K, V> delegate : delegates) {
                if(delegate.containsKey(key)) return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<K> keySet() {
        Stream<K> s = thisMap.keySet().stream();
        for(Map<K, V> delegate : delegates) {
            s = Stream.concat(s, delegate.keySet().stream());
        }
        return s.collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Collection<V> values() {
        return keySet().stream().map(this::get).collect(Collectors.toList());
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return keySet().stream()
            .map(k -> new AbstractMap.SimpleEntry<>(k, get(k)))
            .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public boolean equals(Object o) {
        if(o == null) {
            return false;
        } else if(o instanceof Map) {
            return Objects.equals(entrySet(), ((Map) o).entrySet());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int result = thisMap.hashCode();
        for(Map<K, V> delegate : delegates) {
            result ^= delegate.hashCode();
        }
        return result;
    }
}
