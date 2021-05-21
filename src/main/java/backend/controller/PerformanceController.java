package backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import backend.domain.Performance;
import backend.dto.PerformanceDTO;
import backend.service.PerformanceService;


@RequestMapping("/performance")
@RestController
@CrossOrigin
public class PerformanceController {

    private PerformanceService performanceService;

    @Autowired
    public PerformanceController(PerformanceService performanceService) {
        this.performanceService = performanceService;
    }

    @GetMapping(value="/doc2vec")
    public ResponseEntity<PerformanceDTO> getDoc2vecPerformance() {
        Performance performance = performanceService.getDoc2vecPerformance();
        PerformanceDTO performanceDTO = performanceDTOConverter(performance);
        return new ResponseEntity<>(performanceDTO, HttpStatus.OK);
    }

    @GetMapping(value="/word2vec")
    public ResponseEntity<PerformanceDTO> getWord2vecPerformance() {
        Performance performance = performanceService.getWord2vecPerformance();
        PerformanceDTO performanceDTO = performanceDTOConverter(performance);
        return new ResponseEntity<>(performanceDTO, HttpStatus.OK);
    }
    @GetMapping(value="/combined")
    public ResponseEntity<PerformanceDTO> getCombinedPerformance() {
        Performance performance = performanceService.getCombinedPerformance();
        PerformanceDTO performanceDTO = performanceDTOConverter(performance);
        return new ResponseEntity<>(performanceDTO, HttpStatus.OK);
    }

    private PerformanceDTO performanceDTOConverter(Performance performance) {
        return PerformanceDTO.builder()
                .accuracy(performance.getAccuracy())
                .bestRecommendations(performance.getBestRecommendations())
                .correctEvaluatedSongs(performance.getCorrectEvaluatedSongs())
                .totalRecommendations(performance.getTotalRecommendations())
                .totalSongsRecommended(performance.getTotalSongsRecommended())
                .worstRecommendations(performance.getWorstRecommendations())
                .build();
    }
}
