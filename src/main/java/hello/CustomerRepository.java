package hello;

import java.util.List;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;

public interface CustomerRepository extends CassandraRepository<Customer> {

    @Override
    List<Customer> findAll();

    @Query("SELECT * FROM Customer WHERE lastName = ?0 ALLOW FILTERING")
    List<Customer> findByLastName(String lastName);
    
}
