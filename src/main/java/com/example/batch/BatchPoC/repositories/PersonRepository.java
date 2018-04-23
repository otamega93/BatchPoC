package com.example.batch.BatchPoC.repositories;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.example.batch.BatchPoC.entities.Person;

@Repository
public interface PersonRepository extends PagingAndSortingRepository<Person, Long> {

}
