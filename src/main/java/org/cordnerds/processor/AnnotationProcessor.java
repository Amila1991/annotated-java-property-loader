package org.cordnerds.processor;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Amila Karunathilaka
 */
public abstract class AnnotationProcessor<T, I> {

    public abstract T process(I i);

    public List<T> processAsCollection(List<I> iList) {
        return iList.stream().map(i -> process(i)).collect(Collectors.toList());
    }

    protected Optional<String> extractAnnotationMethodAsString(String name, Annotation annotation) {
        return extractAnnotationMethod(name, annotation, String.class);
    }

    protected <R> Optional<R> extractAnnotationMethod(String name, Annotation annotation, Class<R> type)  {
        try {
            return Optional.ofNullable(type.cast(annotation.annotationType().getDeclaredMethod(name).invoke(annotation)));
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

}
