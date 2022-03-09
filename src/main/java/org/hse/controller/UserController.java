package org.hse.controller;

import org.hse.exception.UserNotFoundException;
import org.hse.model.User;
import org.hse.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Controller
public class UserController {

    @Autowired
    UserRepository userRepository;

    @RequestMapping({"/", "/home"})
    public String index()
    {
        return "index.html";
    }

    @RequestMapping({"/Book"})
    public String Book()
    {
        return "Book.html";
    }

    @RequestMapping({"/signup"})
    public String signup()
    {
        return "signup.html";
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
