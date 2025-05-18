package com.secure.security.config;

import com.secure.appuser.AppUserService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@AllArgsConstructor
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final AppUserService appUserService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private RecaptchaConfig.RecaptchaService recaptchaService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                // Allow public access to login and static resources
                .antMatchers("/login", "/register/confirm", "/css/**").permitAll()
                // Only teachers can view the registration form and call the API
                .antMatchers("/dashboard", "/register", "/users", "/courses", "/api/**").hasAuthority("TEACHER")
                // Courses listing is public
                .antMatchers("/dashboard-student").hasAuthority("STUDENT")
                // All other endpoints require authentication
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/login")
                .successHandler(customAuthenticationSuccessHandler())
                .permitAll();

        // Add reCAPTCHA filter
        http.addFilterBefore(new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request,
                                          HttpServletResponse response,
                                          FilterChain filterChain) throws ServletException, IOException {
                if (request.getMethod().equals("POST") && request.getRequestURI().equals("/login")) {
                    String recaptchaResponse = request.getParameter("g-recaptcha-response");
                    if (recaptchaResponse == null || !recaptchaService.verifyRecaptcha(recaptchaResponse)) {
                        response.sendRedirect("/login?error=captcha");
                        return;
                    }
                }
                filterChain.doFilter(request, response);
            }
        }, UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(daoAuthenticationProvider());
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();
        provider.setPasswordEncoder(bCryptPasswordEncoder);
        provider.setUserDetailsService(appUserService);
        return provider;
    }

    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            Object principal = authentication.getPrincipal();
            if (principal instanceof com.secure.appuser.AppUser) {
                com.secure.appuser.AppUser user = (com.secure.appuser.AppUser) principal;
                if (user.getAppUserRole() != null && user.getAppUserRole().name().equals("STUDENT")) {
                    response.sendRedirect("/dashboard-student");
                    return;
                }
            }
            response.sendRedirect("/dashboard");
        };
    }


}
