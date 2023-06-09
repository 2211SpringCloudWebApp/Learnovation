package com.kh.learnovation.domain.user.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kh.learnovation.domain.jobexplore.dto.JobExploreDTO;
import com.kh.learnovation.domain.notice.entity.Notice;
import com.kh.learnovation.domain.notice.service.NoticeService;
import com.kh.learnovation.domain.user.dto.UserDTO;
import com.kh.learnovation.domain.user.entity.User;
import com.kh.learnovation.domain.user.service.UserService;
import com.kh.learnovation.domain.user.service.UserServiceImpl;
import groovy.util.logging.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kh.learnovation.domain.user.config.auth.PrincipalDetail;
import com.kh.learnovation.domain.user.model.KakaoProfile;
import com.kh.learnovation.domain.user.model.OAuthToken;
import com.kh.learnovation.domain.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;


// 인증이 안된 사용자들이 출입할 수 있는 경로를 /auth/** 허용
// 그냥 주소가 / 이면 index.jsp 허용
// static이하에 있는 /js/**, /css/**, /image/**

@Slf4j
@Controller
public class UserController {

	private final Logger logger = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private NoticeService nService;
	//HttpURLConnection con = (HttpURLConnection) url.openConnection();
	private String cosKey = "cos1234";

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	// 회원 목록 조회
	@RequestMapping(value = "/admin/userList", method = RequestMethod.GET)
	public String userList(
			Model model,
			@RequestParam(value = "page", defaultValue = "1") Integer pageNum
	) {

		List<UserDTO> userList = userService.getUserList(pageNum);
		Integer[] pageList = userService.getPageList(pageNum);

		model.addAttribute("userList", userList);
		model.addAttribute("pageList", pageList);

		return "admin/userList";
	}

	// 회원 이름으로 검색
	@RequestMapping(value = "/admin/search")
	public String search(@RequestParam(value = "keyword") String keyword, Model model) {
		List<UserDTO> userDTOList = userService.searchUsers(keyword);
		model.addAttribute("userList", userDTOList);

		return "admin/userList";
	}

    // 회원 상세정보 조회(관리자)
    @RequestMapping(value = "/admin/detail/{no}", method = RequestMethod.GET)
    public String detail(@PathVariable("no") Long id, Model model) {
        UserDTO userDTO = userService.getPost(id);

		model.addAttribute("user", userDTO);
		return "admin/userDetail";
	}

	// 회원 정보 수정
	@PutMapping("/admin/update/{id}")
	public String userUpdate(UserDTO userDTO) {
		userService.savePost(userDTO);
		return "redirect:/admin/userList";
	}

	// 회원 탈퇴(삭제)
	@PostMapping("/admin/delete")
	public String userDelete(@RequestParam("selectedIds") List<Long> selectedIds) {
		userService.deleteByIdIn(selectedIds);
		return "redirect:/admin/userList";
	}

