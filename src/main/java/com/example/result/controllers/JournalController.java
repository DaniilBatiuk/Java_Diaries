package com.example.result.controllers;
import com.example.result.models.Journal;
import com.example.result.repositories.JournalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
public class JournalController {

    @Autowired
    private JournalRepository journalRepository;

    @GetMapping("/diariesCreate")
    public String getNotesCreate(@RequestParam(name = "text",required = false,defaultValue = "hello")String text,
                                 Model model){
        model.addAttribute("journal", new Journal());
        return "diariesCreate";
    }


    @GetMapping("/update/{id}")
    public String editJournal(@PathVariable Long id, Model model) {
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
            if ((!(searchResults.get(0).getId() == updatedJournal.getId()) && searchResults.get(0).getTitle().equals(updatedJournal.getTitle()))) {
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


    @PostMapping("/diariesCreate")
    public String createJournal(Journal journal, BindingResult bindingResult) {
        if ("".equals(journal.getText()) || "".equals(journal.getTitle())) {
            bindingResult.rejectValue("photoLink", "error.journal", "Text input and Title input must be fill");
            return "diariesCreate";
        }
        List<Journal> searchResults = journalRepository.findByTitleContainingIgnoreCase(journal.getTitle());
        if(!searchResults.isEmpty()){
            bindingResult.rejectValue("photoLink", "error.journal", "Diary with this title is already exist");
            return "diariesCreate";
        }

        journalRepository.save(journal);
        return "redirect:/diaries";
    }

    @GetMapping("/diaries")
    public String getJournalList(Model model) {
        Iterable<Journal> journals = journalRepository.findAll();
        model.addAttribute("journals", journals);
        return "diaries";
    }


    @GetMapping("/diaries/{title}")
    public String searchJournals(@RequestParam(name = "title", required = false) String title, Model model) {
        List<Journal> searchResults;

        if (title != null && !title.isEmpty()) {
            searchResults = journalRepository.findByTitleContainingIgnoreCase(title);
        } else {
            searchResults = (List<Journal>) journalRepository.findAll();
            model.addAttribute("journals", searchResults);
            return "redirect:/diaries";
        }

        model.addAttribute("journals", searchResults);
        return "search";
    }
}
