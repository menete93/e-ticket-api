package mz.co.mozbuy.e_ticket.event.core.service;

import lombok.RequiredArgsConstructor;
import mz.co.mozbuy.e_ticket.event.core.model.VenueEntity;
import mz.co.mozbuy.e_ticket.event.core.repository.VenueRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VenueService {

    private final VenueRepository venueRepository;

    public VenueEntity create(VenueEntity venue) {
        return venueRepository.save(venue);
    }

    public VenueEntity update(VenueEntity venue) {
        return venueRepository.save(venue);
    }

    public Optional<VenueEntity> findById(Long id) {
        return venueRepository.findById(id);
    }

    public List<VenueEntity> findAll() {
        return venueRepository.findAll();
    }

    public void delete(Long id) {
        venueRepository.deleteById(id);
    }
}
