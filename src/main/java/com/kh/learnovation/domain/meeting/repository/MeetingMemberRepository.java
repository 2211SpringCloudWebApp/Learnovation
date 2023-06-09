package com.kh.learnovation.domain.meeting.repository;

import com.kh.learnovation.domain.meeting.entity.MeetingMember;
import com.kh.learnovation.domain.meeting.entity.MeetingMemberPk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MeetingMemberRepository extends JpaRepository<MeetingMember, MeetingMemberPk> {


    @Query(nativeQuery = true, value = "SELECT COUNT(*) FROM meeting_member where MEETING_ID = :id")
    int countByMeeting(@Param("id") long groupNo);
    @Query(nativeQuery = true, value= "SELECT * FROM meeting_member where MEETING_ID = :id")
    List<MeetingMember> findByMeetingId(@Param("id") long meetingNo);
}
