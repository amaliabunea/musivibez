package backend.ml.RS;


import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.KeywordsResult;
import org.apache.commons.io.FileUtils;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.nd4j.linalg.io.ClassPathResource;
import org.nd4j.shade.guava.collect.Lists;
import backend.domain.Playlist;
import backend.domain.Song;
import backend.domain.SongToRecommend;
import backend.ml.NLU;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Word2VecRecommender {
    private Word2Vec word2VecModel;
    private Song song;
    private Playlist playlist;
    private List<String> keywords;
    private NLU nlu;
    private NavigableSet<RecommendedSong> recommendations = new TreeSet<>();
    private int noRecommendations;

    public Word2VecRecommender() {
        File gModel = null;
        try {
            gModel = new ClassPathResource("models/GoogleWord2vecModel.bin.gz").getFile();
            this.word2VecModel = WordVectorSerializer.readWord2VecModel(gModel);
            this.noRecommendations = 5;
            this.keywords = new ArrayList<>();
            this.nlu = new NLU();
//            exportKeywords();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setSong(Song song) {
        this.song = song;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = splitKeywordsIntoWords(keywords);
    }

    public List<String> splitKeywordsIntoWords(List<String> keywords) {
        Set<String> words = new HashSet<>();
        for (String keyword : keywords) {
            if (keyword.contains(" ")) {
                String[] parts = keyword.split(" ");
                for (String part : parts) {
                    words.add(part.trim());
                }
            } else {
                words.add(keyword);
            }
        }
        return Lists.newArrayList(words);
    }

    public void exportKeywords() throws IOException {
        File[] allfiles = new ClassPathResource("lyrics").getFile().listFiles();
        for (File f : allfiles) {
            if (f.getName().endsWith(".txt")) {
                String str = FileUtils.readFileToString(f, "UTF-8");
                BufferedWriter writer = new BufferedWriter(new FileWriter("D:\\Licenta\\LICENTA\\src\\main\\resources\\keywords\\" + f.getName()));
                List<String> keywordsToCompare = splitKeywordsIntoWords(nlu.getKeywords(str).stream().map(KeywordsResult::getText).collect(Collectors.toList()));
                for (String keyword : keywordsToCompare) {
                    writer.write(keyword);
                    writer.newLine();
                }
                writer.close();
            }
        }
    }

    public void recommendSongs() throws IOException {
        recommendations = new TreeSet<>();
        File[] allfiles = new ClassPathResource("keywords").getFile().listFiles();
        for (File f : allfiles) {
            String titleArtist = f.getName().substring(0, f.getName().indexOf(".txt"));
            String artist = titleArtist.split("-")[0];
            String title = titleArtist.substring(artist.length() + 1);
            if (f.getName().endsWith(".txt") && !f.getName().equals(song.getArtist() + "-" + song.getTitle() + ".txt") && !playlist.containsSong(artist, title)) {
                List<String> keywordsToCompare = new ArrayList<>();
                BufferedReader reader = new BufferedReader(new FileReader(f));
                String k = null;
                while ((k = reader.readLine()) != null) {
                    keywordsToCompare.add(k);
                }
                reader.close();
                double sumOfSimilarities = 0;
                double noSim = 0;
                for (String keyword : keywords) {
                    for (String keywordToCompare : keywordsToCompare) {
                        double sim = word2VecModel.similarity(keyword, keywordToCompare);
                        if (!Double.isNaN(sim)) {
                            sumOfSimilarities += sim;
                            noSim += 1;
                        }
                    }
                }
                double similarity = sumOfSimilarities / noSim;
                recommendations.add(new RecommendedSong(f.getName().substring(0, f.getName().indexOf(".txt")), similarity));
                if (recommendations.size() > this.noRecommendations)
                    recommendations.remove(recommendations.first());
            }
        }
    }

    public List<SongToRecommend> getRecommendedSongs() {
        recommendations = recommendations.descendingSet();
        List<SongToRecommend> res = new ArrayList<>();
        for (RecommendedSong song : recommendations) {
            res.add(new SongToRecommend(song.getTitleArtist(), "2"));
        }
        return res;
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }

    public Word2Vec getWord2VecModel() {
        return word2VecModel;
    }

    public double[] getVector(String title, String artist) throws IOException {
        File file = new ClassPathResource("keywords/" + artist + "-" + title + ".txt").getFile();
        List<String> keywords = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String k = null;
        while ((k = reader.readLine()) != null) {
            keywords.add(k);
        }
        reader.close();
        double[] keywordV, vector = new double[0];
        for (String keyword : keywords) {
            keywordV = word2VecModel.getWordVector(keyword);
            vector = new double[keywordV.length];
            for (int i = 0; i < keywordV.length; i++) {
                vector[i] += keywordV[i];
            }
        }
            for (int i = 0; i < vector.length; i++) {
                vector[i] = vector[i] / keywords.size();
            }
        return vector;
    }
}
