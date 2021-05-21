package backend.domain;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Rating implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String songTitle;
    private String songArtist;
    private Long method;
    private double ratingAlg;
    private double ratingUser;

    @ManyToOne
    private User user;

    @ManyToOne
    private Song song;

    public Rating(String songTitle, String songArtist, Long method, double ratingAlg, double ratingUser, User user, Song song) {
        this.songTitle = songTitle;
        this.songArtist = songArtist;
        this.method = method;
        this.ratingAlg = ratingAlg;
        this.ratingUser = ratingUser;
        this.user = user;
        this.song = song;
    }
}
