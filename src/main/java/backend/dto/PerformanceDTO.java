package backend.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
@Builder
public class PerformanceDTO {

    private Double accuracy;
    private Integer correctEvaluatedSongs;
    private Integer worstRecommendations;
    private Integer bestRecommendations;
    private Long totalRecommendations;
    private Long totalSongsRecommended;
}
