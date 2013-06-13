/**
 *
 * (c) Copyright 2013
 * Created Time: 2013-06-06 14:38
 */
package dbutils.tools;

import java.util.Map;

/**
 *
 * for data base translate Map to Object
 *
 * @author Xiangling Li(hanklee)
 *         $Id: MapToObject.java 2082 2013-06-07 10:52:57Z hanklee $
 */
public interface MapToObject<E> {

    E toObject(Map map);

}
