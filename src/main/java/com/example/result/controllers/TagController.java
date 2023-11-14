package com.example.result.controllers;

import com.example.result.models.Journal;
import com.example.result.models.Tag;
import com.example.result.repositories.JournalRepository;
import com.example.result.repositories.TagRepository;
import com.example.result.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class TagController {
    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private JournalRepository journalRepository;
    private boolean isAuthenticated(@CookieValue(value = "userId", required = false) String userId) {
        return userId != null;
    }
    @GetMapping("/tagsCreate")
    public String getTagCreate(@RequestParam(name = "text",required = false,defaultValue = "hello")String text,
                                 Model model, @CookieValue(value = "userId", required = false) String userId){
        boolean authenticated = isAuthenticated(userId);
        model.addAttribute("authenticated", authenticated);
        model.addAttribute("tag", new Tag());
        return "tagsCreate";
    }

    @PostMapping("/tagsCreate")
    public String createJournal(Tag tag, BindingResult bindingResult) {
        if ("".equals(tag.getName())) {
            bindingResult.rejectValue("name", "error.tag", "Name input must be fill");
            return "tagsCreate";
        }
        List<Tag> searchResults = tagRepository.findByNameContainingIgnoreCase(tag.getName());
        if(!searchResults.isEmpty()){
            bindingResult.rejectValue("name", "error.tag", "Tag with this name is already exist");
            return "tagsCreate";
        }

        tagRepository.save(tag);
        return "redirect:/diaries";
    }

    @GetMapping("/addTags/{id}")
    public String getAddTags(@PathVariable Long id, Model model, @CookieValue(value = "userId", required = false) String userId){
        boolean authenticated = isAuthenticated(userId);
        model.addAttribute("authenticated", authenticated);
        Optional<Journal> journalOptional = journalRepository.findById(id);
        Iterable<Tag> tagsOptional = tagRepository.findAll();
        List<Tag> tagsOptional1 =new ArrayList<>();
        List<String> juarnalTagsName =new ArrayList<>();
        List<String> aa = new ArrayList<>();
        for (Tag tag : journalOptional.get().getTags()) {
            if(!juarnalTagsName.contains(tag.getName())){
                juarnalTagsName.add(tag.getName());
            }
        }
        for (Tag tag : tagsOptional) {
            if (!aa.contains(tag.getName())) {
                aa.add(tag.getName());
                if(!juarnalTagsName.contains(tag.getName())) {
                    tagsOptional1.add(tag);
                }
            }
        }
        if (journalOptional.isPresent()) {
            Journal journal = journalOptional.get();
            model.addAttribute("journal", journal);
            model.addAttribute("tags", tagsOptional1);
            return "addTags";
        } else {
            return "diaries";
        }
    }


    @PostMapping("/addTags/{id}/{journalId}")
    public String postAddTags(@PathVariable Long id, @PathVariable Long journalId, Model model) {
        Optional<Journal> journalOptional = journalRepository.findById(journalId);
        Optional<Tag> tagOptional = tagRepository.findById(id);
        Tag a = new Tag();
        a.setName(tagOptional.get().getName());
        Tag ss = tagRepository.save(a);
        if (journalOptional.isPresent() && tagOptional.isPresent()) {
            Journal journal = journalOptional.get();
            journal.getTags().add(ss);

            journalRepository.save(journal);

            return "redirect:/diaries";
        } else {
            return "redirect:/diaries";
        }
    }

}
