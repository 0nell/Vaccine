package org.hse.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;


@Service("userDetailsService")
@Transactional
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private LoginAttemptService loginAttemptService;

    private Logger logger = LoggerFactory.getLogger(MyUserDetailsService.class);

    @Autowired
    private HttpServletRequest request;

    @Autowired
    DataSource dataSource;

    JdbcDaoImpl jdbc = new JdbcDaoImpl();

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String ip = getClientIP();
        if (loginAttemptService.isBlocked(ip)) {
            throw new RuntimeException("blocked");
        }
        logger.info("Login attempt for account {"+email+"} from IP: {"+ip+"}");
        jdbc.setDataSource(dataSource);
        jdbc.setUsersByUsernameQuery("select aes_decrypt(from_base64(username), 'pls-dont-reveal1'),aes_decrypt(from_base64(password), 'pls-dont-reveal1'),enabled from users where username=to_base64(aes_encrypt(?, 'pls-dont-reveal1'))");
        jdbc.setAuthoritiesByUsernameQuery("select aes_decrypt(from_base64(username), 'pls-dont-reveal1'),aes_decrypt(from_base64(authority), 'pls-dont-reveal1') from users where username=to_base64(aes_encrypt(?, 'pls-dont-reveal1'))");
        return jdbc.loadUserByUsername(email);
    }

    private String getClientIP() {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null){
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
