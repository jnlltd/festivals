import ie.festivals.hibernate.UTF8MySQL5InnoDBDialect

import java.sql.Connection

dataSource {
    pooled = true
    driverClassName = "com.mysql.jdbc.Driver"
    dialect = UTF8MySQL5InnoDBDialect.name
    logSql = false
    url = "jdbc:mysql://localhost/festival?useUnicode=yes"

    // http://grails.org/2.3.6%20Release%20Notes
    properties {
        // See http://grails.org/doc/latest/guide/conf.html#dataSource for documentation
        jmxEnabled = false
        initialSize = 5
        maxActive = 50
        minIdle = 5
        maxIdle = 25
        maxWait = 10000
        maxAge = 10 * 60000
        timeBetweenEvictionRunsMillis = 5000
        minEvictableIdleTimeMillis = 60000
        validationQuery = "SELECT 1"
        validationQueryTimeout = 3
        validationInterval = 15000
        testOnBorrow = true
        testWhileIdle = true
        testOnReturn = false
        jdbcInterceptors = "ConnectionState;StatementCache(max=200)"
        defaultTransactionIsolation = Connection.TRANSACTION_READ_COMMITTED
    }
}

hibernate {
    // https://grails.org/2.4.3+Release+Notes
    flush.mode = 'manual'

    cache.use_second_level_cache = true
    cache.use_query_cache = true
    cache.region.factory_class = 'net.sf.ehcache.hibernate.EhCacheRegionFactory'
}

environments {

    development {
        dataSource {
            driverClassName = "org.h2.Driver"
            username = "sa"
            password = ""
            dialect = "org.hibernate.dialect.H2Dialect"
            dbCreate = "create-drop" // one of 'create', 'create-drop', 'update', 'validate', ''
            url = "jdbc:h2:mem:devDb;MVCC=TRUE;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE"
        }

        hibernate {
            //format_sql = true
        }
    }

    test {
        dataSource {
            dbCreate = "create"
            username = "root"
            password = ""
            url = "jdbc:mysql://localhost/festival_test?useUnicode=yes&characterEncoding=UTF-8"
        }
    }

    production {
        dataSource {
            dbCreate = "update"
            url = "jdbc:mysql://localhost/festival?useUnicode=yes&characterEncoding=UTF-8"
        }
    }
}
