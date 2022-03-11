package org.hse.controller;

import org.apache.catalina.startup.ClassLoaderFactory.Repository;
import org.hibernate.dialect.function.AvgWithArgumentCastFunction;
import org.hse.model.*;
import org.hse.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

@Controller
public class UserController {
    String error = "";

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
            userRepository.save(new User("Skete",null,null,null,null,null,"admin@admin.ie",null,"iamadmin",UserType.ADMIN,"true"));
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
        if(isLoggedIn()){
            if(isAdmin())
                response.sendRedirect("/admin");
            else
                response.sendRedirect("/user");
        }
        return "index.html";
    }

    @RequestMapping({"/book"})
    public String Book(Model model, HttpServletResponse response) throws IOException {
        if(!isLoggedIn() || isAdmin()){
            response.sendRedirect("/");
        }
        model.addAttribute("centres", centreRepository.findAll());
        List<List<String>> dates = new ArrayList<>();
        for(Centre c: centreRepository.findAll()){
            dates.add(c.getBookedDates());
        }
        String min = "" + java.time.LocalDate.now();
        String max = String.valueOf(java.time.LocalDate.now().plusDays(14));
        User user = userRepository.getById(currentUserID);
        if(user.firstBooked()){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cal = Calendar.getInstance();
            try {
                cal.setTime(sdf.parse(user.getAppointments().get(0).getAppointmentDateTime()));
                cal.add(Calendar.DATE, 21);
                min = sdf.format(cal.getTime());
                cal.add(Calendar.DATE, 365);
                max = sdf.format(cal.getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        model.addAttribute("min", min);
        model.addAttribute("max", max);
        model.addAttribute("dates", dates);
        return "Book.html";
    }

    @RequestMapping({"/signup"})
    public String signup(HttpServletResponse response, Model model){
        if(isLoggedIn()){
            try {
                response.sendRedirect("/");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        model.addAttribute("error", error);
        error = "";
        return "signup.html";
    }

    @RequestMapping({"/logout"})
    public void logout(HttpServletResponse response) throws IOException {
       currentUserID = -1;
       response.sendRedirect("/");
    }

    @RequestMapping({"/login"})
    public String login(HttpServletResponse response)
    {
        if(isLoggedIn()){
            try {
                response.sendRedirect("/");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "login.html";
    }

    @RequestMapping({"/stats"})
    public String stats(Model model)
    {
        int avgAge=0, userCount=0;
        float maleCount=0;
        Set<String> countries = new HashSet<>();



        for(User user : userRepository.findAll()) {
            if(user.getUserType() == UserType.USER && user.firstDose()) {       //user has had a vaccination
                userCount++;
                countries.add(user.getNationality());
                if(user.isMale()){maleCount++;}
                avgAge+= user.getAge();
            }
        }

        if(userCount == 0) {
            model.addAttribute("total", 0);
            model.addAttribute("age", "NA");
            model.addAttribute("malePer", "NA");
            model.addAttribute("femalePer", "NA");
            model.addAttribute("nationalities", 0);
            return "stats.html";
        }

        avgAge = avgAge/userCount;
        int min = avgAge-(avgAge%10);
        String range = (min) + "-" + (min+10);

        int vaccinationCount = 0;
        for(Appointment a : appointmentRepository.findAll()) {
            if(a.isReceived()) {
                vaccinationCount++;
            }
        }
        model.addAttribute("total", vaccinationCount);
        model.addAttribute("age", range);
        model.addAttribute("malePer", ""+(maleCount/userCount)*100);
        model.addAttribute("femalePer", ""+(100-((maleCount/userCount)*100)));
        model.addAttribute("nationalities", countries.size());

        return "stats.html";
    }

    @RequestMapping({"/forum"})
    public String forum(Model model)
    {
        model.addAttribute("unanswered", questionRepository.findByAnswerIsNull());
        model.addAttribute("answered", questionRepository.findByAnswerIsNotNull());
        boolean admin = false;
        if (isLoggedIn())
            admin = userRepository.findById(currentUserID).getUserType() == UserType.ADMIN;
        model.addAttribute("isAdmin", admin);
        return "forum.html";
    }

    @RequestMapping({"/admin"})
    public String admin(Model model, HttpServletResponse response) throws IOException {
        if(!isLoggedIn() || !isAdmin()) {
            response.sendRedirect("/");
        }
        else {
            model.addAttribute("appointments", appointmentRepository.findAll());
        }
        return "admin.html";
    }

    @RequestMapping({"/cancel-appointment"})
    public void cancel(HttpServletResponse response) throws IOException {
        User user = userRepository.getById(currentUserID);
        if (!user.getAppointments().isEmpty()) {
            Appointment apt = user.getAppointments().remove(user.getAppointments().size()-1);
            appointmentRepository.delete(apt);
            Centre centre = centreRepository.getById(apt.getCentre().getId());
            centre.getAppointments().remove(apt);
            centreRepository.save(centre);
            userRepository.save(user);
        }
        response.sendRedirect("/");
    }
    @RequestMapping({"/user"})
    public String user(Model model, HttpServletResponse response) throws IOException {
        if(!isLoggedIn()) {
            response.sendRedirect("/");
            return "index.html";
        }
        else {
            User user = userRepository.getById(currentUserID);
            List<Appointment> userAppointments = user.getAppointments();
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
                    model.addAttribute("dose", "Dose " + 1);
                    model.addAttribute("date", userAppointments.get(userAppointments.size()-1).getAppointmentDateTime());
                }
            } else if(userAppointments.size() == 2 && !userAppointments.get(1).isReceived()){
                lastActivity = "Second Dose has been booked";
                bookMessage = "You have no more vaccines to book";
                model.addAttribute("dose", "Dose " + 2);
                model.addAttribute("date", userAppointments.get(userAppointments.size()-1).getAppointmentDateTime());
            }
            else if (userAppointments.get(1).isReceived()) {
                lastActivity = "You are fully vaccinated";
                bookMessage = "You have no more vaccines to book";
            }

            model.addAttribute("lastActivity", lastActivity);
            model.addAttribute("bookMessage", bookMessage);

            return "User-Profile.html";
        }
    }

    @PostMapping({"/signup"})
    public void signup_submit(User user, HttpServletResponse response) throws IOException, ParseException {
        String redirect = "/";
        if(!isLoggedIn()) {
            boolean emailNotExists = userRepository.findByEmail(user.getEmail()).isEmpty();
            boolean ppsNotExists = userRepository.findByPpsn(user.getPpsn()).isEmpty();
            Date d = new SimpleDateFormat("yyyy-MM-dd").parse(user.getDob());
            boolean ageRequirement = isOver18(d);

            if(!emailNotExists || !ppsNotExists || !ageRequirement){
                if(!emailNotExists){
                    error += "That Email already belongs to an account,     ";
                    redirect = "/signup";
                }
                if (!ppsNotExists){
                    error += "That PPS already belongs to an account,   ";
                    redirect = "/signup";
                }
                if(!ageRequirement){
                    error += "\nYou cannot register if you are under 18";
                    redirect = "/signup";
                }
            }
            else {
                user.setUserType(UserType.USER);
                currentUserID = userRepository.save(user).getId();
                redirect = "/user";
            }
        }
        response.sendRedirect(redirect);
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
                response.sendRedirect("/");
            }
            else {
                response.sendRedirect("/login");
            }
        }
        else {
            response.sendRedirect("/login");
        }
    }

    @PostMapping({"/forum/question"})
    public void ask_question(String title, String question, HttpServletResponse response) throws IOException {
        String name = "Anonymous User";
        if(isLoggedIn())
            name  = userRepository.findById(currentUserID).getFirstName();

        Question newQuestion = new Question(title, question, name);
        questionRepository.save(newQuestion);
        response.sendRedirect("/forum");
    }

    @PostMapping({"/forum/answer"})
    public void answer_question(String answer, long questionId, HttpServletResponse response) throws IOException {
        if(isLoggedIn() && isAdmin())
        {
            String name  = userRepository.findById(currentUserID).getFirstName();
            Question question = questionRepository.findById(questionId);
            Answer newAnswer = new Answer(answer, name, question);
            question.setAnswer(newAnswer);
            answerRepository.save(newAnswer);
            questionRepository.save(question);
            response.sendRedirect("/forum");
        }
        else
            response.sendRedirect("/forum");
    }

    @PostMapping({"/book"})
    public void book_submit(String date, long centreId, HttpServletResponse response) throws IOException {
        if(isLoggedIn() && !isAdmin() && getUser().canBook()){
            User user = getUser();
            Centre centre = centreRepository.getById(centreId);
            if (appointmentRepository.findByAppointmentDateTimeAndCentre(date, centre).isEmpty()) {
                boolean firstDose = user.getAppointments().isEmpty();
                Appointment appointment = new Appointment(date, firstDose, user, centre);
                user.getAppointments().add(appointment);
                centre.getAppointments().add(appointment);
                appointmentRepository.save(appointment);
                userRepository.save(user);
                centreRepository.save(centre);
                response.sendRedirect("/user");
            } else {
                response.sendRedirect("/book");
            }
        }
        else
            response.sendRedirect("/");
    }

    @PostMapping({"/apply-dose"})
    public void applyDose(String vaccine, long appointmentId, HttpServletResponse response) throws IOException {
        if(isLoggedIn() && isAdmin()) {
            Appointment appointment = appointmentRepository.getById(appointmentId);
            appointment.setVaccineType(vaccine);
            appointment.setReceived(true);
            appointmentRepository.save(appointment);
            if (appointment.isFirstDose()) {
                appointmentRepository.save(getSecondAppointment(appointment));
            }
            response.sendRedirect("/admin");
        }
        else
            response.sendRedirect("/");
    }

    public Appointment getSecondAppointment(Appointment oldAppointment){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.setTime(new Date()); // Using today's date
        c.add(Calendar.DATE, 21); // Adding 5 days
        String date = sdf.format(c.getTime());
        return new Appointment(date, false, oldAppointment.getUser(), oldAppointment.getCentre());
    }

    public boolean isLoggedIn(){
        return userRepository.findById(currentUserID) != null;
    }

    public boolean isAdmin() {
        return userRepository.findById(currentUserID).getUserType() == UserType.ADMIN;
    }

    private User getUser(){
        return userRepository.findById(currentUserID);
    }
}
