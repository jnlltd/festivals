package ie.festivals.hibernate;

import org.hibernate.dialect.MySQL5InnoDBDialect;

/**
 * Hibernate dialect that uses utf8mb4 as the default character set
 */
public class UTF8MySQL5InnoDBDialect extends MySQL5InnoDBDialect {

    @Override
    public String getTableTypeString() {
        return " ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci";
    }
}
