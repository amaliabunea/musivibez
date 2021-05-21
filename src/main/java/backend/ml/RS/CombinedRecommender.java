package backend.ml.RS;

import org.apache.commons.lang.ArrayUtils;
import org.nd4j.linalg.io.ClassPathResource;
import backend.domain.Playlist;
import backend.domain.Song;
import backend.domain.SongToRecommend;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CombinedRecommender {

    private Doc2VecRecommender doc2VecRecommender;
    private Word2VecRecommender word2VecRecommender;
    private Song song;
    private Playlist playlist;
    private NavigableSet<RecommendedSong> recommendations = new TreeSet<>();
    private int noRecommendations;

    public CombinedRecommender(Doc2VecRecommender doc2VecRecommender, Word2VecRecommender word2VecRecommender) {
        this.doc2VecRecommender = doc2VecRecommender;
        this.word2VecRecommender = word2VecRecommender;
        noRecommendations = 5;
    }

    public void setSong(Song song) {
        this.song = song;
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }

    public double cosineSimilarity(double[] vectorA, double[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    public void recommendSongs() throws IOException {
        recommendations = new TreeSet<>();
        File[] allfiles = new ClassPathResource("lyrics").getFile().listFiles();
        int index = getIndexOfCurrentSong();
        double[] vector = ArrayUtils.addAll(word2VecRecommender.getVector(
                song.getTitle(), song.getArtist()), doc2VecRecommender.getVector(song.getTitle(), song.getArtist()));
        int currentIndex = 0;
        for (File f : allfiles) {
            String titleArtist = f.getName().substring(0, f.getName().indexOf(".txt"));
            String artist = titleArtist.split("-")[0];
            String title = titleArtist.substring(artist.length() + 1);
            if (f.getName().endsWith(".txt") && currentIndex != index && !playlist.containsSong(artist, title)) {
                double[] vector2 = ArrayUtils.addAll(
                        word2VecRecommender.getVector(title, artist), doc2VecRecommender.getVector(title, artist));
                recommendations.add(new RecommendedSong(
                        f.getName().substring(0, f.getName().indexOf(".txt")), this.cosineSimilarity(vector, vector2)));
                if (recommendations.size() > this.noRecommendations) {
                    recommendations.remove(recommendations.first());
                }
            }
            currentIndex++;
        }
    }

    public List<SongToRecommend> getRecommendedSongs() {
        recommendations = recommendations.descendingSet();
        List<SongToRecommend> res = new ArrayList<>();
        for (RecommendedSong recommendedSong : recommendations) {
            res.add(new SongToRecommend(recommendedSong.getTitleArtist(), "3"));
        }
        return res;
    }

    private int getIndexOfCurrentSong() {
        try {
            File[] allfiles = new ClassPathResource("lyrics").getFile().listFiles();
            boolean found = false;
            int i = 0;
            while (i < allfiles.length && !found) {
                if (allfiles[i].getName().equals(song.getArtist() + "-" + song.getTitle() + ".txt")) {
                    found = true;
                }
                else {
                    i++;
                }
            }
            if (found) {
                return i;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

}
