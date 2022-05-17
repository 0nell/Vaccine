package org.hse.controller;

import org.hse.model.*;
import org.hse.repository.*;
import org.hse.validator.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Controller
public class UserController {
    private Logger logger = LoggerFactory.getLogger(UserController.class);
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
    @Autowired
    private UserValidator userValidator;

    private BCryptPasswordEncoder encoder= new BCryptPasswordEncoder();

    @EventListener(ApplicationReadyEvent.class)
    public void initialiseDatabaseValues() {
        if(userRepository.count() == 0)
        {
            userRepository.save(new User("Skete"," "," "," "," "," ","admin@admin.ie"," ",encoder.encode("iamadmin"),"ADMIN","true"));
        }
        if(centreRepository.count() == 0)
        {
            centreRepository.save(new Centre("HSE clinic North","1 Coolock road","84738291"));
            centreRepository.save(new Centre("HSE clinic South","1 Stillorgan road","84738292"));
            centreRepository.save(new Centre("HSE clinic East","1 Port road","84738293"));
            centreRepository.save(new Centre("HSE clinic West","1 Lucan road","84738294"));
            centreRepository.save(new Centre("HSE clinic Central","1 Connolly Street","84738295"));
        }
        logger.info("Application Database populated");
    }

    @RequestMapping({"/", "/home"})
    public String index(HttpServletResponse response, Authentication authentication) throws ParseException, IOException {
        int currentUserType = getCurrentAccountType(authentication);
        if(currentUserType == 1)
            response.sendRedirect("/admin");
        else if (currentUserType == 2)
            response.sendRedirect("/user");
        return "index.html";
    }

