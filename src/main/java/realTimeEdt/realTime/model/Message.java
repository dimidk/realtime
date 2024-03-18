package realTimeEdt.realTime.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "Message")
public class Message {


    @Id
    String id;
    String text;
    String senderId;
    String docId;
    TypeMessage type;

}
