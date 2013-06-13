/**
 *
 * (c) Copyright 2013
 * Created Time: 2013-06-07 16:02
 */
package dbutils.tools;

import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.RowProcessor;
import org.apache.commons.dbutils.handlers.AbstractListHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * @author Xiangling Li(hanklee)
 *         $Id: SimpleObjectListHandler.java 2082 2013-06-07 10:52:57Z hanklee $
 */
public class SimpleObjectListHandler<T> extends AbstractListHandler<T> {

    private RowProcessor convert = new BasicRowProcessor();
    private MapToObject<T> mapToObject;

    public SimpleObjectListHandler(Class clazz) {
        mapToObject = new SimpleMapToObject<T>(clazz);
    }

    @Override
    protected T handleRow(ResultSet resultSet) throws SQLException {
        Map map = this.convert.toMap(resultSet);
        return mapToObject.toObject(map);
    }
}
