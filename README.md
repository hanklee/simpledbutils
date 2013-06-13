simpledbutils
=============

simple dbutils operations

Features:
=============
1.very simple.
2.very light weight (9 classes).


Requirement:
=============
dbutils (http://commons.apache.org/proper/commons-dbutils/)
dbcp (http://commons.apache.org/proper/commons-dbcp/)
or c3p0 (https://github.com/swaldman/c3p0)



Introduction
=============

Database tools:

MapToObject :  a interface , subclass generate a Object by map data,there will be a
SimpleMapToObject, a object template class implement MapToObject.
I don't like setter and getter and I do not write bean class., so I do not use bean information class to process the Map to Object.
For example, User class
I prefer
public class User {
   public int id;
   public String name;
}

to

public class User{
  private int id;
  private String name;
  public setId(id) {this.id = id}
  public int getId() {return this.id}
  ...
  ...
}

DBOperationHelper, a database operation helper class, there are two helpful methods to help insert data and update data to database by a Object.

Page, a pagination class, to help data of database pagination.

PaginationHelper, a pagination helper class, to help create Page Object by pageNo,pageSize, sqlCountRows,sqlFetchRows and interface MapToObject.

dbutils  generate key   (Note: it is not thread safe)

https://issues.apache.org/jira/browse/DBUTILS-54

not like the link solution, I am not override super update method, I create insert method. Because I only just use this class when I inert the object data to table. And this solution is not thread safe, so I don't want use this class in any other operation.


Sample Code:
=============

entity class:

public class User {
    public String table_name = "users";

    public int id;
    public String name = "";
    public String password = "";
}


you can change use table name using table_name attribute.

Dao class:

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

It is very simple.


About the connection pool
==========================

After search the datasource and dbcp, I found c3p0

There is a post about c3p0 VS dbcp : http://stackoverflow.com/questions/520585/connection-pooling-options-with-jdbc-dbcp-vs-c3p0

http://tomcat.apache.org/tomcat-7.0-doc/jdbc-pool.html

JDBC connection pools: replacing commons-dbcp with tomcat-jdbc
http://feenixtech.blogspot.ro/2012/04/jdbc-connection-pools-replacing-commons.html

dbcp is faster than c3p0, c3p0 is scalable, Bonecp benchmark is the best but it did not release after 2011-04-05.
