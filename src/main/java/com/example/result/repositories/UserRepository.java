package com.example.result.repositories;

import com.example.result.models.Journal;
import com.example.result.models.User;
import org.springframework.data.repository.CrudRepository;
import java.util.Optional;
public interface UserRepository extends CrudRepository<User,Long> {
    Optional<User> findByEmail(String email);
}
