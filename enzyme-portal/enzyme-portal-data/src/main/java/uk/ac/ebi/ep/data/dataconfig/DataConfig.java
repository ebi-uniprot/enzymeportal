/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.ep.data.dataconfig;

import java.util.Properties;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.hibernate4.HibernateExceptionTranslator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import uk.ac.ebi.ep.data.service.BioPortalService;
import uk.ac.ebi.ep.data.service.DiseaseParser;
import uk.ac.ebi.ep.data.service.DiseaseService;
import uk.ac.ebi.ep.data.service.EnzymePortalCompoundService;
import uk.ac.ebi.ep.data.service.UniprotEntryService;

/**
 *
 * @author joseph
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories("uk.ac.ebi.ep.data.repositories")
@PropertySource({"classpath:ep-web-client.properties", "classpath:chembl-adapter.properties", "classpath:log4j.properties"})

public class DataConfig {

    @Autowired
    private DataSource dataSource;

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        //em.setPersistenceXmlLocation("classpath*:META-INF/persistence.xml");

        //em.setPersistenceUnitName("ep_PU");
        em.setDataSource(dataSource);
        em.setPackagesToScan("uk.ac.ebi.ep.data.domain");

        Properties properties = new Properties();
        properties.setProperty("hibernate.cache.provider_class", "org.hibernate.cache.NoCacheProvider");
        properties.setProperty("hibernate.connection.driver_class", "oracle.jdbc.OracleDriver");

        HibernateJpaVendorAdapter vendor = new HibernateJpaVendorAdapter();
        vendor.setShowSql(false);
        vendor.setDatabase(Database.ORACLE);
        em.setJpaProperties(properties);
        em.setJpaVendorAdapter(vendor);

        return em;
    }

//    @Bean
//    public JpaTransactionManager transactionManager() {
//        JpaTransactionManager transactionManager = new JpaTransactionManager();
//        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
//        return transactionManager;
//    }
    @Bean
    public PlatformTransactionManager transactionManager() {
        PlatformTransactionManager manager = new JpaTransactionManager(entityManagerFactory().getObject());
        return manager;
    }

    @Bean
    public HibernateExceptionTranslator hibernateExceptionTranslator() {
        return new HibernateExceptionTranslator();
    }

    @Bean
    public DiseaseService diseaseService() {
        return new DiseaseService();
    }

    @Bean
    public DiseaseParser diseaseParser() {
        return new DiseaseParser();
    }

    @Bean
    public BioPortalService bioPortalService() {
        return new BioPortalService();
    }

    @Bean
    public UniprotEntryService uniprotEntryService() {
        return new UniprotEntryService();
    }

    @Bean
    public EnzymePortalCompoundService enzymePortalCompoundService() {
        return new EnzymePortalCompoundService();
    }
}
