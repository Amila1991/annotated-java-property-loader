package org.cordnerds.annotation.model;

import java.util.List;

/**
 * @author Amila Karunathilaka
 */
public class PropertyClassAwareBean extends GenericClassAwareBean {
    private String prefix;
    private String location;
    private String source;
    private List<PropertyField> propertyFields;

    public PropertyClassAwareBean(Class<?> type) {
        super(type, ClassType.findPropertyFieldType(type));
    }

    public PropertyClassAwareBean(Class<?> type, ClassType classType) {
        super(type, classType);
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public List<PropertyField> getPropertyFields() {
        return propertyFields;
    }

    public void setPropertyFields(List<PropertyField> propertyFields) {
        this.propertyFields = propertyFields;
    }
}
