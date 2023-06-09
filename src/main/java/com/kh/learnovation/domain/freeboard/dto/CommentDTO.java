package com.kh.learnovation.domain.freeboard.dto;

import com.kh.learnovation.domain.freeboard.entity.CommentEntity;
import com.kh.learnovation.domain.freeboard.entity.FreeBoardEntity;
import com.kh.learnovation.domain.user.entity.User;
import lombok.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor // 기본생성자
@AllArgsConstructor // 모든 필드를 매개변수로 하는 생성자
public class CommentDTO {
    private Long id;
    private Long userId;
    private Long freeBoardId;
    private String commentContents;
    private Timestamp createdAt;


    @Builder
    public CommentDTO(long id, Long userId, Long freeBoardId, String commentContents, Timestamp createdAt){
        this.id = id;
        this.userId = userId;
        this.freeBoardId = freeBoardId;
        this.commentContents = commentContents;
        this.createdAt = createdAt;
    }



}