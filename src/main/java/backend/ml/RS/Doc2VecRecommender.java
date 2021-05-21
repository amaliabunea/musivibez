package backend.ml.RS;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.text.documentiterator.FileDocumentIterator;
import org.deeplearning4j.text.documentiterator.LabelsSource;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.io.ClassPathResource;
import backend.domain.Playlist;
import backend.domain.Song;
import backend.domain.SongToRecommend;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class Doc2VecRecommender {
    private Song song;
    private Playlist playlist;
    private NavigableSet<RecommendedSong> recommendations = new TreeSet<>();
    private int noRecommendations;
    private ParagraphVectors vec;

    public void setSong(Song song) {
        this.song = song;
    }

    public Doc2VecRecommender() {
        try {
            calculateVectors();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.noRecommendations = 5;
    }

    public void calculateVectors() throws IOException {

//      trainModel();
        this.vec = WordVectorSerializer.readParagraphVectors("src/main/resources/models/doc2vecModel.zip");
    }

    public void trainModel() throws IOException {
        this.vec = null;
        ClassPathResource file = new ClassPathResource("lyrics");
        FileDocumentIterator iter2 = new FileDocumentIterator(file.getFile());

        TokenizerFactory tokenizerFactory = new DefaultTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());
        LabelsSource labelFormat = new LabelsSource("DOC_");

        ParagraphVectors vector = new ParagraphVectors.Builder()
                .learningRate(0.025)
                .minLearningRate(0.001)
                .batchSize(1000)
                .epochs(20)
                .iterate(iter2)
                .labelsSource(labelFormat)
                .trainWordVectors(true)
                .tokenizerFactory(tokenizerFactory)
                .build();

        vector.fit();
        this.vec = vector;
        WordVectorSerializer.writeParagraphVectors(vector, "vecModel.zip");
    }

    public void recommendSongs() throws IOException {

        recommendations = new TreeSet<>();
        File[] allfiles = new ClassPathResource("lyrics").getFile().listFiles();
        int index = getIndexOfCurrentSong();
        int currentIndex = 0;
        for (File f : allfiles) {
            String titleArtist = f.getName().substring(0, f.getName().indexOf(".txt"));
            String artist = titleArtist.split("-")[0];
            String title = titleArtist.substring(artist.length() + 1);
            if (f.getName().endsWith(".txt") && currentIndex != index && !playlist.containsSong(artist, title)) {
                recommendations.add(
                        new RecommendedSong(f.getName().substring(0, f.getName().indexOf(".txt")),
                                this.vec.similarity("DOC_" + index, "DOC_" + currentIndex)));
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
        for (RecommendedSong song : recommendations) {
            res.add(new SongToRecommend(song.getTitleArtist(), "1"));
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

    private int getIndexOfSong(String title, String artist) {
        try {
            File[] allfiles = new ClassPathResource("lyrics").getFile().listFiles();
            boolean found = false;
            int i = 0;
            while (i < allfiles.length && !found) {
                if (allfiles[i].getName().equals(artist + "-" + title + ".txt")) {
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

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }

    public ParagraphVectors getDoc2VecModel() {
        return vec;
    }

    public double[] getVector(String title, String artist) {
        return this.vec.getWordVector("DOC_" + getIndexOfSong(title, artist));
    }
}
