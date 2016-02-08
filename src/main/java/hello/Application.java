package hello;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cassandra.core.keyspace.CreateIndexSpecification;
import org.springframework.cassandra.core.keyspace.CreateTableSpecification;
import org.springframework.context.annotation.Bean;
import org.springframework.data.cassandra.config.CassandraSessionFactoryBean;
import org.springframework.data.cassandra.config.java.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;
import org.springframework.data.cassandra.repository.support.BasicMapId;

@SpringBootApplication
@EnableCassandraRepositories(basePackageClasses = Application.class)
public class Application extends AbstractCassandraConfiguration {
    
    private static final Logger log = LoggerFactory.getLogger(Application.class);
    
    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
    
    @Override
    protected String getKeyspaceName() {
        return "mykeyspace";
    }
    
    @Bean
    public CommandLineRunner loadData(
            CassandraOperations cassandraTemplate,
            CassandraSessionFactoryBean session,
            CustomerRepository repository) {
        return (args) -> {

            // This prepares the database for the demo and is used to showcase 
            // Spring and raw driver level API
            // If you need to get to native Datastacks driver level:
            Session nativeSessionObject = session.getObject();
            
            try {
                nativeSessionObject.execute("DROP TABLE Customer");
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            // Could use manual cql query as well, but Spring can help a bit.
            // We could also could let Spring autogenerate the table
            cassandraTemplate.execute(CreateTableSpecification.createTable()
                    .name("Customer")
                    .partitionKeyColumn("id", DataType.text())
                    .column("lastName", DataType.text())
                    .column("firstName", DataType.text())
            );

            // Create index to allow searching based on lastName
            cassandraTemplate.execute(CreateIndexSpecification.createIndex()
                    .tableName("Customer")
                    .columnName("lastName"));

            // For testing/development, prepare the database
            final Customer jack = new Customer("Jack", "Bauer");

            // save a customer using template
            cassandraTemplate.insert(jack);

            // Two in batch
            cassandraTemplate.insert(Arrays.asList(new Customer("Chloe",
                    "O'Brian"), new Customer("Kim", "Bauer")));

            // with raw CQL, straight to low level session:
            nativeSessionObject.execute(
                    "INSERT INTO Customer (id,  firstName, lastName) VALUES ('matti', 'Matti', 'Tahvonen')");
            
            ResultSet result = nativeSessionObject.execute(
                    "SELECT * from Customer WHERE id = 'matti'");
            Row row = result.one();
            log.info(
                    "Found one result with SELECT * from Customer WHERE id = 'matti' :");
            log.info("-------------------------------");
            log.info(row.getString("firstName") + " " + row.
                    getString("lastName"));

            // with CQL template
            cassandraTemplate.insert(new Customer("David", "Palmer"));

            // or with repository, the most easiest option
            repository.save(new Customer("Michelle", "Dessler"));

            // fetch all customers
            log.info("Customers found with findAll():");
            log.info("-------------------------------");
            for (Customer customer : repository.findAll()) {
                log.info(customer.toString());
            }
            log.info("");

            // fetch an individual customer by ID
            Customer customer = repository.findOne(BasicMapId.id("id", jack.
                    getId()));
            log.info("Customer found Jack with ID " + jack.getId() + ":");
            log.info("--------------------------------");
            log.info(customer.toString());
            log.info("");

            // fetch customers by last name
            log.info(
                    "Customer found with findByLastName('Bauer'):");
            log.info("--------------------------------------------");
            for (Customer bauer : repository
                    .findByLastName("Bauer")) {
                log.info(bauer.toString());
            }
            log.info("");
        };
    }
    
}
