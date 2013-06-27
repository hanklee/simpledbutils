/**
 *
 * (c) Copyright 2013
 * Created Time: 2013-06-07 09:36
 */
package dbutils.tools;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p/>
 * Database operation helper
 * <p/>
 * three very helpful methods:<br/>
 * {@link #insert(org.apache.commons.dbutils.QueryRunner, Object, String)} insert Object to table<br/>
 * {@link #update(org.apache.commons.dbutils.QueryRunner, Object, String)} update Object to table <br/>
 * {@link #delete(org.apache.commons.dbutils.QueryRunner, Object, String)} delete Object's data in the table <br/> <br/>
 * also there are three similar no table name methods <br/>
 * {@link #insert(org.apache.commons.dbutils.QueryRunner, Object)},{@link #update(org.apache.commons.dbutils.QueryRunner, Object)},{@link #delete(org.apache.commons.dbutils.QueryRunner, Object)}<br/>
 * The table name generate by Object class name + 's', this idea inspired by PHP Cake.
 * <p/>
 *
 * @author Xiangling Li(hanklee)
 *         $Id: DBOperationHelper.java 2125 2013-06-20 14:56:44Z hanklee $
 */
public class DBOperationHelper {

    private static QueryRunner innerRunner;

    public static void setInnerRunner(QueryRunner qRunner) {
        innerRunner = qRunner;
    }

    public static void update(Object obj) throws SQLException {
        update(innerRunner, obj);
    }

    public static void insert(Object obj) throws SQLException {
        insert(innerRunner, obj);
    }

    public static void delete(Object obj) throws SQLException {
        delete(innerRunner, obj);
    }

    /**
     * update a object's data to database without table name
     * table name generate by object class name + 's'
     *
     * @param queryRunner dbutils QueryRunner class
     * @param obj         an object to update his data to database
     * @throws java.sql.SQLException sql Exception
     */
    public static void update(QueryRunner queryRunner, Object obj) throws SQLException {
        String table = getTableNameByObject(obj);
        // this idea is inspired by php cake
        update(queryRunner, obj, table);
    }

    /**
     * insert a object's data to database without table name
     *
     * @param queryRunner dbutils QueryRunner class
     * @param obj         an object to insert his data to database
     * @throws java.sql.SQLException sql Exception
     */
    public static void insert(QueryRunner queryRunner, Object obj) throws SQLException {
        String table = getTableNameByObject(obj);
        insert(queryRunner, obj, table);
    }

    /**
     * delete table object's data without table name
     *
     * @param queryRunner dbutils QueryRunner class
     * @param obj         a Object relate the table's row data
     * @throws java.sql.SQLException sql Exception
     */
    public static void delete(QueryRunner queryRunner, Object obj) throws SQLException {
        String table = getTableNameByObject(obj);
        delete(queryRunner, obj, table);
    }

    /**
     * update a object's data to database
     *
     * @param queryRunner dbutils QueryRunner class
     * @param obj         an object to update his data to database
     * @param table       table name
     * @throws java.sql.SQLException sql Exception
     */
    public static void update(QueryRunner queryRunner, Object obj, String table) throws SQLException {
        Map<String, Object> map = new HashMap<String, Object>();
        Field[] field = obj.getClass().getDeclaredFields();

        try {
            for (Field f : field) {
                Object o = f.get(obj);
                map.put(f.getName(), o);
            }

            String primary_key = getPrimaryKey(queryRunner, table);

            StringBuilder s = new StringBuilder("update " + table + " set ");

            Set<String> columns = getColumns(queryRunner, table, primary_key, false);
            //columns.remove(primary_key);
            Object[] objs = new Object[columns.size() + 1];
            int count = 0;
            int size = columns.size();
            for (String column : columns) {
                if (--size == 0) {
                    // last item
                    s.append(column + "=? ");
                } else {
                    s.append(column + "=?, ");
                }
                objs[count] = map.get(column);
                count++;
            }
            s.append(" where ").append(primary_key).append(" = ?");
            objs[count] = map.get(primary_key);
            String sql = s.toString();
            //QueryRunner queryRunner =  DBDataSourceHelper.getQueryRunner();
            int mount = queryRunner.update(sql, objs);
            if (mount < 1) {
                throw new SQLException("No data update.");
            }
        } catch (Exception e) {
            throw new SQLException(e.getMessage());
        }
    }

    /**
     * insert a object's data to database
     *
     * @param queryRunner dbutils QueryRunner class
     * @param obj         an object to insert his data to database
     * @param table       table name
     * @throws java.sql.SQLException sql Exception
     */
    public static void insert(QueryRunner queryRunner, Object obj, String table) throws SQLException {
        Map<String, Object> map = new HashMap<String, Object>();
        Field[] field = obj.getClass().getDeclaredFields();
        //Field.setAccessible(field, true);

        try {
            String primary_key = getPrimaryKey(queryRunner, table);
            Field keyField = null;
            for (Field f : field) {
                Object o = f.get(obj);
                map.put(f.getName(), o);
                if (f.getName().equals(primary_key))
                    keyField = f;
            }

            StringBuffer s = new StringBuffer("insert into " + table + "(");
            StringBuilder sv = new StringBuilder(" values(");

            Set<String> columns = getColumns(queryRunner, table, primary_key,true);
            Object[] objs = new Object[columns.size()];
            int count = 0;
            int size = columns.size();
            for (String column : columns) {
                if (--size == 0) {
                    // last item
                    s.append(column + ")");
                    sv.append("?)");
                } else {
                    s.append(column + ",");
                    sv.append("?,");
                }
                objs[count] = map.get(column);
                count++;
            }

            String sql = s.append(sv).toString();

            // no thread safe
            GenKeyQueryRunner qRunner = new GenKeyQueryRunner(queryRunner.getDataSource(),
                    new ScalarHandler(), primary_key);

            int mount = qRunner.insert(sql, objs);
            if (mount < 1) {
                throw new SQLException("No data insert.");
            }
            if (keyField != null && !columns.contains(primary_key) /* is auto increase */) {
                //System.out.println(".....generate id:" + qRunner.getGeneratedKeys());
                if (keyField.getType().equals(Integer.TYPE))
                    keyField.set(obj, ((Long) qRunner.getGeneratedKeys()).intValue());
                else if (keyField.getType().equals(Long.TYPE))
                    keyField.set(obj, qRunner.getGeneratedKeys());
            }
        } catch (Exception e) {
            throw new SQLException(e.getMessage());
        }
    }

    /**
     * delete table object's data method
     *
     * @param queryRunner dbutils QueryRunner class
     * @param obj         a Object relate the table's row data
     * @param table       table name
     * @throws java.sql.SQLException
     */
    public static void delete(QueryRunner queryRunner, Object obj, String table) throws SQLException {
        Map<String, Object> map = new HashMap<String, Object>();
        Field[] field = obj.getClass().getDeclaredFields();
        //Field.setAccessible(field, true);

        try {
            for (Field f : field) {
                Object o = f.get(obj);
                map.put(f.getName(), o);
            }

            String primary_key = getPrimaryKey(queryRunner, table);

            StringBuilder s = new StringBuilder("delete from ");
            s.append(table).append(" where ").append(primary_key).append(" = ?");
            Object key_value = map.get(primary_key);
            String sql = s.toString();
            int mount = queryRunner.update(sql, key_value);
            if (mount < 1) {
                throw new SQLException("No data delete.");
            }
        } catch (Exception e) {
            throw new SQLException(e.getMessage());
        }
    }

    /**
     * get table columns
     *
     * @param queryRunner dbutils QueryRunner class
     * @param table       table name
     * @param useAutoincrease use auto increase to delete the primary key
     * @return a Set of string
     * @throws java.sql.SQLException
     */
    public static Set<String> getColumns(QueryRunner queryRunner, String table,
                                         String primaryKey,boolean useAutoincrease) throws SQLException {
        Set<String> columns = new HashSet<String>();
        Connection con = null;
        ResultSet rs;
        try {
            con = queryRunner.getDataSource().getConnection();
            rs = con.getMetaData().getColumns(null, null, table, null);
            while (rs.next()) { //字段名称在第 4 列
                String name = rs.getString(4);
                if (name.equals(primaryKey) && ( !useAutoincrease || "true".equals(rs.getString("IS_AUTOINCREMENT"))) ) {
                    // nothing
                } else {
                    columns.add(name);
                }
                //rs.getString("COLUMN_NAME");
                //;
            }
        } finally {
            DbUtils.close(con);
        }
        return columns;
    }

    /**
     * get primary key name of the table
     *
     * @param queryRunner dbutils QueryRunner class
     * @param table       table name
     * @return primary key name
     * @throws java.sql.SQLException
     */
    public static String getPrimaryKey(QueryRunner queryRunner, String table) throws SQLException {
        String key = null;
        Connection con = null;
        ResultSet rs;
        try {
            con = queryRunner.getDataSource().getConnection();
            rs = con.getMetaData().getPrimaryKeys(null, null, table);

            if (rs.next()) { //字段名称在第 4 列
                key = rs.getString(4);   //rs.getString("COLUMN_NAME");
            }
        } finally {
            DbUtils.close(con);
        }
        return key;
    }

    /**
     * generate table name by object
     *
     * @param obj Object
     * @return table name
     */
    public static String getTableNameByObject(Object obj) {
        try {
            Field field = obj.getClass().getField("table_name");
            return (String) field.get(obj);
        } catch (Exception e) {
            return obj.getClass().getSimpleName() + "s";
        }
    }
}
