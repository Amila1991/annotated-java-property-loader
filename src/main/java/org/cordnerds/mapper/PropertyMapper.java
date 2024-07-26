package org.cordnerds.mapper;

import org.cordnerds.annotation.model.PropertyClassAwareBean;
import org.cordnerds.annotation.model.PropertyField;
import org.cordnerds.mapper.factory.PropertyMapperFactory;
import org.cordnerds.util.PropertyReaderUtil;
import org.cordnerds.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Amila Karunathilaka
 */
public abstract class PropertyMapper<T> {

    public final <R> Optional<R> mapProperties(T t, PropertyClassAwareBean propertyAwareBean) {
        //PropertyClassAwareBean propertyClass = PropertyClassAwareBean.class.cast(classAwareBean);
        Class<?> propClass = propertyAwareBean.getType();
        List<Method> setterMethods = getSetterList(propClass);
        List<Method> getterMethods = getGetterList(propClass);
        String propPrefix = propertyAwareBean.getPrefix();

        try {
            R propertyObj = (R) propClass.newInstance();

            T filteredT = StringUtils.isNotBlank(propPrefix) ? filterT(t, propPrefix) : t;
            propertyAwareBean.getPropertyFields().forEach(field -> {
                if (!field.getPropertyName().startsWith(propPrefix)) {
                    field.setPropertyName(propPrefix + "." + field.getName());
                }
                Optional<Method> method = setterMethods.stream().filter(methodFilter("set", field)).findFirst();

                if (isPropertyAvailable(filteredT, field.getPropertyName())) {
                    BiFunction<T, Object, ? extends Optional<?>> parserFunction = getParser(field);
                    if (parserFunction == null) {
                        return;
                    }
                    Optional<Method> getterMethod = getterMethods.stream().filter(methodFilter("get", field).or(methodFilter("is", field))).findFirst();
                    try {
                        Optional<?> value = parserFunction.apply(filteredT, getterMethod.isPresent() ? getterMethod.get().invoke(propertyObj) : null);

                        if (method.isPresent() && value.isPresent()) {
                             method.get().invoke(propertyObj, value.get());

                        } else {
                            System.out.println("Error2");
                            //todo warn log
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                } else {
                    //todo property is not available
                }
            });

            return Optional.ofNullable(propertyObj);
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public abstract PropertyMapperFactory.MapperType getMapperType();

    protected abstract T filterT(T t, String prefix);

    protected abstract boolean isPropertyAvailable(T t, String propName);

    protected abstract <P> BiFunction<T, Object, Optional<Object>> objectParserFunc(PropertyField propertyField);

    protected abstract <R> BiFunction<T, Object, Optional<R>> customObjParserFunc(PropertyField propertyField);

    protected abstract BiFunction<T, Object, Optional<String>> stringParserFunc(PropertyField propertyField);

    protected abstract BiFunction<T, Object, Optional<Integer>> intParserFunc(PropertyField propertyField);

    protected abstract BiFunction<T, Object, Optional<Long>> longParserFunc(PropertyField propertyField);

    protected abstract BiFunction<T, Object, Optional<Double>> doubleParserFunc(PropertyField propertyField);

    protected abstract BiFunction<T, Object, Optional<Float>> floatParserFunc(PropertyField propertyField);

    protected abstract BiFunction<T, Object, Optional<?>> arrayParserFunc(PropertyField propertyField);

    protected abstract <R> BiFunction<T, Object, Optional<List<R>>> listParserFunc(PropertyField propertyField);

    protected abstract <R> BiFunction<T, Object, Optional<Set<R>>> setParserFunc(PropertyField propertyField);

    protected abstract <K, V> BiFunction<T, Object, Optional<Map<K, V>>> mapParserFuc(PropertyField propertyField);

    protected final List<Method> getSetterList(Class<?> propClass) {
        return Arrays
                .stream(propClass.getMethods())
                .filter(PropertyReaderUtil::isSetter)
                .collect(Collectors.toList());
    }

    protected final List<Method> getGetterList(Class<?> propClass) {
        return Arrays
                .stream(propClass.getMethods())
                .filter(PropertyReaderUtil::isGetter)
                .collect(Collectors.toList());
    }


    protected BiFunction<T, Object, ? extends Optional<?>> getParser(PropertyField propertyField) {
        switch (propertyField.getTypeAwareBean().getClassType()) {
            case OBJECT:
                return objectParserFunc(propertyField);
            case ARRAY:
                return arrayParserFunc(propertyField);
            case LIST:
                return listParserFunc(propertyField);
            case SET:
                return setParserFunc(propertyField);
            case MAP:
                return mapParserFuc(propertyField);
            case BOOLEAN:
            case BOOLEAN_WRAPPER:
            case INT:
            case INTEGER_WRAPPER:
                return intParserFunc(propertyField);
            case LONG:
            case LONG_WRAPPER:
                return longParserFunc(propertyField);
            case FLOAT:
            case FLOAT_WRAPPER:
                return floatParserFunc(propertyField);
            case DOUBLE:
            case DOUBLE_WRAPPER:
                return doubleParserFunc(propertyField);
            case STRING:
                return stringParserFunc(propertyField);
            case CUSTOM:
                return customObjParserFunc(propertyField);
            default:
                return null;
        }
    }

    private Predicate<Method> methodFilter(String prefix, PropertyField field) {
        return method -> method.getName().toLowerCase().equals(prefix + field.getName().toLowerCase());
    }

}
