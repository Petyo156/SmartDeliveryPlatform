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
import org.tuvarna.smartdeliveryplatform.web.interceptor.ActiveMerchantInterceptor;

@Configuration
@EnableWebSecurity
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final ActiveMerchantInterceptor activeMerchantInterceptor;

    public WebMvcConfiguration(ActiveMerchantInterceptor activeMerchantInterceptor) {
        this.activeMerchantInterceptor = activeMerchantInterceptor;
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
        registry.addInterceptor(activeMerchantInterceptor)
                .addPathPatterns("/dashboard/merchant/**", "/products/**", "/category/**");
    }
}
