package org.cordnerds.mapper.factory;

import org.cordnerds.mapper.ClassPropertyMapper;
import org.cordnerds.mapper.PropertyMapper;

/**
 * @author Amila Karunathilaka
 */
public class PropertyMapperFactory {

    public static PropertyMapper getPropertyMapper(MapperType mapperType) {
        switch (mapperType) {
            case CLASSMAPPER:
                return new ClassPropertyMapper();
            default:
                return null;
        }

    }

    public enum MapperType {
        CLASSMAPPER;
    }
}
