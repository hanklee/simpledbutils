/**
 *
 * (c) Copyright 2013
 * Created Time: 2013-06-07 15:58
 */
package dbutils.tools;

import java.lang.reflect.Field;
import java.util.Map;

/**
 *
 * I don't bean, so choose this implement not setter and getter.
 *
 * @author Xiangling Li(hanklee)
 *         $Id: SimpleMapToObject.java 2084 2013-06-08 10:16:17Z hanklee $
 */
public class SimpleMapToObject<T> implements MapToObject<T> {

    private Class<T> clazz;

    public SimpleMapToObject(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public T toObject(Map map) {
        T o = null;
        try {
            o = clazz.newInstance();
            for (Field field : clazz.getDeclaredFields()) {
                //Class type = field.getType();
                //Annotation[] annotations = field.getDeclaredAnnotations();
                String name = field.getName();
                Object value = map.get(name);
                if (value != null) {
                    field.set(o, value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return o;
    }
}
