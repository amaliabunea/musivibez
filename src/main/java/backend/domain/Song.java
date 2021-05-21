package backend.domain;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString(exclude = "playlists")
@Builder
public class Song implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String artist;
    private String youtubeId;
    private String thumbnail;
    private String channelTitle;

    @ManyToMany(mappedBy = "songs")
    private Set<Playlist> playlists = new HashSet<>();

    @OneToMany(mappedBy = "song", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Set<Rating> ratings;


    public Song(String title, String artist, String youtubeId, String thumbnail, String channelTitle) {
        this.title = title;
        this.artist = artist;
        this.youtubeId = youtubeId;
        this.thumbnail = thumbnail;
        this.channelTitle = channelTitle;
    }

}
