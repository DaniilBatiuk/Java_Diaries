package com.example.result.controllers;

import com.example.result.models.User;
import com.example.result.repositories.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import org.springframework.web.bind.annotation.CookieValue;
@Controller
public class MainController {
    @Autowired
    private UserRepository userRepository;
    private HttpServletResponse response;


    private boolean isAuthenticated(@CookieValue(value = "userId", required = false) String userId) {
        return userId != "";
    }

    @GetMapping("/")
    public String getHome(Model model, @CookieValue(value = "userId", required = false) String userId) {
        boolean authenticated = isAuthenticated(userId);
        model.addAttribute("authenticated", authenticated);
        return "home";
    }

    @GetMapping("/registration")
    public String getRegistration(@RequestParam(name = "text",required = false,defaultValue = "hello")String text,
                          Model model){
        model.addAttribute("user", new User());
        return "registration";
    }

    @GetMapping("/login")
    public String getLogin(@RequestParam(name = "text",required = false,defaultValue = "hello")String text,
                          Model model){
        model.addAttribute("user", new User());
        return "login";
    }

    @PostMapping("/registration")
    public String registerUser(@ModelAttribute("user") @Valid User user, BindingResult bindingResult) {

        if ("".equals(user.getEmail()) || "".equals(user.getUsername()) || "".equals(user.getPassword())) {
            bindingResult.rejectValue("password", "error.user", "All inputs must be fill");
            return "registration";
        }

        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            bindingResult.rejectValue("password", "error.user", "User with this email already exists");
            return "registration";
        }

        userRepository.save(user);
        return "redirect:/login";
    }



    @PostMapping("/login")
    public String postLogin(@ModelAttribute("user") User user, Model model, HttpServletResponse response) {
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());

        if (existingUser.isPresent() && existingUser.get().getPassword().equals(user.getPassword())) {
            Cookie cookie = new Cookie("userId", existingUser.get().getId().toString());
            cookie.setPath("/");
            response.addCookie(cookie);
            return "redirect:/diaries";
        } else {
            model.addAttribute("error", "Invalid email or password");
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("userId", null);
        cookie.setPath("/");
        response.addCookie(cookie);

        return "redirect:/";
    }
}
