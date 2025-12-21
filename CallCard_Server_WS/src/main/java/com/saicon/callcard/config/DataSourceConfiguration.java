package com.saicon.callcard.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

/**
 * DataSource and JPA Configuration for CallCard microservice.
 *
 * Configures:
 * - HikariCP connection pool
 * - JPA EntityManagerFactory
 * - Transaction management
 * - Hibernate properties (dialect, caching, statistics)
 *
 * Configuration properties from application.properties:
 * - spring.datasource.* (url, username, password, driver)
 * - spring.jpa.* (hibernate properties)
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.saicon.games.entities")
public class DataSourceConfiguration {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${spring.datasource.driver-class-name}")
    private String dbDriverClassName;

    @Value("${spring.jpa.properties.hibernate.dialect}")
    private String hibernateDialect;

    @Value("${spring.jpa.show-sql:false}")
    private boolean showSql;

    @Value("${spring.jpa.properties.hibernate.format_sql:false}")
    private boolean formatSql;

    @Value("${spring.jpa.hibernate.ddl-auto:none}")
    private String ddlAuto;

    /**
     * HikariCP DataSource configuration
     */
    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbUrl);
        config.setUsername(dbUsername);
        config.setPassword(dbPassword);
        config.setDriverClassName(dbDriverClassName);

        // Connection pool settings
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000); // 30 seconds
        config.setIdleTimeout(600000); // 10 minutes
        config.setMaxLifetime(1800000); // 30 minutes
        config.setPoolName("CallCardHikariPool");

        // Performance settings
        config.setAutoCommit(false);
        config.setConnectionTestQuery("SELECT 1");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        return new HikariDataSource(config);
    }

    /**
     * JPA EntityManagerFactory configuration
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource());
        emf.setPackagesToScan(
            "com.saicon.games.entities",
            "com.saicon.games.salesorder.entities",
            "com.saicon.games.invoice.entities",
            "com.saicon.user.entities",
            "com.saicon.application.entities",
            "com.saicon.addressbook.entities",
            "com.saicon.generic.entities"
        );
        emf.setPersistenceUnitName("callcard-pu");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setShowSql(showSql);
        vendorAdapter.setGenerateDdl(false);
        emf.setJpaVendorAdapter(vendorAdapter);

        // Hibernate properties
        Properties jpaProperties = new Properties();
        jpaProperties.put("hibernate.dialect", hibernateDialect);
        jpaProperties.put("hibernate.hbm2ddl.auto", ddlAuto);
        jpaProperties.put("hibernate.show_sql", showSql);
        jpaProperties.put("hibernate.format_sql", formatSql);

        // Second-level cache configuration
        jpaProperties.put("hibernate.cache.use_second_level_cache", "true");
        jpaProperties.put("hibernate.cache.use_query_cache", "true");
        jpaProperties.put("hibernate.cache.region.factory_class", "org.hibernate.cache.jcache.JCacheRegionFactory");
        jpaProperties.put("hibernate.cache.use_structured_entries", "true");

        // Statistics
        jpaProperties.put("hibernate.generate_statistics", "true");

        // JDBC batch settings
        jpaProperties.put("hibernate.jdbc.batch_size", "20");
        jpaProperties.put("hibernate.order_inserts", "true");
        jpaProperties.put("hibernate.order_updates", "true");
        jpaProperties.put("hibernate.jdbc.batch_versioned_data", "true");

        emf.setJpaProperties(jpaProperties);

        return emf;
    }

    /**
     * Transaction manager configuration
     */
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }
}
