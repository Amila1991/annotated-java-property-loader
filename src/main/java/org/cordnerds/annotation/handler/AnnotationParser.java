package org.cordnerds.annotation.handler;

import org.cordnerds.annotation.model.*;

import java.util.*;

/**
 * @author Amila Karunathilaka
 */
public abstract class AnnotationParser<T extends ClassAwareBean, S> {

    public abstract List<T> parseAnnotations(List<S> listofS);

    public abstract T parseAnnotation(S s);

    public List<T> parseAnnotations(String packageName) {
        return parseAnnotations(Package.getPackage(packageName));
    }

    public abstract List<T> parseAnnotations(Package pckge);

    public abstract Class getAnnotation();




}
