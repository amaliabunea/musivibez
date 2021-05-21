package backend.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
@Builder
public class RatingDTO {

    private String id;
    private String songTitle;
    private String songArtist;
    private String method;
    private String ratingAlg;
    private String ratingUser;
    private String username;
    private String songId;
}
