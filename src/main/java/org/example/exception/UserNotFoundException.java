package org.example.exception;
public class UserNotFoundException extends Exception {
private Long user_id;
public UserNotFoundException(Long user_id) {
        super(String.format("User is not found with id : '%s'", user_id));
        }
}
