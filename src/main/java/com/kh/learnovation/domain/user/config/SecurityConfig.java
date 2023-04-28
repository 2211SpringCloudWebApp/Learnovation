package com.kh.learnovation.domain.user.config;

import com.kh.learnovation.domain.user.config.auth.PrincipalDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;



@Configuration // IoC
public class SecurityConfig {

	@Autowired
	private PrincipalDetailService principalDetailService;
	
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
	    return authenticationConfiguration.getAuthenticationManager();
	}
	
	@Bean // IoC가 되요!!
	public BCryptPasswordEncoder encodePWD() {
		return new BCryptPasswordEncoder();
	}
	
	// 시큐리티가 대신 로그인해주는데 password를 가로채기를 하는데
	// 해당 password가 뭘로 해쉬가 되어 회원가입이 되었는지 알아야
	// 같은 해쉬로 암호화해서 DB에 있는 해쉬랑 비교할 수 있음.
	protected void configure(AuthenticationManagerBuilder auth) throws Exception{
		auth.userDetailsService(principalDetailService).passwordEncoder(encodePWD());
	}
	
	/*
	 * @Bean BCryptPasswordEncoder encode() { return new BCryptPasswordEncoder(); }
	 */

	@Bean
	SecurityFilterChain configure(HttpSecurity http) throws Exception {
		http
			.csrf().disable()	// csrf 토큰 비활성화 (테스트시 걸어두는 게 좋음)
			.authorizeRequests()
			.antMatchers("/" ,"/auth/**", "/js/**", "/css/**", "/image/**", "/dummy/**")
			.permitAll()
			.anyRequest()
			.authenticated()
		.and()
		.logout()
		 .addLogoutHandler((request, response, authentication) -> { 
             // 사실 굳이 내가 세션 무효화하지 않아도 됨. 
             // LogoutFilter가 내부적으로 해줌.          
         })
				/* .logoutUrl("/logout1") .logoutSuccessUrl("/auth/loginForm") */
		.and()
			.formLogin()
			.loginPage("/auth/loginForm")
			.loginProcessingUrl("/auth/loginProc")
			.defaultSuccessUrl("/"); // 스프링 시큐리티가 해당 주소로 요청오는 로그인을 가로채서 대신 로그인 해준다.
		    
		return http.build();
	}
}
