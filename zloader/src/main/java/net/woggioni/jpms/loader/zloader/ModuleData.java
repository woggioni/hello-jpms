package net.woggioni.jpms.loader.zloader;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ModuleData {
    final String name;
    final ClassLoader classLoader;
}
