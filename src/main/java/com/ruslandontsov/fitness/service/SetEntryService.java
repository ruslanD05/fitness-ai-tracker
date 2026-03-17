package com.ruslandontsov.fitness.service;

import com.ruslandontsov.fitness.model.SetEntry;
import com.ruslandontsov.fitness.repository.SetEntryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SetEntryService {

    private final SetEntryRepository setRepository;

    public SetEntryService(SetEntryRepository setRepository) {
        this.setRepository = setRepository;
    }

    public SetEntry createSet(SetEntry set) {
        return setRepository.save(set);
    }

    public List<SetEntry> getSetsByWorkout(Long workoutId) {
        return setRepository.findByWorkoutId(workoutId);
    }
}