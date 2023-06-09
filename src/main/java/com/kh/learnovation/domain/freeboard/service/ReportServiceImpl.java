package com.kh.learnovation.domain.freeboard.service;


import com.kh.learnovation.domain.freeboard.dto.ReportDTO;
import com.kh.learnovation.domain.freeboard.entity.FreeBoardEntity;
import com.kh.learnovation.domain.freeboard.entity.ReportEntity;
import com.kh.learnovation.domain.freeboard.repository.FreeBoardRepository;
import com.kh.learnovation.domain.freeboard.repository.ReportRepository;
import com.kh.learnovation.domain.user.entity.User;
import com.kh.learnovation.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl  implements  ReportService{
    private final FreeBoardRepository freeBoardRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;

    @Override
    public Long insertReport(ReportDTO reportDTO) {
        User user = userRepository.findById(reportDTO.getUserId()).get();
        FreeBoardEntity freeBoardEntity = freeBoardRepository.findById(reportDTO.getFreeBoardId()).get();
        ReportEntity reportEntity = ReportEntity.builder()
                .user(user)
                .freeBoardEntity(freeBoardEntity).reportReason(reportDTO.getReportReason())
                .reportContent(reportDTO.getReportContent()).build();
        reportEntity =reportRepository.save(reportEntity);
        reportDTO = ReportDTO.builder()
                .id(reportEntity.getId())
                .userId(user.getId())
                .name(reportEntity.getUser().getName())
                .freeBoardId(reportEntity.getFreeBoardEntity().getId())
                .reportReason(reportEntity.getReportReason())
                .reportContent(reportEntity.getReportContent())
                .build();
        return  reportDTO.getId();
    }
}
