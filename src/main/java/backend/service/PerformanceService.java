package backend.service;

import lombok.Data;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import backend.domain.AccuracyHelper;
import backend.domain.Performance;
import backend.domain.Rating;
import backend.repository.RatingRepository;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Data
public class PerformanceService {

    private RatingRepository ratingRepository;
    private List<AccuracyHelper> accuracyHelpers = new ArrayList<>();
    private Performance doc2vecPerformance;
    private Performance word2vecPerformance;
    private Performance combinedPerformance;

    @Autowired
    public PerformanceService(RatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
        calculatePerformances(5);
    }

    private Map<Long, List<Rating>> getRatingsGroupedBySongId(String username, Long method) {
        List<Rating> ratings = ratingRepository.getByUser_UsernameAndMethod(username, method);
        return ratings.stream().collect(Collectors.groupingBy(r -> r.getSong().getId()));
    }

    private Map<String, List<Rating>> getRatingsGroupedByUser() {
        List<Rating> ratings = ratingRepository.findAll();
        return ratings.stream().collect(Collectors.groupingBy(r -> r.getUser().getUsername()));
    }

    private void computeRatingsVectors(String username, Long method, int noRecommendations) {
        accuracyHelpers = new ArrayList<>();
        Map<Long, List<Rating>> ratingsGroupedBySong = getRatingsGroupedBySongId(username, method);
        for (Long songId : ratingsGroupedBySong.keySet()) {
            List<Rating> ratings = ratingsGroupedBySong.get(songId);
            double[] e = new double[5];
            double[] x = new double[5];
            int i = 0;
            for (Rating rating : ratings) {
                e[i] = rating.getRatingAlg();
                x[i] = rating.getRatingUser();
                i++;
            }
            accuracyHelpers.add(new AccuracyHelper(username, method, songId, e, x));
        }

        if (noRecommendations < 5) {
            // transforming arrays
            for (AccuracyHelper ah : accuracyHelpers) {

                // reducing
                for (int i = 1; i <= 5 - noRecommendations; i++) {
                    double finalI = i;
                    int index = IntStream.range(0, ah.getE().length).filter(j -> ah.getE()[j] == finalI).findFirst().orElse(-1);
                    ah.setE(ArrayUtils.remove(ah.getE(), index));
                    ah.setX(ArrayUtils.remove(ah.getX(), index));
                }

                // transforming e
                double[] e = ah.getE();
                for (int i = 0; i < e.length; i++) {
                    e[i] = e[i] - (5 - noRecommendations);
                }
                ah.setE(e);

                // transforming x
                double[] x = ah.getX();
                for (int i = 0; i < x.length; i++) {
                    if (x[i] > noRecommendations) {
                        x[i] = noRecommendations;
                    }
                }
                ah.setX(x);
            }
        }
    }

    private double calculateAccuracyForMethod(Long method, int noRecommendations, String username) {
        List<Double> similarities = getSimilaritiesBetweenRatingsVectors(username, method, noRecommendations);
        OptionalDouble acc = similarities.stream().mapToDouble(s -> s).average();
        if (acc.isPresent())
            return acc.getAsDouble();
        return 0;
    }

    private List<Double> getSimilaritiesBetweenRatingsVectors(String username, Long method, int noRecommendations) {
        List<Double> similarities = new ArrayList<>();
        List<AccuracyHelper> helpersFilteredByUsernameAndMethod = accuracyHelpers
                .stream()
                .filter(h -> h.getUsername().equals(username) && h.getMethod().equals(method))
                .collect(Collectors.toList());

        for (AccuracyHelper ah : helpersFilteredByUsernameAndMethod) {
            double[] e = ah.getE();
            double[] x = ah.getX();

            double similarity = computeSimilarity(e, x, noRecommendations);

            similarities.add(similarity);
        }
        return similarities;
    }

    private double computeSimilarity(double[] e, double[] x, int n) {
        double similarity = 1.0;
        double absError = 0.0;
        for (int i = 0; i < n; i++) {
            absError += Math.abs(e[i] - x[i]);
        }
        if (n % 2 != 0) {
            similarity -= (2 * absError) / (Math.pow(n, 2) - 1);
        } else {
            similarity -= (2 * absError) / (Math.pow(n, 2));
        }
        return similarity;
    }

    private long getTotalNumberOfRecommendations(Long method, String username) {
        long result = accuracyHelpers.stream()
                .filter(ah -> ah.getUsername().equals(username) && ah.getMethod().equals(method))
                .count();
        return result;
    }

