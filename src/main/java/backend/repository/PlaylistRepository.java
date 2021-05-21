package backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import backend.domain.Playlist;
import backend.domain.Song;

import java.util.List;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    @Query("select p.songs from Playlist p where p.id=:id")
    List<Song> getSongsByPlaylistId(@Param("id") Long id);

    List<Playlist> getByUser_Username(String username);

    List<Playlist> getByUser_UsernameAndTitleContains(String username, String q);

}

