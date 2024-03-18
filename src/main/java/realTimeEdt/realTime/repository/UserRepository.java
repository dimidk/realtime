package realTimeEdt.realTime.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import realTimeEdt.realTime.model.TypeMessage;
import realTimeEdt.realTime.model.User;

import java.util.List;

@Repository
public interface UserRepository extends MongoRepository<User,String> {


     User findUserByName(String name);

     boolean existsByName(String name);

    @Query(value = "{'status': 'JOIN'}")
    public List<User> findAllByStatus(TypeMessage status);
}
