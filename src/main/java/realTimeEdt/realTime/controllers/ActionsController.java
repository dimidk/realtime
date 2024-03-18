package realTimeEdt.realTime.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import realTimeEdt.realTime.model.DocRoom;
import realTimeEdt.realTime.model.User;
import realTimeEdt.realTime.service.EditorService;
import realTimeEdt.realTime.service.UserService;
import realTimeEdt.realTime.service.ValidationDocUser;

//import javax.mail.MessagingException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/*Testing for transfer

 */

@Controller
@AllArgsConstructor
@Slf4j
public class ActionsController {

    private UserService userService;
    private EditorService editorService;
    private ValidationDocUser validationDocUser;

    @GetMapping("/save/{docId}/{userId}")
    public ResponseEntity<String> saveDoc(@PathVariable String docId, @PathVariable String userId) throws IOException {

        DocRoom docRoom = null;

        String temp = Editor.textArea;
        log.info("this is content from client {}",temp);

        editorService.saveDocToDb(docId,Editor.textArea);

        if (validationDocUser.userExist(userId)) {

            log.info("actionController method: user {}",userId);
            FileWriter fpWrite = new FileWriter(userId + "_" + docId + ".txt");
            fpWrite.write(temp);
            fpWrite.flush();
            fpWrite.close();

            return ResponseEntity.ok("200");
        }
        else {
            log.info("cannot save file to no user");
            return ResponseEntity.ok("-200");
        }

    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> findConnectedUsers() {

        List<User> con = userService.findAllConnectedUsers();
        con.forEach(user -> log.info("findConnectedUsers: {}",user.getName()));

        return ResponseEntity.ok(userService.findAllConnectedUsers());
    }

    @PostMapping("/editor/addShareUser/{docId}/{owner}")

    public ResponseEntity<Optional<User>> addShareUser(@PathVariable String docId, @PathVariable String owner,
                                                       @RequestBody User addShareUserInDoc) {


        Optional<User> user = editorService.addShareUserInDoc(docId,owner,addShareUserInDoc);
        return ResponseEntity.of(Optional.ofNullable(user));
    }
}
