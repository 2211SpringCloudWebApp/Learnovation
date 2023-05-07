package com.kh.learnovation.domain.freeboard.entity;

import com.kh.learnovation.domain.freeboard.dto.CommentDTO;
import com.kh.learnovation.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "comment")
@NoArgsConstructor
public class CommentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    @Column
    private String commentContents;
    @Column
    private Timestamp commentCreatedTime;


    /* Board:Comment = 1:N */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "freeBoard_id")
    private FreeBoardEntity freeBoardEntity;

    //------------------------------------------ 여긴 실험 -----------------------------------

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "parent_id")
//    private CommentEntity parentCommentEntity;
//
//
//    @OneToMany(mappedBy = "parent", orphanRemoval = true)
//    private List<CommentEntity> childrenCommentEntity = new ArrayList<>();

    @Builder
    public CommentEntity(long id, User user, String commentContents, FreeBoardEntity freeBoardEntity, Timestamp commentCreatedTime){
        this.id = id;
        this.user = user;
        this.commentContents = commentContents;
        this.freeBoardEntity = freeBoardEntity;
        this.commentCreatedTime = commentCreatedTime;
    }

//    public static CommentEntity toSaveEntity(CommentDTO commentDTO, FreeBoardEntity freeBoardEntity) {
//        CommentEntity commentEntity = new CommentEntity();
//        commentEntity.setCommentWriter(commentDTO.getCommentWriter());
//        commentEntity.setCommentContents(commentDTO.getCommentContents());
//        commentEntity.setFreeBoardEntity(freeBoardEntity);
//        return commentEntity;
//    }
//
//    public static CommentEntity toUpdateEntity(CommentDTO commentDTO, FreeBoardEntity freeBoardEntity) {
//        CommentEntity commentEntity = new CommentEntity();
//        commentEntity.setCommentWriter(commentDTO.getCommentWriter());
//        commentEntity.setCommentContents(commentDTO.getCommentContents());
//        commentEntity.setFreeBoardEntity(freeBoardEntity);
//        return commentEntity;
//
//    }
//
//    public static CommentEntity toDeleteEntity(CommentDTO commentDTO, FreeBoardEntity freeBoardEntity) {
//        CommentEntity commentEntity = new CommentEntity();
//        commentEntity.setCommentWriter(commentDTO.getCommentWriter());
//        commentEntity.setCommentContents(commentDTO.getCommentContents());
//        commentEntity.setFreeBoardEntity(freeBoardEntity);
//        return commentEntity;
//
//    }
}
