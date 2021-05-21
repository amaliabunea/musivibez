package backend.domain;

import lombok.*;

import java.io.Serializable;


@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
@Builder
public class Performance implements Serializable {
    private Double accuracy;
    private Integer correctEvaluatedSongs;
    private Integer worstRecommendations;
    private Integer bestRecommendations;
    private Long totalRecommendations;
    private Long totalSongsRecommended;
}
