package net.woggioni.hello.jpms.a;

import net.woggioni.hello.jpms.b.B;

public class A extends B {
    public static int distance(P2d p1, P2d p2) {
        return (int) Math.sqrt(B.square(p1.getX() - p2.getX()) + B.square(p1.getY() - p2.getY()));
    }
}
