package net.woggioni.jpms.loader.a;

import net.woggioni.jpms.loader.serialization.Serializer;

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
        serializer = ServiceLoader.load(P2d.class.getModule().getLayer(), Serializer.class).findFirst().orElseThrow(() ->
            new RuntimeException(String.format("Unable to load service '%s'", Serializer.class.getName()))
        );
    }

    @Override
    public String toString() {
        return serializer.serialize(this);
    }
}
