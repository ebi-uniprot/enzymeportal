package uk.ac.ebi.ep.data.dataconfig;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 *
 * @author joseph
 */
@Configuration
@Prod
@PropertySource({"classpath:ep-db-uzprel.properties"})
public class ProdDataConfig implements EnzymePortalDataConfig {

  
    @Autowired
    private Environment env;

    @Bean
    @Override
    public DataSource dataSource() {
        String url = String.format("jdbc:oracle:thin:@%s:%s:%s",
                env.getRequiredProperty("ep.db.host"), env.getRequiredProperty("ep.db.port"), env.getRequiredProperty("ep.db.instance"));

        String username = env.getRequiredProperty("ep.db.username");
        String password = env.getRequiredProperty("ep.db.password");
        DriverManagerDataSource ds = new DriverManagerDataSource(url, username, password);

        ds.setDriverClassName("oracle.jdbc.OracleDriver");
  

        return ds;
    }
    
}
