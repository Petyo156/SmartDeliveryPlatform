package org.tuvarna.smartdeliveryplatform.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.tuvarna.smartdeliveryplatform.web.interceptor.ActiveCourierInterceptor;
import org.tuvarna.smartdeliveryplatform.web.interceptor.ActiveMerchantInterceptor;
import org.tuvarna.smartdeliveryplatform.web.interceptor.ActiveUserInterceptor;

@Configuration
@EnableWebSecurity
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final ActiveMerchantInterceptor activeMerchantInterceptor;
    private final ActiveCourierInterceptor activeCourierInterceptor;
    private final ActiveUserInterceptor activeUserInterceptor;

    public WebMvcConfiguration(ActiveMerchantInterceptor activeMerchantInterceptor,
                               ActiveCourierInterceptor activeCourierInterceptor,
                               ActiveUserInterceptor activeUserInterceptor) {
        this.activeMerchantInterceptor = activeMerchantInterceptor;
        this.activeCourierInterceptor = activeCourierInterceptor;
        this.activeUserInterceptor = activeUserInterceptor;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(matchers -> matchers
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers("/", "/register", "/restaurants/**", "/shops/**", "/search/**", "/merchant/**",
                                      "/about", "/contact", "/faq", "/privacy", "/contact/submit",
                                      "/careers", "/shipping", "/terms", "/cookies", "/error").permitAll()
                        .requestMatchers("/dashboard/merchant/**", "/products/**", "/category/**").hasRole("MERCHANT")
                        .requestMatchers("/courier/**").hasRole("COURIER")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error")
                        .permitAll())
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.sendError(HttpServletResponse.SC_NOT_FOUND);
                        }))
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                        .logoutSuccessUrl("/")
                );

        return http.build();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(activeUserInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/login", "/register", "/logout", "/error", "/css/**",
                        "/js/**", "/images/**", "/webjars/**", "/favicon.ico");
        registry.addInterceptor(activeMerchantInterceptor)
                .addPathPatterns("/dashboard/merchant/**", "/products/**", "/category/**");
        registry.addInterceptor(activeCourierInterceptor)
                .addPathPatterns("/courier/**");
    }
}
