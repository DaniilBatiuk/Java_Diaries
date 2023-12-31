package com.example.result.controllers;
import com.example.result.models.Journal;
import com.example.result.models.Tag;
import com.example.result.models.User;
import com.example.result.repositories.JournalRepository;
import com.example.result.repositories.UserRepository;
import com.fasterxml.jackson.databind.util.ArrayIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
public class JournalController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JournalRepository journalRepository;

    @GetMapping("/diariesCreate")
    public String getNotesCreate(@RequestParam(name = "text",required = false,defaultValue = "hello")String text,
                                 Model model, @CookieValue(value = "userId", required = false) String userId){
        boolean authenticated = isAuthenticated(userId);
        model.addAttribute("authenticated", authenticated);
        model.addAttribute("journal", new Journal());
        return "diariesCreate";
    }


    @GetMapping("/update/{id}")
    public String editJournal(@PathVariable Long id, Model model, @CookieValue(value = "userId", required = false) String userId){
        boolean authenticated = isAuthenticated(userId);
        model.addAttribute("authenticated", authenticated);
        Optional<Journal> journalOptional = journalRepository.findById(id);
        if (journalOptional.isPresent()) {
            Journal journal = journalOptional.get();
            model.addAttribute("journal", journal);
            return "update";
        } else {
            return "diaries";
        }
    }

    @PostMapping("/update/{id}")
    public String updateJournal(@PathVariable Long id, @ModelAttribute("journal") Journal updatedJournal, BindingResult bindingResult, Model model) {
        Optional<Journal> existingJournalOptional = journalRepository.findById(id);
        if ("".equals(updatedJournal.getText()) || "".equals(updatedJournal.getTitle())) {
            bindingResult.rejectValue("photoLink", "error.journal", "Text input and Title input must be filled");
            model.addAttribute("journal", updatedJournal);
            return "update";
        }
        List<Journal> searchResults = journalRepository.findByTitleContainingIgnoreCase(updatedJournal.getTitle());
        if (!searchResults.isEmpty()) {
            if ((searchResults.get(0).getId().longValue() != updatedJournal.getId().longValue()) && searchResults.get(0).getTitle().equals(updatedJournal.getTitle())) {
                bindingResult.rejectValue("photoLink", "error.journal", "Diary with this title is already exist");
                model.addAttribute("journal", updatedJournal);
                return "update";
            }
        }

        if (existingJournalOptional.isPresent()) {
            Journal existingJournal = existingJournalOptional.get();
            existingJournal.setTitle(updatedJournal.getTitle());
            existingJournal.setText(updatedJournal.getText());
            existingJournal.setPhotoLink(updatedJournal.getPhotoLink());
            journalRepository.save(existingJournal);
        }
        return "redirect:/diaries";
    }

    @PostMapping("/delete/{id}")
    public String deleteJournal(@PathVariable Long id) {
        journalRepository.deleteById(id);
        return "redirect:/diaries";
    }

    @GetMapping("/journal/{id}")
    public String Journal(@PathVariable Long id, Model model, @CookieValue(value = "userId", required = false) String userId){
        boolean authenticated = isAuthenticated(userId);
        model.addAttribute("authenticated", authenticated);
        Optional<Journal> a = journalRepository.findById(id);
        model.addAttribute("journal", a);
        return "journal";
    }


    @PostMapping("/diariesCreate")
    public String createJournal(Journal journal, BindingResult bindingResult, @CookieValue(value = "userId", required = false) String userId) {
        Optional<User> existingJournalOptional = userRepository.findById(Long.valueOf(userId));
        if ("".equals(journal.getText()) || "".equals(journal.getTitle())) {
            bindingResult.rejectValue("photoLink", "error.journal", "Text input and Title input must be fill");
            return "diariesCreate";
        }
        List<Journal> searchResults = journalRepository.findByTitleContainingIgnoreCase(journal.getTitle());
        if(!searchResults.isEmpty()){
            bindingResult.rejectValue("photoLink", "error.journal", "Diary with this title is already exist");
            return "diariesCreate";
        }
        journal.setOwner(existingJournalOptional.get());
        journalRepository.save(journal);
        return "redirect:/diaries";
    }

    @GetMapping("/diaries")
    public String getJournalList(Model model, @CookieValue(value = "userId", required = false) String userId){
        boolean authenticated = isAuthenticated(userId);
        model.addAttribute("authenticated", authenticated);
        Iterable<Journal> journals = journalRepository.findByOwner_Id(Long.valueOf(userId));
        LocalDate currentDate = LocalDate.now();

        for (Journal journal : journals) {
            if (journal.isArchived()) {
                if(journal.getArchiveTimestamp().isBefore(currentDate)){
                    journalRepository.delete(journal);
                }
            }
        }

        List<Journal> updatedJournals = journalRepository.findByOwner_Id(Long.valueOf(userId));
        List<Journal> allJournals = (List<Journal>) journalRepository.findAll();
        for (Journal journal : allJournals) {
            if (journal.getCollaborator() != null && journal.getCollaborator().getId() == Long.valueOf(userId)){
                updatedJournals.add(journal);
            }
        }
        model.addAttribute("journals", updatedJournals);
        return "diaries";
    }


    @GetMapping("/diaries/{title}")
    public String searchJournals(@RequestParam(name = "title", required = false) String title, Model model, @CookieValue(value = "userId", required = false) String userId){
        boolean authenticated = isAuthenticated(userId);
        model.addAttribute("authenticated", authenticated);
        List<Journal> byOwn = journalRepository.findByOwner_Id(Long.valueOf(userId));
        List<Journal> allJournals = (List<Journal>) journalRepository.findAll();
        List<Journal> searchResults = new ArrayList<Journal>();
        List<Journal> res = new ArrayList<Journal>();
        for (Journal journal : allJournals) {
            if (journal.getCollaborator() != null && journal.getCollaborator().getId() == Long.valueOf(userId)){
                byOwn.add(journal);
            }
        }

        if (title != null && !title.isEmpty()) {
            searchResults = journalRepository.findByTitleContainingIgnoreCase(title);
            for (Journal journal : byOwn) {
                if (searchResults.contains(journal)){
                    res.add(journal);
                }
            }
        } else {
            model.addAttribute("journals", byOwn);
            return "redirect:/diaries";
        }

        model.addAttribute("journals", res);
        return "search";
    }

    @GetMapping("/unarchive/{id}")
    public String getUnArchive(@PathVariable Long id, Model model, @CookieValue(value = "userId", required = false) String userId){
        boolean authenticated = isAuthenticated(userId);
        model.addAttribute("authenticated", authenticated);
        Optional<Journal> journalOptional = journalRepository.findById(id);
        if (journalOptional.isPresent()) {
            Journal journal = journalOptional.get();
            journal.setArchived(false);
            journal.setArchiveTimestamp(null);
            journalRepository.save(journal);
            return "redirect:/diaries";
        } else {
            return "redirect:/diaries";
        }
    }

    @GetMapping("/archive/{id}")
    public String getArchive(@PathVariable Long id, Model model, @CookieValue(value = "userId", required = false) String userId){
        boolean authenticated = isAuthenticated(userId);
        model.addAttribute("authenticated", authenticated);
        Optional<Journal> journalOptional = journalRepository.findById(id);
        if (journalOptional.isPresent()) {
            Journal journal = journalOptional.get();
            model.addAttribute("journal", journal);
            return "archive";
        } else {
            return "archive";
        }
    }

    @PostMapping("/archive/{id}")
    public String archiveJournal(@PathVariable Long id, @ModelAttribute("journal") Journal updatedJournal, BindingResult bindingResult, Model model) {
        Optional<Journal> existingJournalOptional = journalRepository.findById(id);

        if (existingJournalOptional.isPresent()) {
            Journal existingJournal = existingJournalOptional.get();
            existingJournal.setArchived(true);
            existingJournal.setArchiveTimestamp(updatedJournal.getArchiveTimestamp());
            journalRepository.save(existingJournal);
        }
        return "redirect:/diaries";
    }
    private boolean isAuthenticated(@CookieValue(value = "userId", required = false) String userId) {
        return userId != null;
    }


    @GetMapping("/addCollaborant/{id}")
    public String addCollaborant(@PathVariable Long id, Model model, @CookieValue(value = "userId", required = false) String userId){
        boolean authenticated = isAuthenticated(userId);
        model.addAttribute("authenticated", authenticated);

        Optional<Journal> journalOptional = journalRepository.findById(id);
        Iterable<User> users = userRepository.findAll();
        List<User> users2 = new ArrayList<User>();

        for (User user : users) {
            if (user.getId() != Long.valueOf(userId)) {
                users2.add(user);
            }
        }

        if (journalOptional.isPresent()) {
            Journal journal = journalOptional.get();
            model.addAttribute("journal", journal);
            model.addAttribute("users", users2);
            return "addcollaborant";
        } else {
            return "diaries";
        }
    }

    @PostMapping("/addCollaborant/{id}/{journalId}")
    public String postAddTags(@PathVariable Long id, @PathVariable Long journalId, Model model) {
        Optional<Journal> journalOptional = journalRepository.findById(journalId);
        Optional<User> user = userRepository.findById(id);

        if (journalOptional.isPresent()) {
            Journal journal = journalOptional.get();
            journal.setCollaborator(user.get());

            journalRepository.save(journal);

            return "redirect:/diaries";
        } else {
            return "redirect:/diaries";
        }
    }

}
