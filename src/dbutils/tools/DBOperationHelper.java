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
    private static final Object insertLock = new Object();

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
     * @throws SQLException sql Exception
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
     * @throws SQLException sql Exception
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
     * @throws SQLException sql Exception
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
     * @throws SQLException sql Exception
     */
    public static void update(QueryRunner queryRunner, Object obj, String table) throws SQLException {
        Map<String, Object> map = new HashMap<String, Object>();

        try {
            Class clazz = obj.getClass();

            while (clazz != null) {
                Field[] field = clazz.getDeclaredFields();
                for (Field f : field) {
                    Object o = f.get(obj);
                    map.put(f.getName(), o);
                }
                clazz = clazz.getSuperclass();
            }

            Set<String> primary_keys = getPrimaryKey(queryRunner, table);
            //String primary_key = getPrimaryKey(queryRunner, table);

            StringBuilder s = new StringBuilder("update ").append(table).append(" set ");

            Set<String> columns = getColumns(queryRunner, table, primary_keys, false);
            //columns.remove(primary_key);
            Object[] objs = new Object[columns.size() + primary_keys.size()];
            int count = 0;
            int size = columns.size();
            for (String column : columns) {
                if (--size == 0) {
                    // last item
                    s.append(column).append("=? ");
                } else {
                    s.append(column).append("=?, ");
                }
                objs[count] = map.get(column);
                count++;
            }
            s.append(" where ");
            int key_size = primary_keys.size();
            int kye_i = 1;
            for (String primary_key : primary_keys) {
                s.append(primary_key).append(" = ? ");
                if (kye_i != key_size)
                    s.append(" AND ");
                objs[count] = map.get(primary_key);
                count++;
                kye_i++;
            }
            String sql = s.toString();
            //QueryRunner queryRunner =  DBDataSourceHelper.getQueryRunner();
            int mount = queryRunner.update(sql, objs);
            if (mount < 1) {
                throw new SQLException("No data update." + sql);
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
     * @throws SQLException sql Exception
     */
    public static void insert(QueryRunner queryRunner, Object obj, String table) throws SQLException {
        Map<String, Object> map = new HashMap<String, Object>();

        //Field.setAccessible(field, true);

        try {
            Set<String> primary_keys = getPrimaryKey(queryRunner, table);
            String[] array_keys = primary_keys.toArray(new String[primary_keys.size()]);

            Field keyField = null;
            Class clazz = obj.getClass();
            while (clazz != null) {
                Field[] field = clazz.getDeclaredFields();
                for (Field f : field) {
                    Object o = f.get(obj);
                    map.put(f.getName(), o);
                    if (array_keys.length == 1 && f.getName().equals(array_keys[0]))  // only test one element
                        keyField = f;
                }
                clazz = clazz.getSuperclass();
            }

            StringBuilder s = new StringBuilder("insert into ").append(table).append("(");
            StringBuilder sv = new StringBuilder(" values(");

            Set<String> columns = getColumns(queryRunner,
                    table, primary_keys, true); // true means if it is not auto increase then add key's column
            Object[] objs = new Object[columns.size()];
            int count = 0;
            int size = columns.size();
            for (String column : columns) {
                if (--size == 0) {
                    // last item
                    s.append(column).append(")");
                    sv.append("?)");
                } else {
                    s.append(column).append(",");
                    sv.append("?,");
                }
                objs[count] = map.get(column);
                count++;
            }

            String sql = s.append(sv).toString();

            synchronized (insertLock) {
                // no thread safe
                GenKeyQueryRunner qRunner = new GenKeyQueryRunner<Long>(queryRunner.getDataSource(),
                        new ScalarHandler<Long>(), array_keys);

                int mount = qRunner.insert(sql, objs);
                if (mount < 1) {
                    throw new SQLException("No data insert.");
                }

                if (keyField != null && !columns.contains(array_keys[0]) /* is auto increase */) {
                    if (keyField.getType().equals(Integer.TYPE)) {
                        keyField.set(obj, ((Long) qRunner.getGeneratedKeys()).intValue());
//                        System.out.println(sql);
                    } else if (keyField.getType().equals(Long.TYPE)) {
                        keyField.set(obj, qRunner.getGeneratedKeys());
                    }
                }
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
     * @throws SQLException
     */
    public static void delete(QueryRunner queryRunner, Object obj, String table) throws SQLException {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            Class clazz = obj.getClass();
            while (clazz != null) {
                Field[] field = clazz.getDeclaredFields();
                for (Field f : field) {
                    Object o = f.get(obj);
                    map.put(f.getName(), o);
                }
                clazz = clazz.getSuperclass();
            }

            Set<String> primary_keys = getPrimaryKey(queryRunner, table);

            StringBuilder s = new StringBuilder("delete from ");
            s.append(table).append(" where ");
            Object[] objs = new Object[primary_keys.size()];
            int count = 0;
            int key_size = primary_keys.size();
            int kye_i = 1;
            for (String primary_key : primary_keys) {
                s.append(primary_key).append(" = ? ");
                if (kye_i != key_size)
                    s.append(" AND ");
                objs[count] = map.get(primary_key);
                count++;
                kye_i++;
            }
            String sql = s.toString();
            int mount = queryRunner.update(sql, objs);
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
     * @param addKeys     is true and key is not auto increase then add columns
     * @return a Set of string
     * @throws SQLException
     */
    public static Set<String> getColumns(QueryRunner queryRunner, String table,
                                         Set<String> primaryKeys, boolean addKeys) throws SQLException {
        Set<String> columns = new HashSet<String>();
        Connection con = null;
        ResultSet rs;
        try {
            con = queryRunner.getDataSource().getConnection();
            rs = con.getMetaData().getColumns(null, null, table, null);
            while (rs.next()) { // column name in the NO. 4
                String name = rs.getString(4); //rs.getString("COLUMN_NAME");
                if (primaryKeys.contains(name)) {
                    if (addKeys && !"YES".equals(rs.getString("IS_AUTOINCREMENT"))) {
                        columns.add(name);
                    }
//                    System.out.println(rs.getString("IS_AUTOINCREMENT")); "YES"
                    // nothing
                } else {
                    columns.add(name);
                }
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
     * @throws SQLException
     */
    public static Set<String> getPrimaryKey(QueryRunner queryRunner, String table) throws SQLException {
        Set<String> keys = new HashSet<String>();
        String key;
        Connection con = null;
        ResultSet rs;
        try {
            con = queryRunner.getDataSource().getConnection();
            rs = con.getMetaData().getPrimaryKeys(null, null, table);

            while (rs.next()) { // column name in the NO. 4
                key = rs.getString(4);   //rs.getString("COLUMN_NAME");
                keys.add(key);
            }
        } finally {
            DbUtils.close(con);
        }
        return keys;
    }

    /**
     * generate table name by object
     *
     * @param obj Object
     * @return table name
     */
    public static String getTableNameByObject(Object obj) {
        try {
            Class clazz = obj.getClass();
            Field field = clazz.getField("table_name");
            if (field == null && clazz.getSuperclass() != null) {
                clazz = clazz.getSuperclass();
                field = clazz.getField("table_name");
            }
            if (field == null) {
                return obj.getClass().getSimpleName() + "s";
            }
            return (String) field.get(obj);
        } catch (Exception e) {
            return obj.getClass().getSimpleName() + "s";
        }
    }
}
