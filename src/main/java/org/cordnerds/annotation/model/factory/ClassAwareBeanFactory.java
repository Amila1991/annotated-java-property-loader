package org.cordnerds.annotation.model.factory;

import org.cordnerds.annotation.model.*;

/**
 * @author Amila Karunathilaka
 */
public class ClassAwareBeanFactory {

    public static <R> ClassAwareBean getClassAwareBean(Class<R> clazz) {
        ClassType classType = ClassType.findPropertyFieldType(clazz);
        switch (classType) {
            case OBJECT:
            case STRING:
            case INT:
            case INTEGER_WRAPPER:
            case LONG:
            case LONG_WRAPPER:
            case DOUBLE:
            case DOUBLE_WRAPPER:
            case FLOAT:
            case FLOAT_WRAPPER:
            case BOOLEAN:
            case BOOLEAN_WRAPPER:
                return new ClassAwareBean(clazz, classType);
            case ARRAY:
                return new ArrayClassAwareBean(clazz, classType);
            case LIST:
            case SET:
            case MAP:
                return new CollectionClassAwareBean(clazz ,classType);
            case CUSTOM:
                return new PropertyClassAwareBean(clazz, classType);
            default:
                return null;
        }
    }
}
