package org.cordnerds.io.factory;

import org.cordnerds.io.DefaultPropertySourceReader;
import org.cordnerds.io.PropertySourceReader;

/**
 * @author Amila Karunathilaka
 */
public class PropertySourceReaderFactory {


    public static PropertySourceReader getPropertySourceReader() {
        return new DefaultPropertySourceReader();
    }
}
