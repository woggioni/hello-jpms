package net.woggioni.jpms.loader.zloader;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.net.URL;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CpkURL {
    public final String cpkFilePath;
    public final String jarFilePath;
    public final String resourceName;

    public static CpkURL from(URL url) {
        String urlPath = url.getPath();
        int cursor = urlPath.indexOf('!');
        if (cursor < 0) throw new IllegalArgumentException(String.format("Invalid URL '%s'", url.toString()));
        int firstSeparator = cursor;
        cursor = urlPath.indexOf('!', cursor + 1);
        if (cursor < 0) throw new IllegalArgumentException(String.format("Invalid URL '%s'", url.toString()));
        int secondSeparator = cursor;
        String cpkFilePath = urlPath.substring(0, firstSeparator);
        String jarFilePath = urlPath.substring(firstSeparator + 1, secondSeparator);
        String resourceName = urlPath.substring(secondSeparator + 1);
        return new CpkURL(cpkFilePath, jarFilePath, resourceName);
    }
}
