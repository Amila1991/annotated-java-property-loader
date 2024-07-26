package org.cordnerds.mapper;

import org.cordnerds.annotation.model.*;
import org.cordnerds.mapper.factory.PropertyMapperFactory;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

/**
 * @author Amila Karunathilaka
 */
public class ClassPropertyMapper extends PropertyMapper<Map<String, String>> {

    @Override
    public PropertyMapperFactory.MapperType getMapperType() {
        return PropertyMapperFactory.MapperType.CLASSMAPPER;
    }

    @Override
    protected Map<String, String> filterT(Map<String, String> propertyMap, String prefix) {
        return propertyMap.entrySet().stream().filter(entry -> entry.getKey().startsWith(prefix)).collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
    }

    @Override
    protected boolean isPropertyAvailable(Map<String, String> propertyMap, String propName) {
        return propertyMap.keySet().stream().anyMatch(key -> key.startsWith(propName));
    }

    @Override
    protected BiFunction<Map<String, String>, Object, Optional<Object>> objectParserFunc(PropertyField propertyField) {
        return valueParser(propertyField, Object.class::cast);
    }

    @Override
    protected <R> BiFunction<Map<String, String>, Object, Optional<R>> customObjParserFunc(PropertyField propertyField) {
        return (propertyMap, instance) -> mapProperties(propertyMap, (PropertyClassAwareBean) propertyField.getTypeAwareBean());
    }

    @Override
    protected BiFunction<Map<String, String>, Object, Optional<String>> stringParserFunc(PropertyField propertyField) {
        return valueParser(propertyField, Function.identity());
    }

    @Override
    protected BiFunction<Map<String, String>, Object, Optional<Integer>> intParserFunc(PropertyField propertyField) {
        return valueParser(propertyField, Integer::parseInt);
    }

    @Override
    protected BiFunction<Map<String, String>, Object, Optional<Long>> longParserFunc(PropertyField propertyField) {
        return valueParser(propertyField, Long::parseLong);
    }

    @Override
    protected BiFunction<Map<String, String>, Object, Optional<Double>> doubleParserFunc(PropertyField propertyField) {
        return valueParser(propertyField, Double::parseDouble);
    }

    @Override
    protected BiFunction<Map<String, String>, Object, Optional<Float>> floatParserFunc(PropertyField propertyField) {
        return valueParser(propertyField, Float::parseFloat);
    }

    @Override
    protected BiFunction<Map<String, String>, Object, Optional<?>> arrayParserFunc(PropertyField propertyField) {
        return (propertyMap, instance) -> {
            ClassAwareBean componentAwareBean = ((ArrayClassAwareBean) propertyField.getTypeAwareBean()).getComponent();
            // Class<?> arrCompClass =propertyField.getTypeAwareBean().getType().getComponentType();
            // ClassType classType =
            //       ClassType.findPropertyFieldType(arrCompClass);
            /*Map<Integer, Map<String, String>> valueMapofMap = new HashMap<>();
            Map<String, String[]> valueMap = propertyMap.entrySet().stream().filter(entry -> entry.getKey().startsWith(propertyField.getPropertyName())).collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue().split(",")));
            valueMap.forEach((key, value) -> {
                IntStream.range(0, value.length).forEach(index -> {
                    Map<String, String> innerMap = new HashMap<>();
                    innerMap.put(key, value[index]);
                    valueMapofMap.merge(index, innerMap, (oldMap, newMap) -> {
                        oldMap.putAll(newMap);
                        return oldMap;
                    });
                });
            });*/

            List<Map<String, String>> splitedValueMaps = propertyValueSplitter(propertyMap, propertyField.getPropertyName());

            //AtomicReferenceArray<R> arrRef = new AtomicReferenceArray<R>(valueMapofMap.size());
            //  R[] arrR = (R[])Array.newInstance(componentAwareBean.getType(), valueMapofMap.size());
            PropertyField propField = new PropertyField();
            propField.setTypeAwareBean(componentAwareBean);
            propField.setPropertyName(propertyField.getPropertyName());
            BiFunction<Map<String, String>, ?, ? extends Optional<?>> parseFunction = getParser(propField);

          /*  valueMapofMap.forEach((index, value) -> {
                arrRef.set(index, (R) parseFunction.apply(value).orElse(null));
                //arrR[index] = (R) parseFunction.apply(value).orElse(null);
            });*/

            return mapToArray(componentAwareBean, splitedValueMaps, instance, parseFunction);


            /*Optional<String> strValue = stringParserFunc(propertyField).apply(propertyMap);
            if (strValue.isPresent()) {
                String[] arrValues = strValue.get().split(",");

                 Object[] arrObj = Arrays.stream(arrValues).map(value -> {

                    PropertyField propField = new PropertyField();
                    //propField.setFieldClassType(classType);
                    propField.setPropertyName("$0");
                     Function<Map<String, String>, ? extends Optional<?>> parserFunction = getParser(propField);

                    Map<String, String> map = Collections.singletonMap("$0", value);
                    return parserFunction.apply(map);
                }).filter(valueOpt -> valueOpt.isPresent()).map(Optional::get).map(value -> {
                     System.out.println("dd");
                    return value;
                 }).toArray();

                 return Optional.ofNullable(mapToArray(componentAwareBean.getClassType(), componentAwareBean.getType(), arrObj));
            }
            return Optional.empty();*/
        };
    }

