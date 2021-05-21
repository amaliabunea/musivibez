package backend.domain;


import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"songs"})
@Getter
@Builder
public class Playlist implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @ManyToMany(cascade = {CascadeType.MERGE})
    private Set<Song> songs = new HashSet<>();

    @ManyToOne
    private User user;

    public Playlist(String name, User user) {
        this.title = name;
        this.user = user;
    }

    public void addSong(Song song) {
        songs.add(song);
    }

    public void removeSong(Song song) {
        songs.remove(song);
    }

    public boolean containsSong(String artist, String title) {
        boolean found = false;
        for (Song song : songs) {
            if (song.getArtist().equals(artist) && song.getTitle().equals(title)) {
                found = true;
                break;
            }
        }
        return found;
    }
}
