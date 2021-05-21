package backend.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
@Builder
public class SongToRecommendDTO {

    private String title;
    private String method;
}
