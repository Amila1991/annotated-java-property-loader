package org.cordnerds.annotation.model;

import java.util.Map;

/**
 * @author Amila Karunathilaka
 */
public abstract class GenericClassAwareBean extends ClassAwareBean {
    private Map<String, ClassAwareBean> genericClasses;

    public GenericClassAwareBean(Class<?> type, ClassType classType) {
        super(type, classType);
    }

    public Map<String, ClassAwareBean> getGenericClasses() {
        return genericClasses;
    }

    public void setGenericClasses(Map<String, ClassAwareBean> genericClasses) {
        this.genericClasses = genericClasses;
    }

}
