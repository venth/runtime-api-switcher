package org.venth.poc.runtimeapiswitcher.osgi;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.Callable;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Venth on 10/05/2015
 *
 * This is simple modification which handles pattern:
 * classpath*:** slash *.class
 * For full pattern handling this class must be improved
 * Note that bundled jars are not scanned
 *
 */
@SuppressWarnings("PackageAccessibility")
public class OsgiBundleResourceResolver extends PathMatchingResourcePatternResolver {
    @Override
    protected Resource[] findPathMatchingResources(String locationPattern) throws IOException {
        String rootDirPath = determineRootDir(locationPattern);
        String subPattern = locationPattern.substring(rootDirPath.length());
        Resource[] rootDirResources = getResources(rootDirPath);

        Bundle currentBundle = FrameworkUtil.getBundle(getClass());

        return Arrays.stream(rootDirResources).flatMap(res -> {
            boolean handledByOsgi = execute(() -> res.getURL().getProtocol().startsWith("bundle"));

            Stream<Resource> foundResources;
            if (handledByOsgi) {
                foundResources = stream(
                        currentBundle.findEntries("", "*.class", true)
                ).map(url -> new UrlResource(url));
            } else {
                foundResources = Arrays.stream(
                        execute(() -> super.findPathMatchingResources(locationPattern))
                );
            }

            return foundResources;
        }).toArray(Resource[]::new);
    }


    private static <R> R execute(Callable<R> checkedExceptionThrowingFunc) {
        try {
            return checkedExceptionThrowingFunc.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> Stream<T> stream(Enumeration<T> e) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                        new Iterator<T>() {
                            public T next() {
                                return e.nextElement();
                            }

                            public boolean hasNext() {
                                return e.hasMoreElements();
                            }
                        },
                        Spliterator.ORDERED), false);
    }
}
