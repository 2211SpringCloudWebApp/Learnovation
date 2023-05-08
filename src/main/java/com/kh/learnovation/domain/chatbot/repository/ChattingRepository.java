package com.kh.learnovation.domain.chatbot.repository;

import com.kh.learnovation.domain.chatbot.entity.Chatting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChattingRepository extends JpaRepository<Chatting, Long> {
    List<Chatting> findByRoomId(Long id);
}