    @RequestMapping({"/book"})
    public String Book(Model model, HttpServletResponse response, Authentication authentication) throws IOException {
        int currentUserType = getCurrentAccountType(authentication);
        if(currentUserType != 2){
            response.sendRedirect("/");
            logger.warn("Attempted booking by non user");
        }
        model.addAttribute("centres", centreRepository.findAll());
        List<List<String>> dates = new ArrayList<>();
        for(Centre c: centreRepository.findAll()){
            dates.add(c.getBookedDates());
        }
        String min = "" + java.time.LocalDate.now();
        String max = String.valueOf(java.time.LocalDate.now().plusDays(14));
        User user = userRepository.findByUsername(((UserDetails)authentication.getPrincipal()).getUsername());
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
                //e.printStackTrace();
                logger.error("Booking-error retrieving valid second appointment time");
            }
        }
        model.addAttribute("min", min);
        model.addAttribute("max", max);
        model.addAttribute("dates", dates);
        return "Book.html";
    }

    @RequestMapping({"/signup"})
    public String signup(HttpServletResponse response, Model model, Authentication authentication){
        String max = String.valueOf(java.time.LocalDate.now().minusYears(18));
        if(getCurrentAccountType(authentication) != 0){
            try {
                response.sendRedirect("/");
            } catch (IOException e) {
                //e.printStackTrace();
                logger.warn("Redirect failure: Authenticated account attempting to access signup page");
            }
        }
        model.addAttribute("error", error);
        model.addAttribute("max", max);
        error = "";
        return "signup.html";
    }

    @RequestMapping({"/logoutComplete"})
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String xfHeader = request.getHeader("X-Forwarded-For");
        String ip = (xfHeader == null) ?request.getRemoteAddr():xfHeader.split(",")[0];

        logger.info("Successful Logout from IP: {"+ip+"}");
        response.sendRedirect("/login");
    }

    //neither
    @RequestMapping({"/login"})
    public String login(HttpServletResponse response,Authentication authentication, Model model, HttpServletRequest request)
    {
        if(getCurrentAccountType(authentication) != 0){
            try {
                response.sendRedirect("/");
            } catch (IOException e) {
               // e.printStackTrace();
                logger.warn("Redirect failure: Authenticated account attempting to access login page");
            }
        }
        Map<String, ?> inputFlashMap = RequestContextUtils.getInputFlashMap(request);
        if (inputFlashMap != null) {
            String error = (String) inputFlashMap.get("error");
            model.addAttribute("error", error);
        }
        return "login.html";
    }

    //either
    @RequestMapping({"/stats"})
    public String stats(Model model)
    {
        int avgAge=0, userCount=0;
        float maleCount=0;


        Set<String> countries = new HashSet<>();

        int fullVaccine = 0;

        for(User user : userRepository.findAll()) {
            if(user.getAuthority().equals("USER") && user.firstDose()) {       //user has had a vaccination
                userCount++;
                countries.add(user.getNationality());
                if(user.isMale()){maleCount++;}
                avgAge+= user.getAge();
            }
            if(user.vaccinated()){
                fullVaccine++;
            }
        }

        if(userCount == 0) {
            model.addAttribute("total", 0);
            model.addAttribute("age", "NA");
            model.addAttribute("malePer", "NA");
            model.addAttribute("femalePer", "NA");
            model.addAttribute("nationalities", 0);
            model.addAttribute("type", "NA");
            model.addAttribute("full", 0);
            return "stats.html";
        }

        avgAge = avgAge/userCount;
        int min = avgAge-(avgAge%10);
        String range = (min) + "-" + (min+10);
        String mostCommonShot;

        int p = 0;
        int m = 0;
        int vaccinationCount = 0;
        for(Appointment a : appointmentRepository.findAll()) {
            if(a.isReceived()) {
                vaccinationCount++;
                if (a.getVaccineType() == VaccineType.MODERNA)
                        m++;
                else
                    p++;
            }
        }

        if(p > m)
            mostCommonShot = "Pfizer";
        else if (m > p)
            mostCommonShot = "Moderna";
        else
            mostCommonShot = "Pfizer/Moderna";

        model.addAttribute("total", vaccinationCount);
        model.addAttribute("age", range);
        model.addAttribute("type", mostCommonShot);
        model.addAttribute("malePer", ""+Math.round((maleCount/userCount)*100));
        model.addAttribute("femalePer", ""+Math.round(100-((maleCount/userCount)*100)));
        model.addAttribute("nationalities", countries.size());
        model.addAttribute("full", fullVaccine);

        return "stats.html";
    }

    //either
    @RequestMapping({"/forum"})
    public String forum(Model model, Authentication authentication)
    {
        model.addAttribute("unanswered", questionRepository.findByAnswerIsNull());
        model.addAttribute("answered", questionRepository.findByAnswerIsNotNull());
        boolean admin = (getCurrentAccountType(authentication) == 1);
        model.addAttribute("isAdmin", admin);
        return "forum.html";
    }

    @RequestMapping({"/admin"})
    public String admin(Model model, HttpServletResponse response,Authentication authentication) throws IOException {
        if(getCurrentAccountType(authentication) != 1) {
            logger.warn("Attempted viewing of admin login page by non verified user");
            response.sendRedirect("/");
        }
        else {
            logger.info("Admin Landing page Accessed by {"+((UserDetails)authentication.getPrincipal()).getUsername()+"}");
            model.addAttribute("appointments", appointmentRepository.findAll());
        }
        return "admin.html";
    }

    //user
    @RequestMapping({"/cancel-appointment"})
    public void cancel(HttpServletResponse response,Authentication authentication) throws IOException {
        if(getCurrentAccountType(authentication) == 2) {
            String username = ((UserDetails)authentication.getPrincipal()).getUsername();
            User user = userRepository.findByUsername(username);
            if (!user.getAppointments().isEmpty()) {
                Appointment apt = user.getAppointments().remove(user.getAppointments().size()-1);
                appointmentRepository.delete(apt);
                Centre centre = centreRepository.getById(apt.getCentre().getId());
                centre.getAppointments().remove(apt);
                centreRepository.save(centre);
                userRepository.save(user);
                logger.info("Appointment cancelled for user {"+username+"}");
            }
            else
                logger.warn("Attempted appointment cancellation by user with no appointment {"+username+"}");
        }
        else
            logger.warn("Attempted appointment cancellation by non user");
        response.sendRedirect("/");
    }

    //user
    @RequestMapping({"/user"})
    public String userLanding(Model model, HttpServletResponse response,Authentication authentication) throws IOException {
        if(getCurrentAccountType(authentication) != 2) {
            response.sendRedirect("/");
            logger.warn("Attempted user landing page access by non user ");
        }
        User user = userRepository.findByUsername(((UserDetails)authentication.getPrincipal()).getUsername());
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
        logger.info("User Landing page accessed: {"+user.getUsername()+"}");
        return "User-Profile.html";
    }

    //neither
    @PostMapping({"/signup"})
    public void signup_submit(UserDto userDto, HttpServletResponse response, Authentication authentication, BindingResult bindingResult) throws IOException, ParseException {
        String redirect = "/";

        User userPersist = new User(userDto.getFirstName(),userDto.getSurname(),userDto.getDob(), userDto.getPpsn(), userDto.getAddress(),userDto.getPhoneNumber(),userDto.getUsername(), userDto.getNationality(), userDto.getPassword(), userDto.getAuthority(), userDto.getMale());
        if(getCurrentAccountType(authentication)==0) {
            userValidator.validate(userPersist,bindingResult);

            if (bindingResult.hasErrors()) {
                logger.warn("Potential Man in Middle attack altering signup user details or email already in use");
                redirect = "/signup";
            }
            else{
                userPersist.setAuthority("USER");
                userPersist.setPassword(new BCryptPasswordEncoder().encode(userDto.getPassword()));
                userRepository.save(userPersist);
                redirect = "/login";
                logger.info("New user created {"+userPersist.getUsername()+"}");
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

    @GetMapping({"/login-uperror"})
    public String loginError(HttpServletResponse response, RedirectAttributes redirectAttrs)//HttpServletRequest request, Model model)
    {
        redirectAttrs.addFlashAttribute("error","Invalid Username or Password");
        return "redirect:/login";
    }

    @GetMapping({"/login-blockerror"})
    public String loginError1(HttpServletResponse response, RedirectAttributes redirectAttrs)//HttpServletRequest request, Model model)
    {
        redirectAttrs.addFlashAttribute("error","IP blocked due to too many login attempts");
        return "redirect:/login";
    }

    //user
    @PostMapping({"/forum/question"})
    public void ask_question(String title, String question, HttpServletResponse response, Authentication authentication) throws IOException {
        String name = "Anonymous User";
        if(getCurrentAccountType(authentication)!=0)
            name  = userRepository.findByUsername(((UserDetails)authentication.getPrincipal()).getUsername()).getFirstName();
        question = question.replaceAll("[><=]+","");
        Question newQuestion = new Question(title, question, name);
        questionRepository.save(newQuestion);
        logger.info("New Question posted in forum by {"+name+"}");
        response.sendRedirect("/forum");
    }

    //admin
    @PostMapping({"/forum/answer"})
    public void answer_question(String answer, long questionId, HttpServletResponse response, Authentication authentication) throws IOException {
        if(getCurrentAccountType(authentication)==1)
        {
            String name  = userRepository.findByUsername(((UserDetails)authentication.getPrincipal()).getUsername()).getFirstName();
            Question question = questionRepository.findById(questionId);
            Answer newAnswer = new Answer(answer, name, question);
            question.setAnswer(newAnswer);
            answerRepository.save(newAnswer);
            questionRepository.save(question);
            logger.info("Question ("+questionId+") answered by admin {"+name+"}");
            response.sendRedirect("/forum");
        }
        else
        {
            logger.warn("Attempted answer of question by unauthorised user");
            response.sendRedirect("/forum");
        }

    }

    //not admin
    @PostMapping({"/book"})
    public void book_submit(String date, long centreId, HttpServletResponse response, Authentication authentication) throws IOException {
        String redirect = "/";
        if(getCurrentAccountType(authentication) == 2)
        {
            User user = userRepository.findByUsername(((UserDetails)authentication.getPrincipal()).getUsername());
            if(user.canBook()) {

                Centre centre = centreRepository.getById(centreId);
                if (appointmentRepository.findByAppointmentDateTimeAndCentre(date, centre).isEmpty()) {
                    boolean firstDose = user.getAppointments().isEmpty();
                    Appointment appointment = new Appointment(date, firstDose, user, centre);
                    user.getAppointments().add(appointment);
                    centre.getAppointments().add(appointment);
                    appointmentRepository.save(appointment);
                    userRepository.save(user);
                    centreRepository.save(centre);
                    logger.info("Booked new appointment for user {"+user.getUsername()+"} at centre ("+centreId+")");
                    redirect="/user";
                } else {
                    logger.warn("Attempted booking of invalid appointment time at a centre");
                    redirect="/book";
                }
            }
            else
                logger.warn("Attempted booking by user who has had 2 appointments");
        }
        else
            logger.warn("Attempted booking by non user");
        response.sendRedirect(redirect);
    }

    //admin
    @PostMapping({"/apply-dose"})
    public void applyDose(String vaccine, long appointmentId, HttpServletResponse response, Authentication authentication) throws IOException {
        if(getCurrentAccountType(authentication) == 1) {
            Appointment appointment = appointmentRepository.getById(appointmentId);
            appointment.setVaccineType(vaccine);
            appointment.setReceived(true);
            appointmentRepository.save(appointment);
            logger.info("Admin {"+((UserDetails)authentication.getPrincipal()).getUsername()+"} applied dose for appointment ("+appointmentId+")");
            if (appointment.isFirstDose()) {
                appointmentRepository.save(getSecondAppointment(appointment));
                logger.info("Second appointment booked for user {"+appointment.getUser().getUsername()+"}");
            }
            response.sendRedirect("/admin");
        }
        else
        {
            logger.warn("Attempted dose application for appoinment ("+appointmentId+") by non admin");
            response.sendRedirect("/");
        }
    }

    public Appointment getSecondAppointment(Appointment oldAppointment){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.setTime(new Date()); // Using today's date
        c.add(Calendar.DATE, 21); // Adding 5 days
        String date = sdf.format(c.getTime());
        return new Appointment(date, false, oldAppointment.getUser(), oldAppointment.getCentre());
    }


    /*
    0 === not logged in
    1 === logged in as admin
    2 === logged in as user
     */
    private int getCurrentAccountType(Authentication authentication)
    {
        /*if(!(authentication instanceof AnonymousAuthenticationToken))
        {
            if(authentication.getAuthorities().stream().anyMatch(grantedAuth -> grantedAuth.getAuthority().equals("ADMIN")))
                return 1;
            else
                return 2;
        }*/
        if(authentication != null)
        {
            if(authentication.getAuthorities().stream().anyMatch(grantedAuth -> grantedAuth.getAuthority().equals("ADMIN")))
                return 1;
            else
                return 2;
        }
        return 0;
    }


}
