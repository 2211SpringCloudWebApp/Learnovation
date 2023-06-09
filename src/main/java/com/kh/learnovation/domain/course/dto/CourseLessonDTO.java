package com.kh.learnovation.domain.course.dto;

import lombok.*;

import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CourseLessonDTO {
    private Long id;
    private Long chapterId;
    private String title;
    private int lessonOrder;
    private String[][] lesson; // form에서 넘어오는 강의 목록을 저장하기 위한 2차원 배열
    private CourseVideoDTO courseVideoDTO; // 강의와 동영상은 1:1 관계이므로 List<CoursevideoDTO>로 선언할 필요가 없음

    @Builder
    public CourseLessonDTO(Long id, Long chapterId, String title, int lessonOrder, CourseVideoDTO courseVideoDTO){
        this.id = id;
        this.chapterId = chapterId;
        this.title = title;
        this.lessonOrder = lessonOrder;
        this.courseVideoDTO = courseVideoDTO;
    }
}
