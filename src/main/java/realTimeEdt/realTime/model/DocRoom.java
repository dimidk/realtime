package realTimeEdt.realTime.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import realTimeEdt.realTime.actions.EditActions;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "Doc")
public class DocRoom implements EditActions {


    @Id
    String docId;

    @JsonProperty
    User ownerUser;

    @JsonProperty
    List<User> shareUser;

    @JsonProperty
    String content;

    @JsonProperty
    List<Message> messages;

    @JsonCreator
    public static DocRoom of (@JsonProperty("docId") String docId,
                              @JsonProperty("ownerUser") String ownerUser,
                              @JsonProperty("shareUser") List<User> shareUser,
                              @JsonProperty("content") String content) {

        DocRoom docRoom = new DocRoom();
        docRoom.docId = docId;
        docRoom.content = content;

        User user = new User();
        user.setName(ownerUser);
        docRoom.ownerUser = user;

        shareUser = new ArrayList<>();
        docRoom.shareUser = shareUser;

        return docRoom;
    }

    @Override
    public void load(DocRoom textDoc) {

    }

    @Override
    public void save(DocRoom textDoc) {

    }
}
