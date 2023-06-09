package com.kh.learnovation.domain.course.repository;

import com.kh.learnovation.domain.course.entity.CourseReview;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<CourseReview, Long> {
    Optional<CourseReview> findByUserIdAndCourseId(Long userId, Long courseId);

    int deleteByIdAndUserId(Long id, Long userId);

    Optional<CourseReview> findByIdAndUserId(Long id, Long userId);

    /** 페이징 */
    Slice<CourseReview> findByCourseId(Long courseId, PageRequest pageRequest);
}
