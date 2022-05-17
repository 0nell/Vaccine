package org.hse.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


@Service
public class LoginAttemptService {

    private final int MAX_ATTEMPT = 2;
    private LoadingCache<String, Integer> attemptsCache;
    private Logger logger = LoggerFactory.getLogger(LoginAttemptService.class);


    public LoginAttemptService() {
        super();
        attemptsCache = CacheBuilder.newBuilder().
                expireAfterWrite(1, TimeUnit.MINUTES).build(new CacheLoader<String, Integer>() {
            public Integer load(String key) {
                return 0;
            }
        });
    }

    public void loginSucceeded(String key) {
        logger.info("Successful login from IP: {"+key+"}");
        attemptsCache.invalidate(key);
    }

    public void loginFailed(String key) {
        logger.info("UnSuccessful login from IP: {"+key+"}");
        int attempts = 0;
        try {
            attempts = attemptsCache.get(key);
        } catch (ExecutionException e) {
            attempts = 0;
        }
        attempts++;
        if(attempts>=MAX_ATTEMPT)
            logger.warn("Too Many UnSuccessful logins from IP: {"+key+"}, IP BLOCKED");
        System.out.println(key + ", " + attempts);
        attemptsCache.put(key, attempts);
    }

    public boolean isBlocked(String key) {
        try {
            return attemptsCache.get(key) >= MAX_ATTEMPT;
        } catch (ExecutionException e) {
            return false;
        }
    }
}
