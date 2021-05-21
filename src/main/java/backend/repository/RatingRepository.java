package backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import backend.domain.Rating;

import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    List<Rating> getByUser_UsernameAndSong_IdAndSongTitleAndSongArtistAndMethod(String username, Long id, String songTitle, String songArtist, Long  method);
    List<Rating> getByUser_UsernameAndMethod(String username, Long method);
}
