package org.cordnerds.annotation.model;

/**
 * @author Amila Karunathilaka
 */
public class PropertyField {
    private String name;
    private String propertyName;
    private ClassAwareBean typeAwareBean;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public ClassAwareBean getTypeAwareBean() {
        return typeAwareBean;
    }

    public void setTypeAwareBean(ClassAwareBean typeAwareBean) {
        this.typeAwareBean = typeAwareBean;
    }
}
