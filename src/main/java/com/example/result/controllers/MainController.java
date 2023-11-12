package com.example.result.controllers;

import com.example.result.models.User;
import com.example.result.repositories.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Objects;
import java.util.Optional;

@Controller
public class MainController {
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String getHome(@RequestParam(name = "text",required = false,defaultValue = "hello")String text,
                          Model model){
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
}
