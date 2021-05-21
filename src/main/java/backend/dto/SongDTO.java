package backend.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
@Builder
public class SongDTO {

    private String id;
    private String title;
    private String artist;
    private String youtubeId;
    private String thumbnail;
    private String channelTitle;
}
