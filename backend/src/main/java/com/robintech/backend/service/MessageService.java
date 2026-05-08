package com.robintech.backend.service;

import com.robintech.backend.dto.MessageResponse;
import com.robintech.backend.model.Message;
import com.robintech.backend.model.User;
import com.robintech.backend.repository.MessageRepository;
import com.robintech.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public Message saveMessage(Long senderId, Long receiverId, String content) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .build();
        return messageRepository.save(message);
    }

    public List<MessageResponse> getConversation(Long otherUserId) {
        User currentUser = getCurrentUser();
        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Message> messages = messageRepository.findConversation(currentUser, otherUser);

        // Mark messages as read
        messages.stream()
                .filter(m -> m.getReceiver().getId().equals(currentUser.getId()) && !m.isRead())
                .forEach(m -> {
                    m.setRead(true);
                    messageRepository.save(m);
                });

        return messages.stream()
                .map(MessageResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<MessageResponse> getUnreadMessages() {
        User currentUser = getCurrentUser();
        return messageRepository.findUnreadMessages(currentUser)
                .stream()
                .map(MessageResponse::fromEntity)
                .collect(Collectors.toList());
    }
}