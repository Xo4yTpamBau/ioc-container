package com.company;

import com.company.annotation.*;
import com.company.test.Cat;
import org.reflections.Reflections;

import java.lang.reflect.*;
import java.util.*;

public class Factory {

    private Map<String, Method> lazyClasses = new HashMap<>();
    private Map<String, Object> container = new HashMap<>();
    private Class<?> configurationClass;

    public Factory(Class<?> configurationClass) {
        this.configurationClass = configurationClass;
        List<Method> methods = readMethodDefinition();
        List<Constructor<?>> constructors = readClassDefinition();
        createBeans(methods, constructors);
    }

    public void searchValueFields(Object name) {
        try {
            for (Field declaredField : configurationClass.getDeclaredFields()) {
                if (declaredField.isAnnotationPresent(Value.class)) {
                    String value = declaredField.getDeclaredAnnotation(Value.class).value();
                    declaredField.setAccessible(true);
                    declaredField.set(name, value);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private List<Constructor<?>> readClassDefinition() {
        List<Constructor<?>> listConstructor = new ArrayList<>();
        String path = configurationClass.getDeclaredAnnotation(ComponentScan.class).basePackage();
        Reflections reflections = new Reflections(path);
        Set<Class<?>> typesAnnotatedWith = reflections.getTypesAnnotatedWith(Component.class);
        for (Class<?> aClass : typesAnnotatedWith) {
            Constructor<?>[] declaredConstructors = aClass.getDeclaredConstructors();
            listConstructor.add(declaredConstructors[0]);
        }
        listConstructor.sort(new ConstructorParameterCountComparator());
        return listConstructor;
    }

    private Object[] searchParameterArgs(Parameter[] parameters) {
        List<Object> array = new ArrayList<>();
        for (Parameter parameter : parameters) {
            if (parameter.isAnnotationPresent(Value.class)) {
                String value = parameter.getDeclaredAnnotation(Value.class).value();
                array.add(value);
            } else if (parameter.isAnnotationPresent(Qualifier.class)) {
                String value = parameter.getDeclaredAnnotation(Qualifier.class).value();
                Object e = container.get(value);
                if (e == null) throw new NullPointerException(value + " bean not found!");
                array.add(e);
            } else {
                Class<?> type = parameter.getType();
                Collection<Object> values = container.values();
                a:
                for (Object value : values) {
                    if (value.getClass().equals(type)) {
                        array.add(value);
                        break;
                    }
                    if (value.getClass().getSuperclass() != null && type.equals(value.getClass().getSuperclass())) {
                        array.add(value);
                        break;
                    }
                    for (Class<?> anInterface : type.getInterfaces()) {
                        if (anInterface.equals(value.getClass())) {
                            array.add(value);
                            break a;
                        }
                    }
                }
            }
        }
        if (array.size() != parameters.length)
            throw new NullPointerException("Some beans not found. Parameters: " + Arrays.toString(parameters) + ", Found: " + array);
        return array.toArray();
    }

    private void filterLazyDefinition(List<Method> a) {
        for (int i = 0; i < a.size(); i++) {
            if (a.get(i).isAnnotationPresent(Lazy.class)) {
                lazyClasses.put(a.get(i).getName(), a.get(i));
                a.remove(i);
            }
        }
    }

    private List<Method> readMethodDefinition() {
        Method[] declaredMethods = configurationClass.getDeclaredMethods();
        List<Method> methodArrayList = new ArrayList<>();
        for (Method declaredMethod : declaredMethods) {
            if (declaredMethod.isAnnotationPresent(Bean.class)) {
                methodArrayList.add(declaredMethod);
            }
        }
        filterLazyDefinition(methodArrayList);
        methodArrayList.sort(new MethodParameterCountComparator());
        return methodArrayList;
    }

    public List<Object> getBeans() {
        return new ArrayList<>(container.values());
    }

    private void createBeans(List<Method> methods, List<Constructor<?>> constructors) {
        Object o = null;
        try {
            o = configurationClass.getDeclaredConstructors()[0].newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 10; i++) {
            for (Method method : methods) {
                if (method.getParameterCount() == i) {
                    try {
                        String name = method.getName();
                        Parameter[] parameters = method.getParameters();
                        Object[] objects = searchParameterArgs(parameters);
                        Object invoke = method.invoke(o, objects);
                        invokeInitMethods(name, invoke);
                        container.put(name, invoke);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                    }
                }
            }
            for (Constructor<?> constructor : constructors) {
                if (constructor.getParameterCount() == i) {
                    try {
                        String[] split = constructor.getName().split("[.]");
                        String name = split[split.length - 1].toLowerCase();
                        Parameter[] parameters = constructor.getParameters();
                        Object[] objects = searchParameterArgs(parameters);
                        Object o1 = constructor.newInstance(objects);
                        invokeInitMethods(name, o1);
                        container.put(name, o1);
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    }
                }
            }
        }
    }

    private void invokeInitMethods(String name, Object o) {
        try {
            if (o.getClass().isAnnotationPresent(Component.class)) {
                for (Method declaredMethod : o.getClass().getDeclaredMethods()) {
                    if (declaredMethod.isAnnotationPresent(PostConstruct.class)) {
                        declaredMethod.invoke(o);
                    }
                }
            } else {
                Method[] declaredMethods = configurationClass.getDeclaredMethods();
                for (Method declaredMethod : declaredMethods) {
                    if (declaredMethod.getName().equals(name)) {
                        String[] strings = declaredMethod.getAnnotation(Bean.class).initMethod();
                        for (String string : strings) {
                            for (Method method : o.getClass().getDeclaredMethods()) {
                                if (method.getName().equals(string)) {
                                    method.invoke(o);
                                }
                            }
                        }
                    }
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        String name = Cat.class.getDeclaredConstructors()[0].getName();
        String[] split = name.split("[.]");
        String s = split[split.length - 1];
        System.out.println(s.toLowerCase());
    }


    public Object getBean(String name) {
        return container.get(name);
    }

    public void close() {
        Set<Map.Entry<String, Object>> entries = container.entrySet();
        for (Map.Entry<String, Object> entry : entries) {

            Object value = entry.getValue();
            String key = entry.getKey();

            try {
                if (value.getClass().isAnnotationPresent(Component.class)) {
                    for (Method declaredMethod : value.getClass().getDeclaredMethods()) {
                        if (declaredMethod.isAnnotationPresent(PreDestroy.class)) {
                            declaredMethod.invoke(value);
                        }
                    }
                } else {
                    Method[] declaredMethods = configurationClass.getDeclaredMethods();
                    for (Method declaredMethod : declaredMethods) {
                        if (declaredMethod.getName().equals(key)) {
                            String[] strings = declaredMethod.getAnnotation(Bean.class).destroyMethod();
                            for (String string : strings) {
                                Method[] declaredMethods2 = value.getClass().getDeclaredMethods();
                                for (Method method : declaredMethods2) {
                                    if (method.getName().equals(string)) {
                                        method.invoke(value);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        container = null;
    }
}
