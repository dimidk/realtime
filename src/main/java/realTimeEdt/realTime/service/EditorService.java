package realTimeEdt.realTime.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import realTimeEdt.realTime.model.*;

//import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class EditorService {

    private final DocRoomService docRoomService;
    private final UserService userService;
    private final SendMailService sendMailService;

    public void saveDocToDb(String docId,String context) {

        DocRoom docRoom;

        if (docId == null) {
            throw new IllegalArgumentException("no name");
        }
        log.info("saveDocToDb: get docId {}",docId);

        if (docRoomService.exist(docId)) {
            docRoom = docRoomService.getDocRoom(docId);
        }

        docRoomService.updateDocument(docId,context);

    }

    public void createDoc(Message message) {

        DocRoom docRoom = null;
        List<User> shared = new ArrayList<>();
        List<Message> messages = new ArrayList<>();

        if (message == null) {
            throw new IllegalArgumentException("no message");
        }

        String docId = message.getDocId();
        log.info("createDoc method:{}",docId);
        if (!docRoomService.exist(docId)) {
            log.info("createDoc method: new Doc");
            docRoom = DocRoom.builder()
                    .docId(message.getDocId())
                    .ownerUser(userService.findUser(message.getSenderId()))
                    .shareUser(shared)
                    .content(message.getText())
                    .messages(messages)
                    .build();

            docRoomService.addDoc(docRoom);
        }
        log.info("createDoc method: existing doc");
    }

    private boolean findOwner(String docId,String name) {

        if (docId == null) {
            throw new IllegalArgumentException("no name");
        }
        if (name == null) {
            throw new IllegalArgumentException("no given status");
        }

        DocRoom docRoom = docRoomService.getDocRoom(docId);
        log.info("findOwner method: {}",docRoom.getOwnerUser().getName());
        return docRoom.getOwnerUser().getName().compareTo(name) == 0;
    }

    public String getContentDocId(String docId) {

        if (docId == null) {
            throw new IllegalArgumentException("no name");
        }

        return docRoomService.getContentDocId(docId);
    }

    public List<DocRoom> findAllDocs() {

        return docRoomService.findAllDocs();
    }


    public Optional<User> getShareUser(String docId, String userId) {
        User user = null;
        List<User> sharedUsers = null;

        if (docId == null) {
            throw new IllegalArgumentException("no name");
        }
        if (userId == null) {
            throw new IllegalArgumentException("no given status");
        }

        Optional<User>  u = docRoomService.findShareUser(docId,userId);

        return u;

    }

    public boolean checkDocUser(Message message) {

        Optional<User> userFind = null;
        boolean access = true;

        if (message == null) {
            throw new IllegalArgumentException("noe message");
        }

        DocRoom docRoom = docRoomService.getDocRoom(message.getDocId());
        User user = userService.findUser(message.getSenderId());
        userFind = docRoomService.findShareUser(message.getDocId(), message.getSenderId());
        if (!docRoomService.findOwnerDoc(message.getDocId(), user) && userFind.isEmpty()) {

            log.info("don't have privilege to access document");
            access = false;
        }

        return access;
    }

    private void addNewUser(User name) {

        if (name == null) {
            throw new IllegalArgumentException("null object");
        }

        try {
            if (!userService.exists(name)) {

                userService.addNew(name);
            }
        }catch (Exception e) {
            log.info("cannot add same user: {}",e.getMessage());
        }
    }



    public Optional<User> addShareUserInDoc(String docId, String owner, User userToAdd) {

        User user= null;

        DocRoom docRoom = docRoomService.getDocRoom(docId);

        if (owner.compareTo(docRoom.getOwnerUser().getName()) == 0) {

            log.info("addShareUserInDoc method: owner document {} and user session is {} ",docRoom.getOwnerUser().getName(),owner);

            if (docRoomService.findShareUser(docId,userToAdd.getName()).isEmpty()) {
                log.info("addShareUserInDoc method: either list empty or user not exist in list {}", userToAdd.getName());
                docRoomService.updateShareUserList(docId,userToAdd);
                addNewUser(userToAdd);
                user = userToAdd;

                sendMailService.sendNewMail(user.getName(), "share in RealTime Editor", "this is a message for sharing" +
                        " collaboration a document! You are invited to collaborate having an/a "
                        + user.getRole().toString() + " privileges");

            }
        }
        else {
            log.info("You don't have right to add user to the document");
        }

        return Optional.ofNullable(user);

    }


    public void userLoginUpdate(Message message, Role roleType) {

        if (message == null) {
            throw new IllegalArgumentException("noe message");
        }

        try {

            if (!userService.existsByName(message.getSenderId())) {
                //if (!userService.exists(loginUser)) {
                log.info("login user doesn't exist");
                User loginUser = User.builder()
                        .name(message.getSenderId())
                        .role(roleType)
                        .status(message.getType())
                        .build();

                userService.addNew(loginUser);
            }
            userService.updateUser(message.getSenderId(),message.getType());
        } catch (Exception e) {
            log.info("error in User id {}",e.getMessage());
            //throw new RuntimeException(e);
        }

    }

    public void userDisconnectUpdate(Message message) {

        if (message == null) {
            throw new IllegalArgumentException("noe message");
        }

        if (message.getType() == TypeMessage.LEAVE) {

            User user = userService.findUser(message.getSenderId());
            log.info("user in db {}",user.getName());
            log.info("userDisconnectUpdate: {}",message.getSenderId());
            log.info("userDisconnectUpdate: {}",docRoomService.findViewUser(message.getDocId(), user).isEmpty());
            if (!docRoomService.findViewUser(message.getDocId(), user).isEmpty()) {

                log.info("userDisconnectUpdate: before msg{}",message.getText());

                message.setText(docRoomService.getContentDocId(message.getDocId()));

                log.info("userDisconnectUpdate: after msg {}",message.getText());
            }
            userService.updateUser(message.getSenderId(), TypeMessage.LEAVE);
        }
    }

}
