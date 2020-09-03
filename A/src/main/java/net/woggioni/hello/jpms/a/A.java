package net.woggioni.hello.jpms.a;

import net.woggioni.hello.jpms.b.B;
import net.woggioni.hello.jpms.b.impl.BImpl;

public class A {
    public static int distance(P2d p1, P2d p2) {
        System.out.printf("BImpl call returned %d\n", BImpl.square(25));
        return (int) Math.sqrt(B.square(p1.getX() - p2.getX()) + B.square(p1.getY() - p2.getY()));
    }
}
