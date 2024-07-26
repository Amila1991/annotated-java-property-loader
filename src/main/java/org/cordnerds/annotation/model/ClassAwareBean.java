package org.cordnerds.annotation.model;

/**
 * @author Amila Karunathilaka
 */
public class ClassAwareBean implements Cloneable {
    private Class<?> type;
    private ClassType classType;
    private ClassAwareBean parent;

    public ClassAwareBean(Class<?> type, ClassType classType) {
        this.type = type;
        this.classType = classType;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public ClassType getClassType() {
        return classType;
    }

    public void setClassType(ClassType classType) {
        this.classType = classType;
    }

    public ClassAwareBean getParent() {
        return parent;
    }

    public void setParent(ClassAwareBean parent) {
        this.parent = parent;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
