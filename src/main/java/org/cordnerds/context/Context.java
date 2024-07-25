package org.cordnerds.context;

import java.util.Optional;

/**
 * @author Amila Karunathilaka
 */
public interface Context {

    void load(ClassLoader classLoader);

    <R> Optional<R> get(Class<R> propClass);
}
