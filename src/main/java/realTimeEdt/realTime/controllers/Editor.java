package realTimeEdt.realTime.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;
import realTimeEdt.realTime.model.Message;
import realTimeEdt.realTime.model.Role;
import realTimeEdt.realTime.service.EditorService;
import realTimeEdt.realTime.service.ValidationDocUser;

@Controller
@AllArgsConstructor
@Slf4j
public class Editor {


    private final EditorService editorService;
    private final ValidationDocUser validationDocUser;
    protected static String textArea = null;
    private static int id = 0;


    @MessageMapping("/controllers.sendMessage")
    @SendTo("/topic/public")
    public Message sendMessage(@Payload Message message) {

        log.info("This is message {}",message.getText());

        Editor.textArea = message.getText();

        log.info("this is textArea content {}",textArea);

        editorService.saveDocToDb(message.getDocId(),Editor.textArea);
        return message;
    }

    @MessageMapping("/controllers.subscribeToView")
    public void viewOnlyMessage(@Payload Message message) {

        log.info("This is a subscription only user {}",message.toString());

        editorService.userLoginUpdate(message, Role.VIEW);
        editorService.checkDocUser(message);
        Editor.textArea = editorService.getContentDocId(message.getDocId());


    }

    private void mainEditorUsage(Message message) {

        Editor.id += 1;

        if (textArea == null && editorService.findAllDocs().isEmpty()) {
            Editor.textArea = message.getText();
        }

    }

    @MessageMapping("/controllers.signInRoomUser")
    @SendTo("/topic/public")
    public Message signInDocUser(@Payload Message message, @RequestParam String username) {

        mainEditorUsage(message);

//        if (validationDocUser.docExist(message) && !validationDocUser.userExist(message)) {
        if (validationDocUser.docExist(message) && !validationDocUser.userExist(message)) {

            log.info("there is no such a user for this document");

        }

        if (validationDocUser.docExist(message) && validationDocUser.userExist(message)) {

            if (editorService.checkDocUser(message)) {

                Editor.textArea = editorService.getContentDocId(message.getDocId());
                message.setText(Editor.textArea);
                editorService.userLoginUpdate(message,Role.EDIT);
                editorService.saveDocToDb(message.getDocId(),message.getText());

                log.info("this is first time of user logged textArea {}",textArea);

            }
            else {
                log.info("cannot access document");
            }

        }

        return message;
    }

    @MessageMapping("/controllers.docRoomUser")
    @SendTo("/topic/public")
    public Message logUserToDoc(@Payload Message message, @RequestParam String username) {

        mainEditorUsage(message);

        log.info("first message {}",message.getText());
        log.info("user logged in {}",username);

        log.info("check if user exits");

        if (!validationDocUser.docExist(message)) {

            editorService.userLoginUpdate(message,Role.EDIT);
            editorService.createDoc(message);
        }

        return message;
    }

    @MessageMapping("controllers.onDisconnect")
    @SendTo("/topic/public")
    public Message onDisconnect(@Payload Message message, StompHeaderAccessor headerAccessor) {


        editorService.userDisconnectUpdate(message);

        if (headerAccessor.getCommand().equals(StompCommand.CONNECT )) {

            headerAccessor.setMessageTypeIfNotSet(SimpMessageType.DISCONNECT);
        }

        return message;
    }
}
