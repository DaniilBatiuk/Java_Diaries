package com.example.result.repositories;

import com.example.result.models.Tag;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Set;

public interface TagRepository extends CrudRepository<Tag,Long> {
    List<Tag> findByNameContainingIgnoreCase(String name);

}
