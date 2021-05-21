package backend.domain;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(exclude = {"playlists", "ratings"})
@ToString(exclude = {"playlists", "ratings"})
public class User  {

    @Id
    private String username;
    private String password;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<Playlist> playlists;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private Set<Rating> ratings;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public User(String username) {
        this.username=username;
    }
    public User(String username, String password) {
        this.username=username;
        this.password=password;
    }
}
