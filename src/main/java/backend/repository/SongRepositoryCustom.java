package backend.repository;

import org.nd4j.shade.guava.collect.Lists;
import backend.domain.Song;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SongRepositoryCustom {
    public SongRepositoryCustom() {
    }

    public void exportLyricsToFile(Song song, String lyrics) {
        File file = new File("src/main/resources/lyrics/" + song.getArtist() + "-" + song.getTitle() + ".txt");
        try {
            if (!file.exists()) {

                if (file.createNewFile()) {
                    try (PrintWriter out = new PrintWriter(file)) {
                        out.print(lyrics);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
            else {
                try (PrintWriter out = new PrintWriter(file)) {
                    out.print(lyrics);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exportKeywordsToFile(Song song, List<String> keywords) {
        File file = new File("src/main/resources/keywords/" + song.getArtist() + "-" + song.getTitle() + ".txt");
        List<String> keywordsToWrite = splitKeywordsIntoWords(keywords);
        try {
            if (!file.exists()) {
                if (file.createNewFile()) {
                    try (PrintWriter out = new PrintWriter(file)) {
                        for (String keyword: keywordsToWrite) {
                            out.println(keyword);
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
            else {
                try (PrintWriter out = new PrintWriter(file)) {
                    for (String keyword: keywordsToWrite) {
                        out.println(keyword);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
}