    private long getTotalNumberOfRecommendations(Long method) {
        long result = accuracyHelpers.stream()
                .filter(ah -> ah.getMethod().equals(method))
                .count();
        return result;
    }

    private int getNumberOfCorrectEvaluatedSongs(Long method, String username) {
        List<AccuracyHelper> accuracyHelpersFilteredByMethodAndUser = getAccuracyHelpersFilteredByMethodAndUsername(method, username);
        int correctEvaluations = 0;
        for (AccuracyHelper ah : accuracyHelpersFilteredByMethodAndUser) {
            correctEvaluations += getNumberOfEqualValues(ah.getE(), ah.getX());
        }
        return correctEvaluations;
    }

    private int getNumberOfCorrectEvaluatedSongs(Long method) {
        List<AccuracyHelper> accuracyHelpersFilteredByMethod = getAccuracyHelpersFilteredByMethod(method);
        int correctEvaluations = 0;
        for (AccuracyHelper ah : accuracyHelpersFilteredByMethod) {
            correctEvaluations += getNumberOfEqualValues(ah.getE(), ah.getX());
        }
        return correctEvaluations;
    }

    private List<AccuracyHelper> getAccuracyHelpersFilteredByMethodAndUsername(Long method, String username) {
        return accuracyHelpers.stream()
                .filter(ah -> ah.getMethod().equals(method) && ah.getUsername().equals(username))
                .collect(Collectors.toList());
    }

    private List<AccuracyHelper> getAccuracyHelpersFilteredByMethod(Long method) {
        return accuracyHelpers.stream()
                .filter(ah -> ah.getMethod().equals(method))
                .collect(Collectors.toList());
    }

    private int getWorstEvaluationsPerRecommendation(Long method, String username) {
        List<AccuracyHelper> accuracyHelpersFiltered = getAccuracyHelpersFilteredByMethodAndUsername(method, username);
        int count = 0;
        for (AccuracyHelper ah : accuracyHelpersFiltered) {
            double similarity = computeSimilarity(ah.getE(), ah.getX(), ah.getE().length);
            if (similarity == 0)
                count += 1;
        }
        return count;
    }

    private int getWorstEvaluationsPerRecommendation(Long method) {
        List<AccuracyHelper> accuracyHelpersFiltered = getAccuracyHelpersFilteredByMethod(method);
        int count = 0;
        for (AccuracyHelper ah : accuracyHelpersFiltered) {
            double similarity = computeSimilarity(ah.getE(), ah.getX(), ah.getE().length);
            if (similarity == 0)
                count += 1;
        }
        return count;
    }

    private int getBestEvaluationsPerRecommendation(Long method, String username) {
        List<AccuracyHelper> accuracyHelpersFiltered = getAccuracyHelpersFilteredByMethodAndUsername(method, username);
        int count = 0;
        for (AccuracyHelper ah : accuracyHelpersFiltered) {
            double similarity = computeSimilarity(ah.getE(), ah.getX(), ah.getE().length);
            if (similarity == 1)
                count += 1;
        }
        return count;
    }

    private int getBestEvaluationsPerRecommendation(Long method) {
        List<AccuracyHelper> accuracyHelpersFiltered = getAccuracyHelpersFilteredByMethod(method);
        int count = 0;
        for (AccuracyHelper ah : accuracyHelpersFiltered) {
            double similarity = computeSimilarity(ah.getE(), ah.getX(), ah.getE().length);
            if (similarity == 1)
                count += 1;
        }
        return count;
    }

    private int getNumberOfEqualValues(double[] x, double[] y) {
        int result = 0;
        for (int i = 0; i < x.length; i++) {
            if (x[i] == y[i])
                result++;
        }
        return result;
    }

