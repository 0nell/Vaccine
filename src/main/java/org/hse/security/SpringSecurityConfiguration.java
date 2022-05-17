package org.hse.security;


import org.hse.filter.JWTAuthenticationFilter;
import org.hse.filter.JWTAuthorizationFilter;
import org.hse.model.UserType;
import org.hse.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

import javax.sql.DataSource;
import static org.hse.filter.SecurityConstants.COOKIE_NAME;

@Configuration
@EnableWebSecurity
public class SpringSecurityConfiguration extends WebSecurityConfigurerAdapter {

    String[] adminAccessPaths= {"/admin","/logout","/forum/answer","/apply-dose"};
    String[] userAccessPaths= {"/user","/book","/logout","/cancel-appointment"};

    @Autowired
    DataSource dataSource;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication()
                .dataSource(dataSource)
                .passwordEncoder(passwordEncoder())
                .usersByUsernameQuery("select username,password,enabled from users where username=?")
                .authoritiesByUsernameQuery("select username,authority from users where username=?");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().disable()
                .authorizeRequests()
                    .antMatchers("/css/**","/images/**").permitAll()
                .and()
                .authorizeRequests()
                    .antMatchers(adminAccessPaths).hasAuthority("ADMIN")
                    .antMatchers(userAccessPaths).hasAuthority("USER")
                .and()
                    .authorizeRequests()
                    .antMatchers("/user","/book","logout","cancel-appointment","admin","/forum/answer","/apply-dose").permitAll()
                    .anyRequest().authenticated()
                .and()
                .formLogin()
                    .loginPage("/login")
                    .permitAll()
                    .defaultSuccessUrl("/home",true)
                    .failureUrl("/login-error")
                //.successHandler(NEW SUCCESS HANDLER?)
                .and()
                    .logout()
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login")
                    .invalidateHttpSession(true) 
                    .deleteCookies(COOKIE_NAME)
                    .and()
                    //.addFilter(new JWTAuthenticationFilter(authenticationManager()))
                    //.addFilter(new JWTAuthorizationFilter(authenticationManager()))
                    .headers()
                    .frameOptions()
                    .deny()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                .and()
                .headers()
                .xssProtection()
                .and()
                .contentSecurityPolicy("script-src 'self'");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        configuration.setAllowedOrigins(Arrays.asList("localhost"));
        source.registerCorsConfiguration("/**", new CorsConfiguration().applyPermitDefaultValues());
        return source;
    }
}
