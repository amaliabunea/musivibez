package backend.service;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import backend.domain.Rating;
import backend.repository.RatingRepository;

import java.util.*;

@Service
@Data
public class RatingService {

    private RatingRepository ratingRepository;

    @Autowired
    public RatingService(RatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }

    public Rating addRating(Rating rating) {
        List<Rating> existingRatings = ratingRepository.getByUser_UsernameAndSong_IdAndSongTitleAndSongArtistAndMethod(rating.getUser().getUsername(), rating.getSong().getId(), rating.getSongTitle(), rating.getSongArtist(), rating.getMethod());
        if (existingRatings.size() != 0) {
            Rating existingRating = existingRatings.get(0);
            existingRating.setRatingUser((existingRating.getRatingUser() + rating.getRatingUser()) / 2);
            ratingRepository.save(existingRating);
            return existingRating;
        }
        ratingRepository.save(rating);
        return rating;
    }

    public List<Rating> getAllRatings() {
        return ratingRepository.findAll();
    }
}
