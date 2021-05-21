package backend.domain;

import lombok.*;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
@Builder
public class AccuracyHelper implements Serializable {
    private String username;
    private Long method;
    private Long songId;
    private double[] e;
    private double[] x;
}
