package com.management.chat_service.service.implement;

import com.management.chat_service.dto.ChatMessageDTO;
import com.management.chat_service.mapper.IChatMessageMapper;
import com.management.chat_service.model.ChatMessage;
import com.management.chat_service.repository.ChatMessageRepository;
import com.management.chat_service.service.IChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements IChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final IChatMessageMapper chatMessageMapper;
    @Override
    public Page<ChatMessageDTO> getMessagesByRoomId(String roomId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<ChatMessage> pageResult = chatMessageRepository.findByChatRoom_RoomId(roomId, pageable);

        return pageResult.map(chatMessageMapper::toDTO);
    }

}