    @Override
    protected <R> BiFunction<Map<String, String>, Object, Optional<List<R>>> listParserFunc(PropertyField propertyField) {
        return (propertyMap, instance) -> {

            return Optional.ofNullable(listAndSetParser(propertyField, propertyMap, instance));

            /*CollectionClassAwareBean listAwareBean = (CollectionClassAwareBean) propertyField.getTypeAwareBean();
            ClassAwareBean genericAwareBean = listAwareBean.getGenericClasses().get(listAwareBean.getType().getTypeParameters()[0].getName());
            List<Map<String, String>> splitedValueMaps = propertyValueSplitter(propertyMap, propertyField.getPropertyName());
            PropertyField propField = new PropertyField();
            propField.setTypeAwareBean(genericAwareBean);
            propField.setPropertyName(propertyField.getPropertyName());
            BiFunction<Map<String, String>, ?, ? extends Optional<?>> parseFunction = getParser(propField);

            Stream<R> valueStream = (Stream<R>) splitedValueMaps.stream().map(valueMap -> parseFunction.apply(valueMap, null)).filter(Optional::isPresent).map(Optional::get).map(genericAwareBean.getType()::cast);

            List<R> valueList = null;

            if (instance != null) {
                valueList = (List<R>) instance;
                valueStream.forEach(valueList::add);
            } else if (listAwareBean.getType().isInterface() || Modifier.isAbstract(listAwareBean.getType().getModifiers())) {
                valueList = valueStream.collect(Collectors.toList());
            } else {
                try {
                    valueList = (List<R>) listAwareBean.getType().newInstance();
                    valueStream.forEach(valueList::add);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            return Optional.ofNullable(valueList);*/
            //return Optional.of((List<R>) splitedValueMaps.stream().map(valueMap -> parseFunction.apply(valueMap, null)).filter(Optional::isPresent).map(Optional::get).map(genericAwareBean.getType()::cast).collect(Collectors.toList()));

           /* Class<?> arrCompClass =propertyField.getTypeAwareBean().getType().getComponentType();
            ClassType classType =
                    ClassType.findPropertyFieldType(arrCompClass);
            Optional<String> strValue = stringParserFunc(propertyField).apply(propertyMap);
            if (strValue.isPresent()) {
                String[] arrValues = strValue.get().split(",");

                Object[] arrObj = Arrays.stream(arrValues).map(value -> {

                    PropertyField propField = new PropertyField();
                    //propField.setFieldClassType(classType);
                    propField.setPropertyName("$0");
                    Function<Map<String, String>, ? extends Optional<?>> parserFunction = getParser(propField);

                    Map<String, String> map = Collections.singletonMap("$0", value);
                    return parserFunction.apply(map);
                }).filter(valueOpt -> valueOpt.isPresent()).map(Optional::get).map(value -> {
                    System.out.println("dd");
                    return value;
                }).toArray();

                return Optional.empty();//Optional.ofNullable(mapToArray(propertyFieldType, arrCompClass, arrObj));
            }
            return Optional.empty();*/
        };
    }

