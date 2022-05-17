package org.hse.validator;


import org.apache.commons.validator.routines.EmailValidator;
import org.hse.model.User;
import org.hse.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class UserValidator implements Validator {



    private final int MAX_LENGTH = 32;
    private final String NAME_REGEX = "([A-Z][a-z]*).{1,32}";
    private final String PPS_REGEX = "[0-9]{7}([A-Z]|[a-z]){1,2}";

    //private final String LAST_NAME_REGEX = "(^[\\p{L}\\s.’\\-,]+$).{2,32}";


    // (?=.*[a-z])   The string must contain at least 1 lowercase alphabetical character
// (?=.*[A-Z])	The string must contain at least 1 uppercase alphabetical character
// (?=.*[0-9])	The string must contain at least 1 numeric character
// (?=.*[!@#$%^&*])	The string must contain at least one special character, but we are escaping reserved RegEx characters to avoid conflict
// (?=.{8,32})	The string must be eight characters or longer up to 32 characters
    private final String PASSWORD_REGEX = "((?=.*[a-z])(?=.*\\d)(?=.*[A-Z])(?=.*[@#$%!]).{8,32})";

    /*
    RFC 3522
     */

    //private final String EMAIL_PATTERN = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21\x23-\x5b\x5d-\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\]);

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean supports(Class<?> aClass) {
        return User.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        User user = (User) o;

        if(isUserInValid(user))
            errors.rejectValue("name", "Too Long");

        if(!isEmailValid(user.getUsername()))
            errors.rejectValue("email", "InvalidEmail");

        try {
            if(!isOver18(new SimpleDateFormat("yyyy-MM-dd").parse(user.getDob())))
                errors.rejectValue("DOB", "Too Young");
        } catch (ParseException e) {
            errors.rejectValue("DOB", "Invalid");
        }

        if(!isValid(user.getPpsn(), PPS_REGEX))
            errors.rejectValue("ppsn", "Invalid PPS");

        //ValidationUtils.rejectIfEmptyOrWhitespace(errors, "username", "NotEmpty");

        if (!isValid(user.getPassword(), PASSWORD_REGEX))
            errors.rejectValue("password", "Invalid Email or Password");

        if(userRepository.findByUsername(user.getUsername()) != null)
            errors.rejectValue("username", "Invalid Email or Password");
    }

    public void validatePassword(String password, Errors errors){
        if(!isValid(password, PASSWORD_REGEX))
            errors.rejectValue("passwordConfirm", "Diff.userForm.passwordConfirm");


    }

    private boolean isValid(String toValidate, String regex) {

        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(toValidate);
        return matcher.matches();

    }

    private boolean isEmailValid(String email) {

        return EmailValidator.getInstance().isValid(email) && email.length() <= MAX_LENGTH;
    }

    private boolean isUserInValid(User user){
        return (user.getAddress().length() > MAX_LENGTH || user.getFirstName().length() > MAX_LENGTH ||
                user.getSurname().length() > MAX_LENGTH ||(user.getNationality().length() > MAX_LENGTH)
                ||(user.getPhoneNumber().length() > MAX_LENGTH));
    }


    private boolean isOver18(Date dob){
        Calendar cal = Calendar.getInstance();

        cal.add(Calendar.YEAR, -18);
        Date date = cal.getTime();
        System.out.println(new SimpleDateFormat("dd/MM/yyyy").format(date));

        return date.getTime() > dob.getTime();
    }



}