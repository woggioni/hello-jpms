package net.corda.jpms.loader;

import lombok.RequiredArgsConstructor;

import java.lang.module.Configuration;

@RequiredArgsConstructor
public class Bundle {
    private final Configuration configuration;
    private final Module mainModule;
    private final Class<?> mainClass;
}
