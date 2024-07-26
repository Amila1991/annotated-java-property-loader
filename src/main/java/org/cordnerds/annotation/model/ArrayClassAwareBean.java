package org.cordnerds.annotation.model;

/**
 * @author Amila Karunathilaka
 */
public class ArrayClassAwareBean extends ClassAwareBean {

    private ClassAwareBean component;

    public ArrayClassAwareBean(Class<?> type, ClassType classType) {
        super(type, classType);
    }

    public ClassAwareBean getComponent() {
        return component;
    }

    public void setComponent(ClassAwareBean component) {
        this.component = component;
    }
}
