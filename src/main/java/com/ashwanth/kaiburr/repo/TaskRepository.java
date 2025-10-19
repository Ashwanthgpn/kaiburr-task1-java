package com.ashwanth.kaiburr.repo;

import com.ashwanth.kaiburr.domain.Task;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TaskRepository extends MongoRepository<Task, String> {
    List<Task> findByNameContainingIgnoreCase(String namePart);
}
