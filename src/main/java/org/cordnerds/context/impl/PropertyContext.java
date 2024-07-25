package org.cordnerds.context.impl;

import org.cordnerds.annotation.PropertySource;
import org.cordnerds.annotation.handler.AnnotationHandler;
import org.cordnerds.annotation.model.PropertyClassAwareBean;
import org.cordnerds.context.Context;
import org.cordnerds.io.PropertySourceReader;
import org.cordnerds.io.factory.PropertySourceReaderFactory;
import org.cordnerds.mapper.PropertyMapper;
import org.cordnerds.mapper.factory.PropertyMapperFactory;
import org.cordnerds.scanner.SourceScanner;

import java.util.*;
import java.util.function.Function;

/**
 * @author Amila Karunathilaka
 */
public final class PropertyContext implements Context {

    private final static String DEFAULT_LOCATION = "classpath:";
    private final static String DEFAULT_FILE = "application.properties";

    private final static Function<Optional<String>, String> LOCATION_FUNC =
            (location) -> location.orElse(DEFAULT_LOCATION);
    private final static Function<Optional<String>, String> FILE_FUNC = (file) -> file.orElse(DEFAULT_FILE);

    private static Context INSTANCE;
    private final Map<Class<?>, Object> loadedPropertyObjectMap;

    private PropertyContext() {
        loadedPropertyObjectMap = new HashMap<>();
    }


    public static Context getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PropertyContext();
        }

        return INSTANCE;
    }

    @Override
    public void load(ClassLoader classLoader) {
        SourceScanner scanner = SourceScanner.getInstanceByClassLoader(classLoader);
        Class<PropertySource> propertySourceClass = PropertySource.class;
        List<Class<?>> annotatedClasses = scanner.getTypesAnnotatedWith(propertySourceClass);

        annotatedClasses.forEach(this::load);
    }

    @Override
    public <R> Optional<R> get(Class<R> propClass) {
        R propObj = (R)loadedPropertyObjectMap.get(propClass);
        if (propObj == null) {
            return this.load(propClass);
        }
        return Optional.of(propObj);
    }

    private <R> Optional<R> load(Class<R> aClass) {
        PropertyClassAwareBean propertyClass = AnnotationHandler.getInstance().extractPropertySource(aClass);
        PropertySourceReader propertySourceReaderLoader = PropertySourceReaderFactory.getPropertySourceReader();
        Map<String, String> propertyMap = propertySourceReaderLoader.getPropertiesAsMap(
                FILE_FUNC.apply(Optional.ofNullable(propertyClass.getSource())),
                LOCATION_FUNC.apply(Optional.ofNullable(propertyClass.getLocation())));
        PropertyMapper<Map<String, String>> propertyMapper =
                PropertyMapperFactory.getPropertyMapper(PropertyMapperFactory.MapperType.CLASSMAPPER);
        assert propertyMapper != null;
        Optional<R> propObjOpt = propertyMapper.mapProperties(propertyMap, propertyClass);
        propObjOpt.ifPresent(r -> loadedPropertyObjectMap.put(aClass, r));

        return propObjOpt;
    }


    private void loadSource() {

    }
}
