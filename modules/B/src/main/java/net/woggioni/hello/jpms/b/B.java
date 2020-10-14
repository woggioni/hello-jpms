package net.woggioni.hello.jpms.b;

import net.woggioni.hello.jpms.b.impl.BImpl;

public class B {
    public static int square(int n) {
        return BImpl.square(n);
    }
}
