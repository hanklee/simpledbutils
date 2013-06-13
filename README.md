simpledbutils
=============

simple dbutils operations

Features:
=============


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