    @Override
    protected <R> BiFunction<Map<String, String>, Object, Optional<Set<R>>> setParserFunc(PropertyField propertyField) {
        return (propertyMap, instance) -> Optional.ofNullable(listAndSetParser(propertyField, propertyMap, instance));
            /*CollectionClassAwareBean listAwareBean = (CollectionClassAwareBean) propertyField.getTypeAwareBean();
            ClassAwareBean genericAwareBean = listAwareBean.getGenericClasses().get(listAwareBean.getType().getTypeParameters()[0].getName());
            List<Map<String, String>> splitedValueMaps = propertyValueSplitter(propertyMap, propertyField.getPropertyName());
            PropertyField propField = new PropertyField();
            propField.setTypeAwareBean(genericAwareBean);
            propField.setPropertyName(propertyField.getPropertyName());
            BiFunction<Map<String, String>, ?, ? extends Optional<?>> parseFunction = getParser(propField);

            return Optional.of((Set<R>) splitedValueMaps.stream().map(valueMap -> parseFunction.apply(valueMap, null)).filter(Optional::isPresent).map(Optional::get).map(genericAwareBean.getType()::cast).collect(Collectors.toSet()));*/
    }

    @Override
    protected <K, V> BiFunction<Map<String, String>, Object, Optional<Map<K, V>>> mapParserFuc(PropertyField propertyField) {
        return (propertyMap, instance) -> {
            CollectionClassAwareBean collectionAwareBean = (CollectionClassAwareBean) propertyField.getTypeAwareBean();
            ClassAwareBean keyAwareBean = collectionAwareBean.getGenericClasses().get(collectionAwareBean.getType().getTypeParameters()[0].getName());
            ClassAwareBean valueAwareBean = collectionAwareBean.getGenericClasses().get(collectionAwareBean.getType().getTypeParameters()[1].getName());
            PropertyField keyField = new PropertyField();
            keyField.setTypeAwareBean(keyAwareBean);
            keyField.setPropertyName("keyProperty");
            PropertyField valueField = new PropertyField();
            valueField.setTypeAwareBean(valueAwareBean);

            BiFunction<Map<String, String>, ?, ? extends Optional<?>> keyFunction = getParser(keyField);
            BiFunction<Map<String, String>, ?, ? extends Optional<?>> valueFunction = getParser(valueField);
            Map<String, Map<String, String>> mapofValueMap = propertyMap.entrySet().stream().filter(entry -> entry.getKey().startsWith(propertyField.getPropertyName())).collect(Collectors.groupingBy(entry -> {
                String propNameWithoutPrefix = entry.getKey().replace(propertyField.getPropertyName(), "");
                String[] propNameSplitedFromDot = (propNameWithoutPrefix.startsWith(".") ? propNameWithoutPrefix.substring(1) : propNameWithoutPrefix).split("\\.");
                System.out.println(Arrays.toString(propNameSplitedFromDot));
                return propNameSplitedFromDot[0];
            }, Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue())));

          /*  Stream<Map.Entry<K, V>> entryStream = mapofValueMap.entrySet().stream().map(entry ->
                    new AbstractMap.SimpleEntry<>(
                        (K) keyFunction.apply(Collections.singletonMap("keyProperty", entry.getKey()), null),
                        (V) valueFunction.apply(entry.getValue(), null)));*/
            Map<K, V> valueMap;

            try {
                if (instance != null) {
                    valueMap = (Map<K, V>) instance;
                } else if (collectionAwareBean.getType().isInterface() || Modifier.isAbstract(collectionAwareBean.getType().getModifiers())) {
                    valueMap = (Map<K, V>) collectionAwareBean.getClassType().getDefaultType().newInstance();
                } else {
                    valueMap = (Map<K, V>) collectionAwareBean.getType().newInstance();
                }
               // valueStream.forEach(valueCollection::add);
                mapofValueMap
                        .entrySet()
                        .stream()
                        .forEach(entry -> {
                            valueField.setPropertyName(propertyField.getPropertyName() + "." + entry.getKey());
                            if (ClassType.CUSTOM.equals(valueAwareBean.getClassType())) {
                                ((PropertyClassAwareBean)valueAwareBean).setPrefix(propertyField.getPropertyName() + "." + entry.getKey());
                            }
                            Optional<K> keyOpt = (Optional<K>) keyFunction.apply(Collections.singletonMap("keyProperty", entry.getKey()), null);
                            Optional<V> valueOpt = (Optional<V>) valueFunction.apply(entry.getValue(), null);
                            if (keyOpt.isPresent() && valueOpt.isPresent()) {
                                valueMap.put(keyOpt.get(), valueOpt.get());
                            }
                        });
                return Optional.ofNullable(valueMap);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }


            return Optional.empty();
        };
    }

    private <R> BiFunction<Map<String, String>, Object, Optional<R>> valueParser(PropertyField field, Function<String, R> mapFunc) {
        return (propertyMap, instance) -> propertyMap
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().equals(field.getPropertyName()))
                .map(Map.Entry::getValue)
                .map(mapFunc).findFirst();
    }

    private List<Map<String, String>> propertyValueSplitter(Map<String, String> propertyMap, String propertyName) {
        //  ClassAwareBean componentAwareBean = ((ArrayClassAwareBean)propertyField.getTypeAwareBean()).getComponent();
        // Class<?> arrCompClass =propertyField.getTypeAwareBean().getType().getComponentType();
        // ClassType classType =
        //       ClassType.findPropertyFieldType(arrCompClass);
        //Map<Integer, Map<String, String>> valueMapofMap = new HashMap<>();
        List<Map<String, String>> valueMapList = new ArrayList<>();
        Map<String, String[]> valueMap = propertyMap.entrySet().stream().filter(entry -> entry.getKey().startsWith(propertyName)).collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue().split(",")));
        valueMap.forEach((key, value) -> {
            IntStream.range(0, value.length).forEach(index -> {
                if (valueMapList.isEmpty() || valueMapList.size() <= index) {
                    valueMapList.add(index, new HashMap<>());
                }
                Map<String, String> innerMap = valueMapList.get(index);
                innerMap.put(key, value[index]);
            });
        });
        return valueMapList;
    }

    private <R> Optional<Object> mapToArray(ClassAwareBean componentAwareBean, List<Map<String, String>> valueMapList,
                                            Object instance, BiFunction<Map<String, String>, ?, ? extends Optional<?>> parseFunction) {

        switch (componentAwareBean.getClassType()) {
            case INT:
                int[] intArr = instance != null ? (int[]) instance : new int[valueMapList.size()];
                IntStream.range(0, intArr.length).forEach(index -> {
                    intArr[index] = Integer.class.cast(parseFunction.apply(valueMapList.get(index), null).get());
                });
         /*       valueMap.forEach((index, value) -> {
                    intArr[index] = Integer.class.cast(parseFunction.apply(value).get());
                });*/
              /*  IntStream.range(0, objArr.length).forEach(index -> {
                    intArr[index] = Integer.class.cast(objArr[index]);
                });*/
                return ofNullable(intArr);
            case LONG:
                long[] longArr = instance != null ? (long[]) instance : new long[valueMapList.size()];
                IntStream.range(0, longArr.length).forEach(index -> {
                    longArr[index] = Long.class.cast(parseFunction.apply(valueMapList.get(index), null).get());
                });
                /*valueMap.forEach((index, value) -> {
                    longArr[index] = Long.class.cast(parseFunction.apply(value).get());
                });*/
                return ofNullable(longArr);
            case DOUBLE:
                double[] doubleArr = instance != null ? (double[]) instance : new double[valueMapList.size()];
                IntStream.range(0, doubleArr.length).forEach(index -> {
                    doubleArr[index] = Double.class.cast(parseFunction.apply(valueMapList.get(index), null).get());
                });
               /* valueMap.forEach((index, value) -> {
                    doubleArr[index] = Double.class.cast(parseFunction.apply(value).get());
                });*/
                return ofNullable(doubleArr);
            default:
                Stream<?> valueStream = valueMapList
                        .stream()
                        .map(valueMap -> parseFunction.apply(valueMap, null))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .map(componentAwareBean.getType()::cast);
                if (instance != null) {
                    valueStream = valueStream.limit(((R[]) instance).length);
                }
                return Optional.of(valueStream.toArray(listSize -> (R[]) Array.newInstance(componentAwareBean.getType(), listSize)));
        }
    }

    private <R extends Collection<V>, V> R listAndSetParser(PropertyField propertyField, Map<String, String> propertyMap, Object instance) {
        CollectionClassAwareBean collectionAwareBean = (CollectionClassAwareBean) propertyField.getTypeAwareBean();
        ClassAwareBean genericAwareBean = collectionAwareBean.getGenericClasses().get(collectionAwareBean.getType().getTypeParameters()[0].getName());
        List<Map<String, String>> splitedValueMaps = propertyValueSplitter(propertyMap, propertyField.getPropertyName());
        PropertyField propField = new PropertyField();
        propField.setTypeAwareBean(genericAwareBean);
        propField.setPropertyName(propertyField.getPropertyName());
        BiFunction<Map<String, String>, ?, ? extends Optional<?>> parseFunction = getParser(propField);

        Stream<V> valueStream = (Stream<V>) splitedValueMaps.stream().map(valueMap -> parseFunction.apply(valueMap, null)).filter(Optional::isPresent).map(Optional::get).map(genericAwareBean.getType()::cast);

        R valueCollection = null;

        try {
            if (instance != null) {
                valueCollection = (R) instance;
            } else if (collectionAwareBean.getType().isInterface() || Modifier.isAbstract(collectionAwareBean.getType().getModifiers())) {
                valueCollection = (R) collectionAwareBean.getClassType().getDefaultType().newInstance();
            } else {
                valueCollection = (R) collectionAwareBean.getType().newInstance();
            }
            valueStream.forEach(valueCollection::add);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    return valueCollection;
}

    private <R> Object mapToArray(ClassType fieldType, Class<R> rClass, Object[] objArr) {

        switch (fieldType) {
            case INT:
                int[] intArr = new int[objArr.length];
                IntStream.range(0, objArr.length).forEach(index -> {
                    intArr[index] = Integer.class.cast(objArr[index]);
                });
                return intArr;
            case LONG:
                long[] longArr = new long[objArr.length];
                IntStream.range(0, objArr.length).forEach(index -> {
                    longArr[index] = Long.class.cast(objArr[index]);
                });
                return longArr;
            case DOUBLE:
                double[] doubleArr = new double[objArr.length];
                IntStream.range(0, objArr.length).forEach(index -> {
                    doubleArr[index] = Double.class.cast(objArr[index]);
                });
                return doubleArr;
            case FLOAT:
                float[] floatArr = new float[objArr.length];

            case BOOLEAN:
            default:
                return Arrays.stream(objArr).map(rClass::cast).toArray(value -> (R[]) Array.newInstance(rClass, value));

        }
    }

}
