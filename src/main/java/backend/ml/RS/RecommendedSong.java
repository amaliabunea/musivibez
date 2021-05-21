package backend.ml.RS;

class RecommendedSong implements Comparable<RecommendedSong>{
    private String titleArtist;
    private Double similarityValue;

    public RecommendedSong(String titleArtist, Double similarityValue) {
        this.titleArtist = titleArtist;
        this.similarityValue = similarityValue;
    }

    @Override
    public int compareTo(RecommendedSong o) {
        return Double.compare(similarityValue, o.similarityValue);
    }

    public String getTitleArtist() {
        return titleArtist;
    }

    public Double getSimilarityValue() {
        return similarityValue;
    }
}
