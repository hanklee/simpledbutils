/**
 *
 * (c) Copyright 2013
 * Created Time: 2013-06-07 15:47
 */
package dbutils.tools;

import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.RowProcessor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * @author Xiangling Li(hanklee)
 *         $Id: SimpleObjectHandler.java 2082 2013-06-07 10:52:57Z hanklee $
 */
public class SimpleObjectHandler<T> implements ResultSetHandler<T> {

    private RowProcessor convert = new BasicRowProcessor();
    private MapToObject<T> mapToObject;

    public SimpleObjectHandler(Class<T> clazz) {
        mapToObject = new SimpleMapToObject<T>(clazz);
    }

    @Override
    public T handle(ResultSet resultSet) throws SQLException {
        // it must execute rs.next()
        Map map = resultSet.next() ? this.convert.toMap(resultSet) : null;
        if (map == null) return null;
        return mapToObject.toObject(map);
    }
}
