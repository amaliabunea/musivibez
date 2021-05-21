package backend.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
@Builder
public class PlaylistDTO {

    private String id;
    private String username;
    private String title;
}
