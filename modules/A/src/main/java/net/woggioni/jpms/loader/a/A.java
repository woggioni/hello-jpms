package net.woggioni.jpms.loader.a;


import com.fasterxml.jackson.databind.ObjectMapper;
import net.woggioni.jpms.loader.b.B;

public class A extends B {
    static {
        ClassLoadingUtils.debugClass(ObjectMapper.class.getName(), A.class);
    }

    public static int distance(P2d p1, P2d p2) {
        return (int) Math.sqrt(B.square(p1.getX() - p2.getX()) + B.square(p1.getY() - p2.getY()));
    }
}
