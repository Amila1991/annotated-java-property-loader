package org.cordnerds.scanner;

import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Amila Karunathilaka
 */
public class SourceScanner {

    private Reflections reflections;

    private SourceScanner(ConfigurationBuilder configurationBuilder) {
        reflections = new Reflections(configurationBuilder);
    }

    public static SourceScanner getInstanceByPackage(Package root) {
        ConfigurationBuilder cb = new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(root.getName(), root.getClass().getClassLoader()))
                .setScanners(new SubTypesScanner(),
                        new TypeAnnotationsScanner(),
                        new ResourcesScanner());


        return new SourceScanner(cb);
    }

    public static SourceScanner getInstanceByClassLoader(ClassLoader classLoader) {
        ConfigurationBuilder cb = new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forClassLoader(classLoader))
                .setScanners(new SubTypesScanner(),
                new TypeAnnotationsScanner(),
                new ResourcesScanner());

        return new SourceScanner(cb);
    }

    public List<Class<?>> getTypesAnnotatedWith(Class<? extends Annotation> annotation) {
        return toList(reflections.getTypesAnnotatedWith(annotation));
    }

    public List<String> getResourcesByRegex(String regex) {
        return toList(reflections.getResources(Pattern.compile(regex)));
    }

    public List<String> getResourcesByName(String name) {
        return toList(reflections.getResources(input -> input != null && input.equals(name)));
    }

    private <T> List<T> toList(Set<T> valueSet) {
        return valueSet.stream().collect(Collectors.toList());
    }

}
