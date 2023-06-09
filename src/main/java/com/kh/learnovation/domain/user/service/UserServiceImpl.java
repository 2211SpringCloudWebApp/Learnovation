package com.kh.learnovation.domain.user.service;

import com.kh.learnovation.domain.user.dto.UserDTO;
import com.kh.learnovation.domain.user.entity.User;
import com.kh.learnovation.domain.user.model.RoleType;
import com.kh.learnovation.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private BCryptPasswordEncoder encoder;
    private User user;
    private final UserRepository userRepository;
    private static final int BLOCK_PAGE_NUM_COUNT = 5; // 블럭에 존재하는 페이지 수
    private static final int PAGE_POST_COUNT = 10; // 한 페이지에 존재하는 게시글 수

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public List<UserDTO> getUserList(Integer pageNum) {

        Page<User> page = userRepository
                .findAll(PageRequest.of(pageNum - 1, PAGE_POST_COUNT, Sort.by(Sort.Direction.ASC, "createdAt")));

//        List<User> users = userRepository.findAll();
        List<User> users = page.getContent();
        List<UserDTO> userDTOList = new ArrayList<>();

        for (User user : users) {
            UserDTO userDTO = UserDTO.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .password(user.getPassword())
                    .name(user.getName())
                    .nickname(user.getNickname())
                    .phoneNumber(user.getPhoneNumber())
                    .profileImage(user.getProfileImage())
                    .createdAt(user.getCreatedAt())
                    .updatedAt(user.getUpdatedAt())
                    .status(user.getStatus())
                    .deletedAt(user.getDeletedAt())
                    .build();

            userDTOList.add(userDTO);
        }
        return userDTOList;
    }

    @Override
    @Transactional
    public Integer[] getPageList(Integer curPageNum) {
        Integer[] pageList = new Integer[BLOCK_PAGE_NUM_COUNT];

        Double usersTotalCount = Double.valueOf(this.getUserCount());

        Integer totalLastPageNum = (int) (Math.ceil((usersTotalCount / PAGE_POST_COUNT)));

        Integer blockLastPageNum = (totalLastPageNum > curPageNum + BLOCK_PAGE_NUM_COUNT)
                ? curPageNum + BLOCK_PAGE_NUM_COUNT
                : totalLastPageNum;

        curPageNum = (curPageNum <= 3) ? 1 : curPageNum - 2;

        for (int val = curPageNum, i = 0; val <= blockLastPageNum; val++, i++) {
            pageList[i] = val;
        }

        return pageList;
    }

    @Override
    @Transactional
    public List<UserDTO> searchUsers(String keyword) {
        List<User> users = userRepository.findByNameContaining(keyword);
        List<UserDTO> userDTOList = new ArrayList<>();

        if (users.isEmpty()) return userDTOList;

        for (User user : users) {
            userDTOList.add(this.convertEntityToDto(user));
        }
        return userDTOList;
    }

    @Override
    @Transactional
    public UserDTO getPost(Long id) {
        Optional<User> userWrapper = userRepository.findById(id);
        User user = userWrapper.get();

        UserDTO userDTO = UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPassword())
                .name(user.getName())
                .nickname(user.getNickname())
                .phoneNumber(user.getPhoneNumber())
                .build();

        return userDTO;
    }

    @Override
    @Transactional
    public void deleteByIdIn(List<Long> ids) {
        for (Long id : ids) {
            userRepository.deleteById(id);
        }
    }

    @Override
    @Transactional
    public Long savePost(UserDTO userDTO) {

        return userRepository.save(userDTO.toEntity()).getId();
    }

    @Override
    public void pwUpdate(UserDTO userDTO) {

        String encryptPassword = encoder.encode(userDTO.getPassword());
    }

    @Override
    public void reloadPw(UserDTO userDTO) {
        Long id = userDTO.getId();
        String password = userDTO.getPassword();

    }


    private UserDTO convertEntityToDto(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPassword())
                .name(user.getName())
                .nickname(user.getNickname())
                .phoneNumber(user.getPhoneNumber())
                .profileImage(user.getProfileImage())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .status(user.getStatus())
                .deletedAt(user.getDeletedAt())
                .build();
    }

    @Transactional
    public Long getUserCount() {
        return userRepository.count();
    }

    @Transactional(readOnly = true)
    @Override
    public User 회원찾기(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            return user.get();
        } else {
            return new User();
        }
    }

    @Transactional
    @Override
    public void 회원가입(User user) {
        String rawPassword = user.getPassword(); // 1234 원문
        String encPassword = encoder.encode(rawPassword); // 해쉬
        user.setPassword(encPassword);
        user.setRole(RoleType.USER);
        userRepository.save(user);
    }

    @Transactional
    @Override
    public void 회원수정(User user) {
        // 수정시에는 영속성 컨텍스트 User 오브젝트를 영속화시키고, 영속화된 User 오브젝트를 수정
        // select를 해서 User오브젝트를 DB로부터 가져오는 이유는 영속화를 하기 위해서!!
        // 영속화된 오브젝트를 변경하면 자동으로 DB에 update문을 날려주거든요.
        User persistance = userRepository.findById(user.getId()).orElseThrow(() -> {
            return new IllegalArgumentException("회원 찾기 실패");
        });

        // Validate 체크 => oauth 필드에 값이 없으면 수정 가능
        if (persistance.getOauth() == null || persistance.getOauth().equals("")) {
            String rawPassword = user.getPassword();
            String encPassword = encoder.encode(rawPassword);
            persistance.setPassword(encPassword);
            persistance.setEmail(user.getEmail());
            persistance.setName(user.getName());
            persistance.setNickname(user.getNickname());
            persistance.setPhoneNumber(user.getPhoneNumber());
        }


        // 회원수정 함수 종료시 = 서비스 종료 = 트랜잭션 종료 = commit이 자동으로 됩니다.n
        // 영속화된 persistance 객체의 변화가 감지되면 더티체킹이 되어 update문을 날려줌.
    }

    @Transactional
    @Override
    public void 회원수정2(User user) {
        // 수정시에는 영속성 컨텍스트 User 오브젝트를 영속화시키고, 영속화된 User 오브젝트를 수정
        // select를 해서 User오브젝트를 DB로부터 가져오는 이유는 영속화를 하기 위해서!!
        // 영속화된 오브젝트를 변경하면 자동으로 DB에 update문을 날려주거든요.
        User persistance = userRepository.findById(user.getId()).orElseThrow(() -> {
            return new IllegalArgumentException("회원 찾기 실패");
        });

        // Validate 체크 => oauth 필드에 값이 없으면 수정 가능
        if (persistance.getOauth() == null || persistance.getOauth().equals("")) {
            String rawPassword = user.getPassword();
            String encPassword = encoder.encode(rawPassword);
            persistance.setPassword(encPassword);
        }


        // 회원수정 함수 종료시 = 서비스 종료 = 트랜잭션 종료 = commit이 자동으로 됩니다.n
        // 영속화된 persistance 객체의 변화가 감지되면 더티체킹이 되어 update문을 날려줌.
    }

    @Override
    public Optional<UserDTO>  getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String email = userDetails.getUsername();
            Optional<User> foundUser = userRepository.findByEmail(email);
            if (foundUser.isPresent()){
                UserDTO userDTO = UserDTO.builder()
                        .id(foundUser.get().getId())
                        .email(foundUser.get().getEmail())
                        .password(foundUser.get().getPassword())
                        .name(foundUser.get().getName())
                        .nickname(foundUser.get().getNickname())
                        .phoneNumber(foundUser.get().getPhoneNumber())
                        .profileImage(foundUser.get().getProfileImage())
                        .createdAt(foundUser.get().getCreatedAt())
                        .updatedAt(foundUser.get().getUpdatedAt())
                        .status(foundUser.get().getStatus())
                        .deletedAt(foundUser.get().getDeletedAt())
                        .oauth(foundUser.get().getOauth())
                        .role(foundUser.get().getRole())
                        .build();
                return Optional.of(userDTO);
            } else{
                return Optional.empty();
            }
        }
        else {
            return Optional.empty();
        } // optional 객체가 존재하는지 (isPresent()) 여부 체크 하세요.
    }

    @Override
    public UserDTO findId(String name, String phoneNumber) {
        Optional<User> userOptional = userRepository.findByNameAndPhoneNumber(name, phoneNumber);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            UserDTO userDTO = new UserDTO();
            userDTO.setEmail(user.getEmail());
            return userDTO;
        } else {
            return null;
        }
    }

    @Override
    public Optional<User> findPw(String email, String name, String phoneNumber) {

        UserDTO userDTO = new UserDTO();
        Optional<User> user = userRepository.findByEmail(email);
        Optional<User> byNameAndPhoneNumber = userRepository.findByNameAndPhoneNumber(name, phoneNumber);
        if(!user.isEmpty() && !byNameAndPhoneNumber.isEmpty()){
            return user;

        }
        return null;
    }

    @Override
    public String mailck(String email) {
        Optional<User> byEmail = userRepository.findByEmail(email);
        if(byEmail.isPresent()){
            return "사용불가";
        }
        System.out.println("byEmail2:"+byEmail);
        return "사용가능";
    }
}