	/**
	 * 승현파트
	 */
	@GetMapping("/")
	public String indexForm(Model model) {

		// 메인페이지 공지사항 ---------------------------------
		Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC,"updatedAt"));
		Page<Notice> pNotice = nService.selectAllNotice(pageable);
		List<Notice> nList = pNotice.stream().collect(Collectors.toList());
		model.addAttribute("nList", nList);
		// ----------------------------------------------------
		// 메인페이지 직업 정보 ---------------------------------
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
		factory.setConnectTimeout(5000); // 타임아웃 설정 5초
		factory.setReadTimeout(5000);

		RestTemplate restTemplate = new RestTemplate(factory);

		// url 생성
		int randomPage = (int)(Math.random() * 6) + 1;
		URI url = UriComponentsBuilder.fromHttpUrl("https://career.go.kr/cnet/front/openapi/jobs.json")
				.queryParam("apiKey", "dcbceba3669ac12190c6af8572955b13")
				.queryParam("searchThemeCode", 102421)
				.queryParam("pageIndex", randomPage)
				.build().toUri();
		// 헤더 생성
		HttpHeaders headers = new HttpHeaders();
		// 받을 데이터 타입 명시
		headers.setContentType(new MediaType("application","json", Charset.forName("UTF-8")));
		headers.setAccept(Arrays.asList(new MediaType[] { MediaType.APPLICATION_JSON }));

		// resttemplate api 호출 통신
		ResponseEntity<String> response = restTemplate.exchange(url.toString()
				, HttpMethod.GET, new HttpEntity<>(headers), String.class);
		System.out.println(response.getStatusCode());
		// 통신 성공시
		if(response.getStatusCode().is2xxSuccessful()) {
			JsonParser jsonParser = new JsonParser();
			// response body Json 데이터 파싱
			JsonObject jsonObject = (JsonObject) JsonParser.parseString(response.getBody());
			String pageSize = jsonObject.get("pageSize").toString().replaceAll("\"", "");
			String totalCount = jsonObject.get("count").toString().replaceAll("\"", "");
			String apipage = jsonObject.get("pageIndex").toString().replaceAll("\"", "");
			int totalPage = (int) Math.ceil((double) Integer.parseInt(totalCount) / Integer.parseInt(pageSize));
			int page = Integer.parseInt(apipage);
			List<JobExploreDTO> jList = new ArrayList<JobExploreDTO>();
			JsonArray jsonArray = (JsonArray) jsonObject.get("jobs");
			for (int i = 0; i < jsonArray.size(); i++) {
				JobExploreDTO job = new JobExploreDTO();
				JsonObject object = (JsonObject) jsonArray.get(i);
				job.setJobId(object.get("job_cd").toString().replaceAll("\"", ""));
				if (object.get("social") != null) {
					job.setSocial(object.get("social").toString().replaceAll("\"", ""));
				} else {
					job.setSocial("알 수 없음");
				}
				job.setWork(object.get("work").toString().replaceAll("\"", ""));
				if (object.get("wage") != null) {
					job.setSalary(object.get("wage").toString().replaceAll("\"", ""));
				} else {
					job.setSalary("알 수 없음");
				}
				job.setJobName(object.get("job_nm").toString().replaceAll("\"", ""));
				if (object.get("wlb") != null) {
					job.setWorkLifeBalance(object.get("wlb").toString().replaceAll("\"", ""));
				} else {
					job.setWorkLifeBalance("알 수 없음");
				}
				jList.add(job);
			}
			model.addAttribute("jList", jList);
		}
		// ------------------- 직업정보 ----------------------------------
		Optional<UserDTO> userDTO = userService.getCurrentUser();
		if (userDTO.isPresent()){
				//System.out.println(userDTO.toString());
			}
		return "index";
	}

	@GetMapping("/auth/joinForm")
	public String joinForm() {
		return "user/joinForm";
	}

	@GetMapping("/auth/loginForm")
	public String loginForm() {
		return "user/loginForm";
	}

	@GetMapping("/auth/findId")
	public String findId() {
		return "user/findId";
	}

	@GetMapping("/auth/findPw")
	public String findPw() {
		return "user/findPw";
	}

	@GetMapping("/loginError")
	public String loginError(@RequestParam(value = "errorMsg", required = false) String errorMsg,
							 Model model) {
		model.addAttribute("errorMsg", errorMsg);
		return "user/loginForm";
	}

	//아이디 찾기
	@PostMapping("findIdLogic")
	@ResponseBody
	public String findId(@ModelAttribute UserDTO userDTO, Model model) {
		String name = userDTO.getName();
		String phoneNumber = userDTO.getPhoneNumber();
		UserDTO findUserDto = userService.findId(name, phoneNumber);
		if (findUserDto != null) {
			return findUserDto.getEmail();
//			model.addAttribute("id", findUserDto.getEmail());
		} else {
			return "실패";
//			model.addAttribute("id", "일치하는 회원정보가 없습니다.");
		}
	}

	//비밀번호 찾기
	@PostMapping("/auth/findPwLogic")
	public String findPw(@ModelAttribute UserDTO userDTO, Model model , RedirectAttributes redirectAttributes) {
		String email = userDTO.getEmail();
		String name = userDTO.getName();
		String phoneNumber = userDTO.getPhoneNumber();
		Optional<User> findUserDto = userService.findPw(email, name, phoneNumber);
		System.out.println("findUserDto::"+findUserDto);
		if (findUserDto != null) {
			findUserDto.get().getEmail();
			System.out.println("email:"+findUserDto.get().getEmail());
			model.addAttribute("email", findUserDto.get().getEmail());
			model.addAttribute("id", findUserDto.get().getId());
			return "user/changePw";
		} else {
			model.addAttribute("id", "일치하는 회원정보가 없습니다.");
			redirectAttributes.addAttribute("status" , false);
			return "redirect:/auth/findPw";
		}
	}

	@PostMapping("/auth/resultPw/{id}")
	public String resultPw(UserDTO userDTO , @PathVariable Long id){
		User user = new User();
		Long userId = userDTO.getId();
		String userPw = userDTO.getPassword();
		user.setId(id);
		user.setPassword(userDTO.getPassword());
		logger.info("userId ={}", id);
		logger.info("userPw ={}", userPw);

		userService.회원수정2(user);

		return "redirect:/auth/loginForm";
	}



	@GetMapping("/auth/kakao/callback")
	public String kakaoCallback(String code, Model model) { // Data를 리턴해주는 컨트롤러 함수, model추가

		// POST방식으로 key=value 데이터를 요청 (카카오쪽으로)
		// Retrofit2
		// OkHttp
		// RestTemplate

		RestTemplate rt = new RestTemplate();

		// HttpHeader 오브젝트 생성
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

		// HttpBody 오브젝트 생성
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("grant_type", "authorization_code");
		params.add("client_id", "0c7a67f1f4fcb8912b7d917baa9f87ee");
		params.add("redirect_uri", "http://localhost:9999/auth/kakao/callback");
		params.add("code", code);

		// HttpHeader와 HttpBody를 하나의 오브젝트에 담기
		HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest =
				new HttpEntity<>(params, headers);

		// Http 요청하기 - Post방식으로 - 그리고 response 변수의 응답 받음.
		ResponseEntity<String> response = rt.exchange(
				"https://kauth.kakao.com/oauth/token",
				HttpMethod.POST,
				kakaoTokenRequest,
				String.class
		);

		// Gson, Json Simple, ObjectMapper
		ObjectMapper objectMapper = new ObjectMapper();
		OAuthToken oauthToken = null;
		try {
			oauthToken = objectMapper.readValue(response.getBody(), OAuthToken.class);
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		System.out.println("카카오 엑세스 토큰 : "+oauthToken.getAccess_token());

		RestTemplate rt2 = new RestTemplate();

		// HttpHeader 오브젝트 생성
		HttpHeaders headers2 = new HttpHeaders();
		headers2.add("Authorization", "Bearer "+oauthToken.getAccess_token());
		headers2.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");


		// HttpHeader와 HttpBody를 하나의 오브젝트에 담기
		HttpEntity<MultiValueMap<String, String>> kakaoProfileRequest2 =
				new HttpEntity<>(headers2);

		// Http 요청하기 - Post방식으로 - 그리고 response 변수의 응답 받음.
		ResponseEntity<String> response2 = rt2.exchange(
				"https://kapi.kakao.com/v2/user/me",
				HttpMethod.POST,
				kakaoProfileRequest2,
				String.class
		);
		System.out.println(response2.getBody());

		ObjectMapper objectMapper2 = new ObjectMapper();
		KakaoProfile kakaoProfile = null;
		try {
			kakaoProfile = objectMapper2.readValue(response2.getBody(), KakaoProfile.class);
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		// User 오브젝트 : username, password, email
		System.out.println("카카오 아이디(번호) : "+kakaoProfile.getId());
		System.out.println("카카오 이메일 : "+kakaoProfile.getKakao_account().getEmail());

		System.out.println("블로그서버 유저네임 : "+kakaoProfile.getKakao_account().getEmail()+"_"+kakaoProfile.getId());
		System.out.println("블로그서버 이메일 : "+kakaoProfile.getKakao_account().getEmail());
		UUID garbagePassword = UUID.randomUUID();
		// UUID란 -> 중복되지 않는 어떤 특정 값을 만들어내는 알고리즘
		System.out.println("블로그서버 패스워드 : "+cosKey);
		KakaoProfile.Properties properties = kakaoProfile.properties;
		User kakaoUser = User.builder()
				.email(kakaoProfile.getKakao_account().getEmail()+"_"+kakaoProfile.getId())
				.password(cosKey)

				//.name("설석현")
				.nickname(properties.getNickname())
				//.phoneNumber("010")


				//.email(kakaoProfile.getKakao_account().getEmail())
				.oauth("kakao")
				.build();

		// 가입자 혹은 비가입자 체크 해서 처리
		User originUser = userService.회원찾기(kakaoUser.getEmail());

		if(originUser.getEmail() == null) {
			model.addAttribute("user", kakaoUser);
			System.out.println("기존 회원이 아니기에 자동 회원가입을 진행합니다");
			return "user/socialForm";
			//userService.회원가입(kakaoUser);
		}

		System.out.println("자동 로그인을 진행합니다.");
		// 로그인 처리
		Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(kakaoUser.getEmail(), cosKey));
		SecurityContextHolder.getContext().setAuthentication(authentication);

		return "redirect:/";
	}

	@GetMapping("/user/updateForm")
	public String updateForm(@AuthenticationPrincipal PrincipalDetail principal) {
		return "user/updateForm";
	}

	@PostMapping("/user/socialForm")
	public String socialForm(@AuthenticationPrincipal PrincipalDetail principal, UserDTO userDTO) {
		User user = User.builder()
				.email(userDTO.getEmail())
				.password(userDTO.getPassword())
				.name(userDTO.getName())
				.nickname(userDTO.getNickname())
				.phoneNumber(userDTO.getPhoneNumber())
				.oauth(userDTO.getOauth())
				.build();
		userService.회원가입(user);
		// 로그인 처리
		return "redirect:https://kauth.kakao.com/oauth/authorize?client_id=0c7a67f1f4fcb8912b7d917baa9f87ee&redirect_uri=http://localhost:9999/auth/kakao/callback&response_type=code";
	}

	/*
	 * @RequestMapping(value="/logout1" ) public String access(HttpSession session)
	 * throws IOException {
	 *
	 * String access_token = (String)session.getAttribute("access_token");
	 * System.out.println("access_token:"+access_token); Map<String, String> map =
	 * new HashMap<String, String>(); map.put("Authorization", "Bearer "+
	 * access_token);
	 *
	 * //String result = conn.HttpURLConnection
	 * ("https://kapi.kakao.com/v1/user/logout", map).toString();
	 * //System.out.println(result);
	 *
	 * return "redirect:/"; }
	 */

    @PutMapping("/admin/pwUpdate")
    public String passwordUpdate(UserDTO userDTO) {

        userService.pwUpdate(userDTO);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userDTO.getEmail(), userDTO.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return "redirect:/admin/userList";
    }

}
