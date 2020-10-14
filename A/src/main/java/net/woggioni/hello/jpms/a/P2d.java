package net.woggioni.hello.jpms.a;

import net.woggioni.hello.jpms.serialization.Serializer;

import java.util.ServiceLoader;

public class P2d {

    private final int x, y;

    public P2d(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    private static Serializer serializer;

    static {
        serializer = ServiceLoader.load(Serializer.class).findFirst().orElse(null);
    }

    @Override
    public String toString() {
        return serializer.serialize(this);
    }
}
