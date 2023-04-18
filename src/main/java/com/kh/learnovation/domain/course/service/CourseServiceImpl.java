package com.kh.learnovation.domain.course.service;

import com.kh.learnovation.domain.course.dto.CourseDTO;
import com.kh.learnovation.domain.course.dto.CourseLessonDTO;
import com.kh.learnovation.domain.course.dto.CourseVideoDTO;
import com.kh.learnovation.domain.course.entity.*;
import com.kh.learnovation.domain.course.repository.*;
import com.kh.learnovation.domain.user.entity.User;
import com.kh.learnovation.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class CourseServiceImpl implements CourseService {
    private final ResourceLoader resourceLoader;
    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final ChapterRepository chapterRepository;
    private final LessonRepository lessonRepository;
    private final VideoRepository videoRepository;
    private final UserRepository userRepository;

    @Autowired
    public CourseServiceImpl(ResourceLoader resourceLoader, CourseRepository courseRepository,
                             CategoryRepository categoryRepository, ChapterRepository chapterRepository,
                             LessonRepository lessonRepository, VideoRepository videoRepository,
                             UserRepository userRepository) {
        this.resourceLoader = resourceLoader;
        this.courseRepository = courseRepository;
        this.categoryRepository = categoryRepository;
        this.chapterRepository = chapterRepository;
        this.lessonRepository = lessonRepository;
        this.videoRepository = videoRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public String createCourse(CourseDTO courseDto,
                               String[] chapters,
                               CourseLessonDTO courseLessonDto,
                               MultipartFile[] videoFiles
    ) throws IOException {
        ;
        /** 글 처리 (카테고리정보, 회원정보, 글제목, 난이도, 가격, 글내용, 생성일, 수정일, 삭제여부, 삭제일) */
//        // 1. DTO -> Entity 변환
//        Course toSaveCourse = courseDto.toEntity();
//        // 2. DTO에 있는 카테고리명을 통해 CourseCategory 객체 불러오기 및 Course Entity에 저장
//        CourseCategory courseCategory = categoryRepository.findByName(courseDto.getCategory());
//        toSaveCourse.setCourseCategory(courseCategory);
//        // 3. DTO에 있는 이메일을 통해 User 객체 불러오기 및 Entity에 저장
//        User user = userRepository.findByEmail(courseDto.getEmail());
//        toSaveCourse.setUser(user);
//        // 4. 후처리 완료된 Entity를 DB에 저장
//        Course savedCourse = courseRepository.save(toSaveCourse);

        // 1. DTO에 있는 카테고리명을 통해 CourseCategory 객체 불러오기
        CourseCategory foundCategory = categoryRepository.findByName(courseDto.getCategory());
        // 2. DTO에 있는 이메일을 통해 User 객체 불러오기
        User foundUser = userRepository.findByEmail(courseDto.getEmail());
        // 3. DTO -> Entity 변환
        Course toSaveCourse = Course.builder()
                .courseCategory(foundCategory)
                .user(foundUser)
                .title(courseDto.getTitle())
                .level(courseDto.getLevel())
                .price(courseDto.getPrice())
                .content(courseDto.getContent())
                .build();
        // 4. 변환된 Entity를 통해 DB 저장
        Course savedCourse = courseRepository.save(toSaveCourse);

        /** 목차 목록 처리 (게시물과 1:N 관계) */
        // 1. 목차 목록을 담을 List 생성
        List<CourseChapter> toSaveChapters = new ArrayList<>();
        // 2. 전달된 목차만큼 for문 반복하여 chapter 객체에 저장후, List에 저장
        for (int i = 0; i < chapters.length; i++) {
//            CourseChapter chapter = new CourseChapter();
//            chapter.setCourse(savedCourse);
//            chapter.setTitle(chapters[i]);
//            chapter.setChapterOrder(i);
//            toSaveChapters.add(chapter);
            CourseChapter toSaveChapter = CourseChapter.builder()
                    .course(savedCourse)
                    .title(chapters[i])
                    .chapterOrder(i)
                    .build();
            toSaveChapters.add(toSaveChapter);
        }
        // 3. 목차들이 저장된 목록 Entity를 DB에 저장
        List<CourseChapter> savedChapters = chapterRepository.saveAll(toSaveChapters);

        /** 강의 목록 처리 (목차와 1:N 관계) */
        // 2차원배열로 표현하여, 행(앞의 중괄호) 값을 목차로 표현
        List<CourseLesson> toSaveLessons = new ArrayList<>();
        String[][] lessons = courseLessonDto.getLesson();
        for (int i = 0; i < lessons.length; i++) {
            for (int j = 0; j < lessons[i].length; j++) {
//                CourseLesson lesson = new CourseLesson();
//                lesson.setCourseChapter(savedChapters.get(i));
//                lesson.setTitle(lessons[i][j]);
//                lesson.setLessonOrder(j);
//                toSaveLessons.add(lesson);
                CourseLesson toSaveLesson = CourseLesson.builder()
                        .courseChapter(savedChapters.get(i))
                        .title(lessons[i][j])
                        .lessonOrder(j)
                        .build();
                toSaveLessons.add(toSaveLesson);
            }
        }
        List<CourseLesson> savedLessons = lessonRepository.saveAll(toSaveLessons);

        /** 동영상 목록 처리 - (강의와 1:1 관계) */
        // 1. 동영상 서버에 저장
        List<CourseVideo> uploadedVideos = createVideos(videoFiles, savedLessons);
        List<CourseVideo> savedVideos = videoRepository.saveAll(uploadedVideos);

        return null;
    }

    @Override
    public List<CourseVideo> createVideos(MultipartFile[] videoFiles, List<CourseLesson> savedLessons
    ) throws IOException {
        // 1. 파일 저장할 디렉터리 경로 구하기
        Resource resource = resourceLoader.getResource("file:src/main/resources/static/course/upload");
        String realPath = resource.getFile().getAbsolutePath();
        // 2. 해당 경로에 날짜별로 디렉터리 생성
        String today = new SimpleDateFormat("yyMMdd").format(new Date()); //230417
        String savedFolder = realPath + File.separator + today; // File.separator는 파일시스템 구분자. 윈도우(\), 유닉스계열(/)
        System.out.println("저장될 폴더 경로: " + savedFolder);
        File folder = new File(savedFolder);
        if (!folder.exists())
            folder.mkdirs();
        // 3. 업로드 되는 파일 목록 저장
        List<CourseVideo> toSaveVideos = new ArrayList<CourseVideo>();
        int k = 0;
        for (MultipartFile videoFile : videoFiles) {
            CourseVideo toSaveVideo = new CourseVideo();
            String originalVideoName = videoFile.getOriginalFilename(); // 원본 파일명이 있으면 업로드된 파일이 남아있다는 것
            if (!originalVideoName.isEmpty()) {
                String savedVideoName = UUID.randomUUID() + originalVideoName.substring(originalVideoName.lastIndexOf('.'));
                toSaveVideo.setCourseLesson(savedLessons.get(k));
                toSaveVideo.setOriginalVideoName(originalVideoName);
                toSaveVideo.setSavedVideoName(savedVideoName);
                toSaveVideo.setSavedPath(today);
//                toSaveVideo.setSavedPath(savedFolder);
                toSaveVideo.setVideoSize(videoFile.getSize());
                videoFile.transferTo(new File(folder, savedVideoName));
                k++;
            }
            toSaveVideos.add(toSaveVideo);
        }
        return toSaveVideos;
    }
}
