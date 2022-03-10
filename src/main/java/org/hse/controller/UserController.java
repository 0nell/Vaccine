package org.hse.controller;

import org.hse.exception.UserNotFoundException;
import org.hse.model.User;
import org.hse.model.UserType;
import org.hse.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Controller
public class UserController {

    @Autowired
    UserRepository userRepository;
    private long currentUserID = 0;

    @EventListener(ApplicationReadyEvent.class)
    public void initialiseDatabasevalues() {
        if(userRepository.count() >= 2)
        {
            System.out.println("full");
        }
        else
        {
            userRepository.save(new User("em1","pass1"));
            userRepository.save(new User("em","pass"));
        }
    }

    @RequestMapping({"/", "/home"})
    public String index() throws ParseException {
        return "index.html";
    }

    @RequestMapping({"/Book"})
    public String Book() {
        return "Book.html";
    }

    @RequestMapping({"/signup"})
    public String signup()
    {
        return "signup.html";
    }

    @RequestMapping({"/login"})
    public String login()
    {
        return "login.html";
    }

    @RequestMapping({"/stats"})
    public String stats()
    {
        return "stats.html";
    }

    @RequestMapping({"/forum"})
    public String forum()
    {
        return "forum.html";
    }

    @PostMapping({"/signup"})
    public void signup_submit(User user, HttpServletResponse response, Model model) throws IOException, ParseException {
        boolean emailExists = userRepository.findByEmail(user.getEmail()).isEmpty();
        boolean ppsExists = userRepository.findByPpsn(user.getPpsn()).isEmpty();
        Date d = new SimpleDateFormat("yyyy-MM-dd").parse(user.getDob());
        boolean ageRequirement = isOver18(d);

        if(emailExists && ppsExists && ageRequirement){
            user.setUserType(UserType.USER);
            currentUserID = user.getId();
            userRepository.save(user);
            System.out.println(user.getEmail() + ", " + user.getPassword() + ", " + user.getDob());
            response.sendRedirect("/");
        }
        else {
            System.out.println("email: " + emailExists + "\npps: " + ppsExists + "\nage: " + ageRequirement);
            response.sendRedirect("/signup");
        }
    }

    public boolean isOver18(Date dob){
        Date today = Calendar.getInstance().getTime();
        long d1 = TimeUnit.DAYS.convert(today.getTime(), TimeUnit.MILLISECONDS);
        long d2 = TimeUnit.DAYS.convert(dob.getTime(), TimeUnit.MILLISECONDS);
        long diff = d1 - d2;
        diff = diff/360;
        return diff > 17;
    }

    @PostMapping({"/login"})
    public void login_submit(User user, HttpServletResponse response) throws IOException {
        List<User> userList = userRepository.findByEmail(user.getEmail());
        if(!userList.isEmpty()){
            if(userList.get(0).getPassword().equals(user.getPassword())) {
                currentUserID = userList.get(0).getId();
                System.out.println("You logged in!!");
                response.sendRedirect("/");
            }
            else {
                System.out.println("Password wrong");
                response.sendRedirect("/login");
            }

        }
        else {
            System.out.println("Email wrong");
            response.sendRedirect("/login");
        }

    }

   /* // Get All Users
    @GetMapping("/users")
    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    // Create a new User
    @PostMapping("/users")
    public User newUser(@Valid @RequestBody User newUser) {
        return userRepository.save(newUser);
    }

    // Get a Single User
    @GetMapping("/users/{id}")
    public User getUserById(@PathVariable(value = "id") Long userId) throws UserNotFoundException {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    // Update an Existing User
    @PutMapping("/users/{id}")
    public User updateUser(@PathVariable(value="id") Long userId, @Valid @RequestBody User userDetails)
            throws UserNotFoundException{

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        user.setFirstName(userDetails.getFirstName());
        user.setSurname(userDetails.getSurname());

        return userRepository.save(user);
    }

    // Delete a User
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable(value="id") Long userId) throws UserNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        userRepository.delete(user);
        return ResponseEntity.ok().build();
    }*/
}
