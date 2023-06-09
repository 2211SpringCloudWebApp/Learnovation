package com.kh.learnovation.domain.notice.repository;

import com.kh.learnovation.domain.notice.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    Page<Notice> findAllByStatus(int status, Pageable pageable);

    Page<Notice> findAllByStatusAndTitleLike(int status, String title, Pageable pageable);
}
