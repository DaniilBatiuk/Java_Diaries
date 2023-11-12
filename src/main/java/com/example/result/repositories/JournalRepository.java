package com.example.result.repositories;

import com.example.result.models.Journal;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface JournalRepository extends CrudRepository<Journal,Long> {
    List<Journal> findByTitleContainingIgnoreCase(String title);
}