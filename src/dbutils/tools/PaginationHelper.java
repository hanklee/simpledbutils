/**
 *
 * Created Date: 2013-06-05 22:28
 */
package dbutils.tools;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.SQLException;
import java.util.List;

/**
 * simple pagination
 *
 * @author Xianling Li(hanklee)
 * $Id: PaginationHelper.java 2086 2013-06-09 07:03:39Z hanklee $
 */
public class PaginationHelper<E> {

    /**
     *
     * generate the page data
     *
     * @param qRunner QueryRunner class
     * @param sqlCountRows sql count string
     * @param sqlFetchRows sql fetch string
     * @param pageNo page number
     * @param pageSize page size
     * @param objectListHandler Object List handler Object
     * @param params sql parameter
     * @return  page object
     */
    public Page<E> fetchPage(
            final QueryRunner qRunner,
            final String sqlCountRows,
            final String sqlFetchRows,
            int pageNo,
            int pageSize,
            final SimpleObjectListHandler<E> objectListHandler,
            final Object... params) {
        try {

            // determine how many rows are available
            final int rowCount = ((Long) qRunner.query(sqlCountRows,
                    new ScalarHandler(), params)).intValue();

            // calculate the number of pages
            int pageCount = rowCount / pageSize;
            if (rowCount > pageSize * pageCount) {
                pageCount++;
            }

            if (pageNo > pageCount)
                pageNo = pageCount;

            // create the page object
            final Page<E> page = new Page<E>();
            page.setPageNumber(pageNo);
            page.setPagesAvailable(pageCount);

            // fetch a single page of results
            // Mysql sql
            final int startRow = (pageNo - 1) * pageSize;
            String mySqlFetch = sqlFetchRows + " LIMIT " + startRow + " , " +  pageSize;
            List<E> objects = qRunner.query(
                    mySqlFetch,
                    objectListHandler, params);

            page.getPageItems().addAll(objects);

            return page;
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
            return new Page<E>();
        }

    }
}
