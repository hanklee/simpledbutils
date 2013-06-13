/**
 *
 * DBDataSourceHelper.java May 29, 2013
 */
package dbutils.tools;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.commons.dbutils.QueryRunner;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Singleton pattern
 *
 * @author Xianling
 *         $Id: DBDataSourceHelper.java 2085 2013-06-08 17:06:50Z hanklee $
 */
public class DBDataSourceHelper {
    private static final String DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull";
    private static final String USER_NAME = "root";
    private static final String USER_PASS = "test";

    private static final String DBFILE = "jdbc.properties";

    private static DataSource userDataSource = null;

    private static DataSource dataSource = null;

    private DBDataSourceHelper() {
    }

    /**
     * synchronize this method.
     *
     * @return QueryRunner
     */
    public synchronized static QueryRunner getQueryRunner() {
        if (dataSource != null) {
            return new QueryRunner(dataSource);
        }
        // dbcp
        // if (DBDataSourceHelper.dataSource == null) {
        // BasicDataSource dbcpDataSource = new BasicDataSource();
        // dbcpDataSource
        // .setUrl("jdbc:mysql://localhost:3306/game?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull");
        // dbcpDataSource.setDriverClassName("com.mysql.jdbc.Driver");
        // dbcpDataSource.setUsername("root");
        // dbcpDataSource.setPassword("hank");
        // dbcpDataSource.setDefaultAutoCommit(true);
        // dbcpDataSource.setMaxActive(100);
        // dbcpDataSource.setMaxIdle(30);
        // dbcpDataSource.setMaxWait(500);
        //
        // DBDataSourceHelper.dataSource = (DataSource) dbcpDataSource;
        // System.out.println("Initialize dbcp...");
        // }
        InputStream in;
        Properties p = new Properties();
        try {
            in = new BufferedInputStream(new FileInputStream(DBFILE));
            //in = DBDataSourceHelper.class.getClassLoader().getResourceAsStream(DBFILE);
            p.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            ComboPooledDataSource c3p0DataSource = new ComboPooledDataSource();
            c3p0DataSource.setDriverClass(p.getProperty("game.jdbc.driverClassName", DRIVER));
            c3p0DataSource.setJdbcUrl(p.getProperty("game.jdbc.url", DB_URL));
            c3p0DataSource.setUser(p.getProperty("game.jdbc.user", USER_NAME));
            c3p0DataSource.setPassword(p.getProperty("game.jdbc.password", USER_PASS));
            // c3p0DataSource.setMaxIdleTime(30); // default is 0 seconds, 0
            // means connections never expire
            c3p0DataSource.setMaxPoolSize(100); // default is 15
            dataSource = c3p0DataSource;
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }
        return new QueryRunner(dataSource);
    }

}
