package org.cordnerds.io;

import org.cordnerds.io.model.Source;

import java.util.Map;

/**
 * @author Amila Karunathilaka
 */
public interface PropertySourceReader<T> {

    T load (String source, String path);

    Source<T> getSource(String source, String path);

    Map<String, String> getPropertiesAsMap(String source, String path);

}
