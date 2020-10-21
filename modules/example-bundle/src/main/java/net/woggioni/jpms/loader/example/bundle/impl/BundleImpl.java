package net.woggioni.jpms.loader.example.bundle.impl;

import net.woggioni.jpms.loader.a.ClassLoadingUtils;
import net.woggioni.jpms.loader.a.P2d;

public class BundleImpl {
    public static void run() {
        ClassLoadingUtils.debugClass(P2d.class.getName(), BundleImpl.class);
    }
}
