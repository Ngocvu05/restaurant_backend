package com.management.chat_service.service.handler;

import com.management.chat_service.model.ChatMessage;
import com.management.chat_service.model.ChatRoom;
import com.management.chat_service.status.SenderType;

public interface IChatMessageHandler {
    boolean supports(SenderType senderType);
    ChatMessage handleMessage(ChatRoom chatRoom, String senderName, Long senderId, String content);
}
