package main;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

//a handler for the hikari connection pool
public class DatabaseConnectionPool {


    //initialize the hikari connection pool

        private static HikariConfig config = new HikariConfig();
        private static HikariDataSource ds;

        static {
            config.setJdbcUrl( "jdbc:sqlite:src/db/chame.db" );
            config.setDriverClassName("org.sqlite.JDBC");
            config.addDataSourceProperty( "cachePrepStmts" , "true" );
            config.addDataSourceProperty( "prepStmtCacheSize" , "250" );
            config.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
            config.setMaxLifetime(60000);
            config.setMaximumPoolSize(50);
            ds = new HikariDataSource( config );
        }

        private DatabaseConnectionPool() {}

        //return a connection from the pool
        public static Connection getConnection() throws SQLException {
            return ds.getConnection();
        }

}
