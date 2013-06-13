/**
 *
 * (c) 2013 Copyright
 * Created Time: 2013-06-13 13:18
 */
package dbutils.example;

import dbutils.tools.*;
import org.apache.commons.dbutils.QueryRunner;

import java.sql.SQLException;

/**
 * @author Xiangling Li(hanklee)
 *         $Id$
 */
public class UserDAO {

    void createUser(String name, String pass) throws SQLException {
        name = name.toLowerCase();
        QueryRunner qRunner = DBDataSourceHelper.getQueryRunner();
        User user = new User();
        user.name = name;
        user.password = pass;
        DBOperationHelper.insert(user);
    }

    User login(String name, String pass) throws SQLException {
        name = name.toLowerCase();
        User user = null;
        QueryRunner qRunner = DBDataSourceHelper.getQueryRunner();
        user = qRunner.query(
                "select * from users where name = ? and pass = ?",
                new SimpleObjectHandler<User>(User.class), name, pass);
        return user;
    }

    void update(User user) throws SQLException {
        DBOperationHelper.update(user);
    }

    void delete(User user) throws SQLException {
        DBOperationHelper.delete(user);
    }

    Page<User> getUsers(int pageNo, int pageSize) throws SQLException {
        String countSQL = "SELECT count(*) FROM users";
        String fetchSQL = "SELECT * FROM users";
        PaginationHelper<User> pageHelp = new PaginationHelper<User>();
        return pageHelp.fetchPage(DBDataSourceHelper.getQueryRunner(),
                countSQL, fetchSQL, pageNo, pageSize,
                new SimpleObjectListHandler<User>(User.class));
    }
}
