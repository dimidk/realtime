package realTimeEdt.realTime.config;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import realTimeEdt.realTime.service.UserService;

@Slf4j
@RequiredArgsConstructor
@Component
public class WebSocketEventListener {

    SimpMessageSendingOperations messageSendingOperations;
    SimpMessagingTemplate messagingTemplate;

    UserService userService;

    @EventListener
    public void handleWebSocketConnectionListener(SessionConnectedEvent event) {

        log.info("connect event triggered {}",event.toString());

        /*String userCon = event.getUser().getName();
        log.info("user from connection {}",userCon);*/

        //messagingTemplate.getHeaderInitializer();
        //MessageChannel msgCh = messagingTemplate.getMessageChannel();

        //Principal userCon = event.getUser();
        //Message msg = (Message) event.getMessage();


    }

    @EventListener
    public void handleSessionSubscribeEvent(SessionSubscribeEvent event) {

        log.info("subscribe session {}",event.toString());

        StompHeaderAccessor stompHeaderAccessor = StompHeaderAccessor.wrap(event.getMessage());

        boolean b = stompHeaderAccessor.getCommand().equals(StompCommand.SUBSCRIBE);
        if (b) {
            log.info("get subscribe message");

        }

    }

    @EventListener
    public void handleWebSocketDisconnectionListener(SessionDisconnectEvent event) {

        String session = event.getSessionId();

        CloseStatus status = event.getCloseStatus();

        log.info("try to get message info from event {}",event.getMessage().getPayload().toString());

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        if (headerAccessor.getCommand() == StompCommand.DISCONNECT || headerAccessor.getCommand() == StompCommand.ABORT) {

            String username = (String) headerAccessor.getSessionAttributes().get("user");
            log.info("user disconnected: {}",username);
/*
            Message discMessage = Message.builder().id("disc")
                    .type(TypeMessage.LEAVE)
                    .senderId(userService.)

            messageSendingOperations.convertAndSend("/topic/public",discMessage);*/
        }

    }
}
