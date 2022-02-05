package com.shishodia.kafka.libraryeventsconsumer.jpa;

import com.shishodia.kafka.libraryeventsconsumer.entity.LibraryEvent;

import org.springframework.data.repository.CrudRepository;

public interface LibraryEventsRepository extends CrudRepository<LibraryEvent, Integer> {
}