    public void calculatePerformances(int noRecommendations) {
        Map<String, List<Rating>> ratingsGroupedByUser = getRatingsGroupedByUser();
        int count = ratingsGroupedByUser.keySet().size();
        double doc2vecAcc = 0, word2vecAcc = 0, combinedAcc = 0;
        int correctEvaluatedSongs1 = 0, worstEvaluationsPerRecommendation1 = 0, bestEvaluationPerRecommendation1 = 0;
        long totalRecommendations1 = 0;
        int correctEvaluatedSongs2 = 0, worstEvaluationsPerRecommendation2 = 0, bestEvaluationPerRecommendation2 = 0;
        long totalRecommendations2 = 0;
        int correctEvaluatedSongs3 = 0, worstEvaluationsPerRecommendation3 = 0, bestEvaluationPerRecommendation3 = 0;
        long totalRecommendations3 = 0;
        for (String username : ratingsGroupedByUser.keySet()) {
            long method = 1;
            computeRatingsVectors(username, method, noRecommendations);
            doc2vecAcc += calculateAccuracyForMethod(method, noRecommendations, username);
            correctEvaluatedSongs1 += getNumberOfCorrectEvaluatedSongs(method, username);
            worstEvaluationsPerRecommendation1 += getWorstEvaluationsPerRecommendation(method, username);
            bestEvaluationPerRecommendation1 += getBestEvaluationsPerRecommendation(method, username);
            totalRecommendations1 += getTotalNumberOfRecommendations(method, username);

            method = 2;
            computeRatingsVectors(username, method, noRecommendations);
            word2vecAcc += calculateAccuracyForMethod(method, noRecommendations, username);
            correctEvaluatedSongs2 += getNumberOfCorrectEvaluatedSongs(method, username);
            worstEvaluationsPerRecommendation2 += getWorstEvaluationsPerRecommendation(method, username);
            bestEvaluationPerRecommendation2 += getBestEvaluationsPerRecommendation(method, username);
            totalRecommendations2 += getTotalNumberOfRecommendations(method, username);

            method = 3;
            computeRatingsVectors(username, method, noRecommendations);
            combinedAcc += calculateAccuracyForMethod(method, noRecommendations, username);
            correctEvaluatedSongs3 += getNumberOfCorrectEvaluatedSongs(method, username);
            worstEvaluationsPerRecommendation3 += getWorstEvaluationsPerRecommendation(method, username);
            bestEvaluationPerRecommendation3 += getBestEvaluationsPerRecommendation(method, username);
            totalRecommendations3 += getTotalNumberOfRecommendations(method, username);
        }

        doc2vecAcc /= count;
        word2vecAcc /= count;
        combinedAcc /= count;

        doc2vecPerformance = new Performance(doc2vecAcc, correctEvaluatedSongs1, worstEvaluationsPerRecommendation1,
                bestEvaluationPerRecommendation1, totalRecommendations1, totalRecommendations1 * noRecommendations);
        word2vecPerformance = new Performance(word2vecAcc, correctEvaluatedSongs2, worstEvaluationsPerRecommendation2,
                bestEvaluationPerRecommendation2, totalRecommendations2, totalRecommendations2 * noRecommendations);
        combinedPerformance = new Performance(combinedAcc, correctEvaluatedSongs3, worstEvaluationsPerRecommendation3,
                bestEvaluationPerRecommendation3, totalRecommendations3, totalRecommendations3 * noRecommendations);
    }

    private void calculateStatistics(int noRecommendations, double doc2vecAcc, double word2vecAcc, double combinedAcc) {
        long method = 1;
        long totalRecommendations = getTotalNumberOfRecommendations(method);
        doc2vecPerformance = new Performance(doc2vecAcc, getNumberOfCorrectEvaluatedSongs(method),
                getWorstEvaluationsPerRecommendation(method),
                getBestEvaluationsPerRecommendation(method),
                totalRecommendations, totalRecommendations * noRecommendations);

        method = 2;
        totalRecommendations = getTotalNumberOfRecommendations(method);
        word2vecPerformance = new Performance(word2vecAcc, getNumberOfCorrectEvaluatedSongs(method),
                getWorstEvaluationsPerRecommendation(method),
                getBestEvaluationsPerRecommendation(method),
                totalRecommendations, totalRecommendations * noRecommendations);

        method = 3;
        totalRecommendations = getTotalNumberOfRecommendations(method);
        combinedPerformance = new Performance(combinedAcc, getNumberOfCorrectEvaluatedSongs(method),
                getWorstEvaluationsPerRecommendation(method),
                getBestEvaluationsPerRecommendation(method),
                totalRecommendations, totalRecommendations * noRecommendations);
    }

    public Performance getDoc2vecPerformance() {
        return doc2vecPerformance;
    }

    public void setDoc2vecPerformance(Performance doc2vecPerformance) {
        this.doc2vecPerformance = doc2vecPerformance;
    }

    public Performance getWord2vecPerformance() {
        return word2vecPerformance;
    }

    public void setWord2vecPerformance(Performance word2vecPerformance) {
        this.word2vecPerformance = word2vecPerformance;
    }

    public Performance getCombinedPerformance() {
        return combinedPerformance;
    }

    public void setCombinedPerformance(Performance combinedPerformance) {
        this.combinedPerformance = combinedPerformance;
    }
}
