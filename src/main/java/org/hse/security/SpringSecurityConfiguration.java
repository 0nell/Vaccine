package org.hse.security;


import org.hse.service.MyUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class SpringSecurityConfiguration extends WebSecurityConfigurerAdapter {

    String[] adminAccessPaths= {"/admin","/logout","/forum/answer","/apply-dose"};
    String[] userAccessPaths= {"/user","/book","/logout","/cancel-appointment"};

    @Autowired
    DataSource dataSource;

    @Autowired
    @Qualifier("userDetailsService")
    MyUserDetailsService myUserDetailsService;

    @Autowired
    private AuthenticationFailureHandler authenticationFailureHandler;

    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(myUserDetailsService).passwordEncoder(passwordEncoder());
    }
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(myUserDetailsService).passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                    .antMatchers(adminAccessPaths).hasAuthority("ADMIN")
                    .antMatchers(userAccessPaths).hasAuthority("USER")
                    .anyRequest().permitAll()
                .and()
                .formLogin()
                    .loginPage("/login")
                    .permitAll()
                    .defaultSuccessUrl("/home",true)
                    .failureUrl("/login-uperror")
                //.successHandler(NEW SUCCESS HANDLER?)
                .and()
                    .logout()
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/logoutComplete")
                    // .logoutSuccessHandler(logoutSuccessHandler)                              4
                   //  .invalidateHttpSession(true)                                             5
                   //  .addLogoutHandler(logoutHandler)                                         6
                    //.deleteCookies(cookieNamesToClear)
                .and()
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

}
