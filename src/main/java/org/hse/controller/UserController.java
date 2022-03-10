package org.hse.controller;

import org.hse.model.*;
import org.hse.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
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
    @Autowired
    QuestionRepository questionRepository;
    @Autowired
    AnswerRepository answerRepository;
    @Autowired
    CentreRepository centreRepository;
    @Autowired
    AppointmentRepository appointmentRepository;
    private long currentUserID = -1;

    @EventListener(ApplicationReadyEvent.class)
    public void initialiseDatabaseValues() {
        if(userRepository.count() == 0)
        {
            userRepository.save(new User("Skete",null,null,null,null,null,"admin@admin.ie",null,"iamadmin",UserType.ADMIN));
        }
        if(centreRepository.count() == 0)
        {
            centreRepository.save(new Centre("HSE clinic North","1 Coolock road","84738291"));
            centreRepository.save(new Centre("HSE clinic South","1 Stillorgan road","84738292"));
            centreRepository.save(new Centre("HSE clinic East","1 Port road","84738293"));
            centreRepository.save(new Centre("HSE clinic West","1 Lucan road","84738294"));
            centreRepository.save(new Centre("HSE clinic Central","1 Connolly Street","84738295"));
        }
    }

    @RequestMapping({"/", "/home"})
    public String index(HttpServletResponse response) throws ParseException, IOException {
        if(userRepository.findById(currentUserID) != null){
            response.sendRedirect("/user");
        }
        return "index.html";
    }

    @RequestMapping({"/book"})
    public String Book(Model model, HttpServletResponse response) throws IOException {
        if(userRepository.findById(currentUserID) == null){
            response.sendRedirect("/");
            return "index.html";
        }
        else if(!userRepository.findById(currentUserID).getAppointments().isEmpty()){
            response.sendRedirect("/user");
        }
        model.addAttribute("centres", centreRepository.findAll());
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
    public String forum(Model model)
    {
        model.addAttribute("unanswered", questionRepository.findByAnswerIsNull());
        model.addAttribute("answered", questionRepository.findByAnswerIsNotNull());
        return "forum.html";
    }

    @RequestMapping({"/user"})
    public String user(Model model, HttpServletResponse response) throws IOException {
        if(userRepository.findById(currentUserID) == null) {
            response.sendRedirect("/");
            return "index.html";
        }
        else {
            List<Appointment> userAppointments = userRepository.getById(currentUserID).getAppointments();
            String lastActivity = "";
            String bookMessage = "";

            if (userAppointments.isEmpty()) {
                lastActivity = "No recent Activity";
                bookMessage = "Book your first dose now";
            }
            else if (userAppointments.size() == 1) {
                if (userAppointments.get(0).isReceived()) {
                    lastActivity = "First Dose has been received, awaiting second dose";
                    bookMessage = "Book your second dose now";
                }
                else {
                    lastActivity = "First Dose has been booked";
                    bookMessage = "You can book your second dose after receiving first dose";
                }
            } else if (userAppointments.get(1).isReceived()) {
                lastActivity = "You are fully vaccinated";
                bookMessage = "You have no more vaccines to book";
            }

            model.addAttribute("lastActivity", lastActivity);
            model.addAttribute("bookMessage", bookMessage);

            return "User-Profile.html";
        }
    }

    @PostMapping({"/signup"})
    public void signup_submit(User user, HttpServletResponse response, Model model) throws IOException, ParseException {
        boolean emailNotExists = userRepository.findByEmail(user.getEmail()).isEmpty();
        boolean ppsNotExists = userRepository.findByPpsn(user.getPpsn()).isEmpty();
        Date d = new SimpleDateFormat("yyyy-MM-dd").parse(user.getDob());
        boolean ageRequirement = isOver18(d);

        if(emailNotExists && ppsNotExists && ageRequirement){
            user.setUserType(UserType.USER);
            currentUserID = userRepository.save(user).getId();
            System.out.println(user.getEmail() + ", " + user.getPassword() + ", " + user.getDob());
            response.sendRedirect("/user");
        }
        else {
            System.out.println("email: " + emailNotExists + "\npps: " + ppsNotExists + "\nage: " + ageRequirement);
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
                response.sendRedirect("/user");
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

    @PostMapping({"/forum/question"})
    public void ask_question(String title, String question, HttpServletResponse response) throws IOException {
        String name = "Anonymous User";
        if(currentUserID != -1)
            name  = userRepository.findById(currentUserID).getFirstName() + " " + userRepository.findById(currentUserID).getSurname();

        Question newQuestion = new Question(title, question, name);
        questionRepository.save(newQuestion);
        response.sendRedirect("/forum");
    }

    @PostMapping({"/forum/answer"})
    public void answer_question(String answer, long questionId, HttpServletResponse response) throws IOException {
        String name = "Anonymous User";
        if(currentUserID != -1)
            name  = userRepository.findById(currentUserID).getFirstName() + " " + userRepository.findById(currentUserID).getSurname();
        System.out.println(questionId);
        Question question = questionRepository.findById(questionId);
        Answer newAnswer = new Answer(answer, name, question);
        answerRepository.save(newAnswer);
        response.sendRedirect("/forum");
    }

    @PostMapping({"/book"})
    public void book_submit(String date, long centreId, HttpServletResponse response) throws IOException {
        User user = userRepository.getById(currentUserID);
        Centre centre = centreRepository.getById(centreId);
        boolean firstDose = user.getAppointments().isEmpty();
        Appointment appointment = new Appointment(date, firstDose, user, centre);
        System.out.println("User:\n" + user.getEmail() + ", " + user.getPassword() + ", " + user.getFirstName());
        System.out.println("\nAppointment:\n" + user.getAppointments().get(0).getCentre().getName());
        appointmentRepository.save(appointment);
        userRepository.save(user);
        response.sendRedirect("/user");
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
