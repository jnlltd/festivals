package ie.festivals

import groovy.sql.Sql
import org.hibernate.SessionFactory
import org.hibernate.jdbc.Work

import java.sql.Connection
import java.sql.SQLException


abstract class AbstractJdbcService {

    SessionFactory sessionFactory

    protected void doJdbcWork(Closure jdbcWork) {

        sessionFactory.currentSession.doWork(

                new Work() {
                    @Override
                    void execute(Connection connection) throws SQLException {

                        // do not close this Sql instance ourselves
                        Sql sql = new Sql(connection)
                        jdbcWork(sql)
                    }
                }
        )
    }
}
