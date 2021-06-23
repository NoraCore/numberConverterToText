package by.jazzTeam.csvWorkers;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Slf4j
public class ParseStrategy<T> {

    private final Class<T> className;
    private final InstanceValidator<T> validator;

    public ParseStrategy(Class<T> className, InstanceValidator<T> validator) {
        this.className = className;
        this.validator = validator;
    }

    private String findValueInMapByFieldName(String name, Map<String, String> args) {
        String key = "";
        for (String k : args.keySet()) {
            if (k.replaceAll("[_ ]", "").toLowerCase(Locale.ROOT).compareTo(name.toLowerCase(Locale.ROOT)) == 0) {
                key = k;
                break;
            }
        }
        return args.get(key);
    }

    public T instantiate(Map<String, String> args) {

        validator.validate(className, args.keySet());

        for (Constructor<?> constructor : className.getConstructors()) {
            Class<?>[] paramTypes = constructor.getParameterTypes();

            if (args.size() == paramTypes.length) {

                List<Object> convertedArgs = new ArrayList<>(args.size());

                Field[] fields = className.getDeclaredFields();
                Arrays.stream(fields).forEach(field -> {
                    String value = findValueInMapByFieldName(field.getName(), args);
                    convertedArgs.add(convert(field.getType(), value.trim()));
                });
                try {
                    T result =  (T) constructor.newInstance(convertedArgs.toArray());
                    return result;
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

        throw new IllegalArgumentException("Not support instantiate: " + className);
    }

    private Object convert(Class<?> target, String s) {
        if (target == Object.class || target == String.class || s == null) {
            return s;
        }
        if (target == Character.class || target == char.class) {
            return s.charAt(0);
        }
        if (target == Byte.class || target == byte.class) {
            return Byte.parseByte(s);
        }
        if (target == Short.class || target == short.class) {
            return Short.parseShort(s);
        }
        if (target == Integer.class || target == int.class) {
            return Integer.parseInt(s);
        }
        if (target == Long.class || target == long.class) {
            return Long.parseLong(s);
        }
        if (target == Float.class || target == float.class) {
            return Float.parseFloat(s);
        }
        if (target == Double.class || target == double.class) {
            return Double.parseDouble(s);
        }
        if (target == Boolean.class || target == boolean.class) {
            return Boolean.parseBoolean(s);
        }
        throw new IllegalArgumentException("Not support converting: " + target);
    }

}
