package backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import backend.domain.Song;

import java.util.List;
import java.util.Optional;

public interface SongRepository extends JpaRepository<Song, Long> {

    void deleteById(Long id);
}
