package realTimeEdt.realTime.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import realTimeEdt.realTime.model.DocRoom;
import realTimeEdt.realTime.model.User;

@Repository
public interface DocRoomRepository extends MongoRepository<DocRoom,String> {


    public DocRoom findByDocId(String docId);

    public User findByDocIdAndOwnerUser(String docId, User ownerUser);

    public boolean existsByDocId(String docId);
    public boolean existsByDocIdAndOwnerUser(String docId, User ownerUser);
}
