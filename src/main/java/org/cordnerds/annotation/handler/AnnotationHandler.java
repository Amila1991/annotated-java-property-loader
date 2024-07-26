package org.cordnerds.annotation.handler;

import org.cordnerds.annotation.model.*;
import org.cordnerds.annotation.model.factory.ClassAwareBeanFactory;
import org.cordnerds.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Amila Karunathilaka
 */
public class AnnotationHandler {

    private static AnnotationHandler INSTANCE;

/*    private Predicate<ClassAwareBean> customClassType = classAwareBean ->
            ClassType.CUSTOM.equals(classAwareBean.getClassType()) || classAwareBean instanceof PropertyClassAwareBean;*/

    private AnnotationHandler() {}

    public final static AnnotationHandler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AnnotationHandler();
        }

        return INSTANCE;
    }

    public List<PropertyClassAwareBean> extractPropertySources(List<Class<?>> annotatedClasses) {
        return annotatedClasses.stream().map(this::extractPropertySource).collect(Collectors.toList());
    }

    public PropertyClassAwareBean extractPropertySource(Class<?> annotatedClass) {
        Annotation propertySourceAnnotation = annotatedClass.getAnnotation(org.cordnerds.annotation.PropertySource.class);
        PropertyClassAwareBean propertyClass = new PropertyClassAwareBean(annotatedClass);
        propertyClass.setType(annotatedClass);
        if (propertySourceAnnotation != null) {
            extractAnnotationMethodAsString("source", propertySourceAnnotation).ifPresent(propertyClass::setSource);
            extractAnnotationMethodAsString("location", propertySourceAnnotation).ifPresent(propertyClass::setLocation);
            //extractAnnotationMethodAsString("prefix", propertySourceAnnotation).ifPresent(propertyClass::setPrefix);
        }
        setPropertyAwareInfo(propertyClass, extractAnnotationMethodAsString("prefix", propertySourceAnnotation), null);
        return propertyClass;
    }


    public List<PropertyField> extractPropertyFields(PropertyClassAwareBean parent, List<Field> fields, String prefix) {
        return fields.stream().map(field -> this.extractPropertyField(parent, field, prefix)).collect(Collectors.toList());
    }

    public PropertyField extractPropertyField(PropertyClassAwareBean parent, Field field, String prefix) {
        Annotation fieldAnnotation = field.getAnnotation(org.cordnerds.annotation.PropertyValue.class);
        String fieldPropName =  extractAnnotationMethodAsString("name", fieldAnnotation).orElse(field.getName());
        prefix = StringUtils.isNotBlank(prefix) ?  prefix.concat(".").concat(fieldPropName) : fieldPropName;

        /*Supplier<String> propertyNameSupplier = () -> {
            String fieldName =  extractAnnotationMethodAsString("name", fieldAnnotation).orElse(field.getName());
            return StringUtils.isNotBlank(prefix) ?
                    prefix.concat(".").concat(fieldName) : fieldName;
        };*/

        PropertyField propertyField = new PropertyField();
        ClassAwareBean classAwareBean = setGenericFieldType(parent, field);
        classAwareBean.setParent(parent);
        propertyField.setName(field.getName());
        propertyField.setPropertyName(prefix);
        propertyField.setTypeAwareBean(classAwareBean);
        Optional<String> prefixOpt = Optional.ofNullable(prefix);

        /*if (classAwareBean.getClassType().isCanBeGeneric() && ((GenericClassAwareBean) classAwareBean).getGenericClasses() == null) {
            generateGenericClassesMap(classAwareBean, field.getGenericType());
        }*/
        /*if (customClassType.test(classAwareBean)) {
            PropertyClassAwareBean propertyClassAwareBean = (PropertyClassAwareBean) classAwareBean;
            propertyClassAwareBean.setPrefix(propertyField.getPropertyName());
        }*/

        setPropertyAwareInfo(classAwareBean, prefixOpt, field.getGenericType());


        return propertyField;
    }

    private void setPropertyAwareInfo(ClassAwareBean classAwareBean, Optional<String> prefix, Type genericType) {
        generateGenericClassesMap(classAwareBean, genericType);
        if (ClassType.CUSTOM.equals(classAwareBean.getClassType()) || classAwareBean instanceof PropertyClassAwareBean) {
            PropertyClassAwareBean propertyClass = (PropertyClassAwareBean) classAwareBean;
            prefix.ifPresent(propertyClass::setPrefix);
            List<PropertyField> fieldList = new ArrayList<>();
            Class<?> propClass = propertyClass.getType();
            while (propClass != null && ClassType.CUSTOM.equals(ClassType.findPropertyFieldType(propClass))) {
              /*  if (parent != null && propClass == parent.getType()) {
                    throw new RuntimeException("Cyclic Property loading");
                }*/
                fieldList.addAll(extractPropertyFields(propertyClass, Arrays.asList(propClass.getDeclaredFields()), propertyClass.getPrefix()));
                propClass = propClass.getSuperclass();
            }
            propertyClass.setPropertyFields(fieldList);
        } else if(ClassType.ARRAY.equals(classAwareBean.getClassType()) || classAwareBean instanceof ArrayClassAwareBean) {
            ArrayClassAwareBean arrayClassAwareBean = (ArrayClassAwareBean) classAwareBean;
            ClassAwareBean componentAwareBean = ClassAwareBeanFactory.getClassAwareBean(classAwareBean.getType().getComponentType());
            setPropertyAwareInfo(componentAwareBean, prefix, genericType);
            arrayClassAwareBean.setComponent(componentAwareBean);
        } else if (classAwareBean.getClassType().isCanBeGeneric()) {
            GenericClassAwareBean genericClassAwareBean = (GenericClassAwareBean) classAwareBean;
            genericClassAwareBean.getGenericClasses().entrySet().stream().forEach(entry -> {
                setPropertyAwareInfo(entry.getValue(), prefix, null);
            });
        }
    }

    private ClassAwareBean setGenericFieldType(PropertyClassAwareBean parentPropAwareBean, Field field) {
            if (field.getGenericType() instanceof TypeVariable) {
                TypeVariable typeVariable = (TypeVariable) field.getGenericType();
                ClassAwareBean genericAwareBean = parentPropAwareBean.getGenericClasses().get(typeVariable.getName());
                try {
                    return (ClassAwareBean) genericAwareBean.clone();
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        return ClassAwareBeanFactory.getClassAwareBean(field.getType());
    }

    private void generateGenericClassesMap(ClassAwareBean classAwareBean, Type genericType) {
        if(genericType == null || !classAwareBean.getClassType().isCanBeGeneric()) {
            return;
        }
        final TypeVariable<? extends Class<?>>[] typeParams = classAwareBean.getType().getTypeParameters();

        if(typeParams.length != 0) {
            ParameterizedType parameterizedType;
            GenericClassAwareBean targetClassAwareBean;
            if (genericType instanceof GenericArrayType) {
                parameterizedType = (ParameterizedType)((GenericArrayType) genericType).getGenericComponentType();
                targetClassAwareBean = (GenericClassAwareBean) classAwareBean;
            } else if(genericType instanceof ParameterizedType) {
                parameterizedType = (ParameterizedType)genericType;
                targetClassAwareBean = (GenericClassAwareBean) classAwareBean;
            } else {
                return;
            }
            Type[] actualTypes = parameterizedType.getActualTypeArguments();
            Map<String, ClassAwareBean> genericAwareBeanMap = new HashMap<>();

            IntStream.range(0, typeParams.length).forEach(index -> {
                Type type = null;
                Type actualType = null;
                if (actualTypes.length > index) {
                    type = actualTypes[index];
                    if (type instanceof GenericArrayType) {
                        actualType = type;
                        type = ((GenericArrayType) type).getGenericComponentType();
                    }
                    if (type instanceof ParameterizedType) {
                        actualType = type;
                        type = ((ParameterizedType) type).getRawType();
                    }
                }
               // try {
                    Class<?> actualGenClass = type != null || type instanceof Class<?> ? /*Class.forName(((Class)type).getName())*/ Class.class.cast(type) : Object.class;
                    ClassAwareBean genericClassAwareBean = ClassAwareBeanFactory.getClassAwareBean(actualGenClass);
                    if (actualType != null){
                        generateGenericClassesMap(genericClassAwareBean, actualTypes[index]);
                    }
                    //setPropertyAwareInfo(genericClassAwareBean, prefix, type);
                    genericAwareBeanMap.put(typeParams[index].getName(), genericClassAwareBean);
               /* } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }*/

            });
            if (!genericAwareBeanMap.isEmpty()) {
                targetClassAwareBean.setGenericClasses(genericAwareBeanMap);
            }
        }

       /* ClassRepository classRepository = field.getType().getGenericInfo();
        if (classRepository != null) {

        }*/

    }

    private Optional<String> extractAnnotationMethodAsString(String name, Annotation annotation) {
        return extractAnnotationMethod(name, annotation, String.class);
    }

    private  <R> Optional<R> extractAnnotationMethod(String name, Annotation annotation, Class<R> type)  {
        try {
            if (annotation != null) {
                return Optional.ofNullable(type.cast(annotation.annotationType().getDeclaredMethod(name).invoke(annotation)));
            }
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}
