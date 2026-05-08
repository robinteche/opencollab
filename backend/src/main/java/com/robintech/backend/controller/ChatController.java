package com.robintech.backend.controller;

import com.robintech.backend.dto.ChatMessage;
import com.robintech.backend.dto.MessageResponse;
import com.robintech.backend.model.Message;
import com.robintech.backend.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;


    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        Message saved = messageService.saveMessage(
                chatMessage.getSenderId(),
                chatMessage.getReceiverId(),
                chatMessage.getContent()
        );

        chatMessage.setSentAt(saved.getSentAt().toString());
        chatMessage.setType(ChatMessage.MessageType.CHAT);


        messagingTemplate.convertAndSendToUser(
                chatMessage.getReceiverId().toString(),
                "/queue/messages",
                chatMessage
        );


        messagingTemplate.convertAndSendToUser(
                chatMessage.getSenderId().toString(),
                "/queue/messages",
                chatMessage
        );
    }


    @GetMapping("/api/messages/{otherUserId}")
    public List<MessageResponse> getConversation(@PathVariable Long otherUserId) {
        return messageService.getConversation(otherUserId);
    }


    @GetMapping("/api/messages/unread")
    public List<MessageResponse> getUnread() {
        return messageService.getUnreadMessages();
    }
}
