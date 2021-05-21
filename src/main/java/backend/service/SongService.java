package backend.service;

import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.*;
import lombok.Data;
import org.jmusixmatch.MusixMatch;
import org.jmusixmatch.MusixMatchException;
import org.jmusixmatch.entity.lyrics.Lyrics;
import org.jmusixmatch.entity.track.Track;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import backend.domain.Playlist;
import backend.domain.Song;
import backend.domain.SongToRecommend;
import backend.ml.NLU;
import backend.ml.RS.CombinedRecommender;
import backend.ml.RS.Doc2VecRecommender;
import backend.ml.RS.Word2VecRecommender;
import backend.repository.PlaylistRepository;
import backend.repository.SongRepository;
import backend.repository.SongRepositoryCustom;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Data
public class SongService {

    private SongRepository songRepository;
    private SongRepositoryCustom songRepositoryCustom;
    private PlaylistRepository playlistRepository;
    private NLU nlu;
    private Doc2VecRecommender doc2VecRecommender;
    private Word2VecRecommender word2VecRecommender;
    private CombinedRecommender combinedRecommender;

    @Autowired
    public SongService(SongRepository songRepository, PlaylistRepository playlistRepository) {
        this.songRepository = songRepository;
        this.playlistRepository = playlistRepository;
        this.songRepositoryCustom = new SongRepositoryCustom();
        this.nlu = new NLU();
        this.doc2VecRecommender = new Doc2VecRecommender();
        this.word2VecRecommender = new Word2VecRecommender();
        this.combinedRecommender = new CombinedRecommender(this.doc2VecRecommender, this.word2VecRecommender);
    }

    public void addSongToPlaylist(Song song, Long playlistId) {
        Playlist playlist = playlistRepository.getOne(playlistId);
        playlist.addSong(song);
        playlistRepository.save(playlist);
        try {
            String lyrics = getLyricsText(song);
            songRepositoryCustom.exportLyricsToFile(song, lyrics);
            songRepositoryCustom.exportKeywordsToFile(song,
                    getKeywords(lyrics).stream().map(KeywordsResult::getText).collect(Collectors.toList()));
        } catch (MusixMatchException e) {
            e.printStackTrace();
        }
    }

    public void deleteSongFromPlaylist(Song song, Playlist playlist) {
        playlist.removeSong(song);
        playlistRepository.save(playlist);
        songRepository.deleteById(song.getId());
    }

    public Song getSongFromPlaylist(Playlist playlist, long idSong) {
        Set<Song> songs = playlist.getSongs();
        Song song = null;
        for (Song s : songs) {
            if (s.getId() == idSong) {
                song = s;
            }
        }
        return song;
    }

    public String getLyricsText(Song song) throws MusixMatchException, IllegalStateException {
        String apikey = "6e01dac3dc64a724788de87f15d7f89b";
        MusixMatch musixMatch = new MusixMatch(apikey);
        Track track = musixMatch.getMatchingTrack(song.getTitle(), song.getArtist());
        Lyrics lyrics = musixMatch.getLyrics(track.getTrack().getTrackId());
        String lyricsText = lyrics.getLyricsBody();
        return lyricsText.substring(0, lyricsText.indexOf("******* This Lyrics"));
    }

    public String getEmotions(String songLyrics) {
        return nlu.getEmotions(songLyrics);
    }

    public List<KeywordsResult> getKeywords(String text) {
        return nlu.getKeywords(text);
    }

    public List<SongToRecommend> recommendSongs(Song song, Playlist playlist) throws IOException {

        doc2VecRecommender.setSong(song);
        doc2VecRecommender.setPlaylist(playlist);
        doc2VecRecommender.recommendSongs();
        return doc2VecRecommender.getRecommendedSongs();
    }

    public List<SongToRecommend> recommendSongsBasedOnKeywords(Song song, Playlist playlist)
            throws MusixMatchException, IOException {

        word2VecRecommender.setSong(song);
        word2VecRecommender.setPlaylist(playlist);
        word2VecRecommender.setKeywords(getKeywords(
                getLyricsText(song)).stream().map(KeywordsResult::getText).collect(Collectors.toList()));
        word2VecRecommender.recommendSongs();
        return word2VecRecommender.getRecommendedSongs();
    }

    public List<SongToRecommend> recommendSongsBasedOnKeywordsAndLyrics(Song song, Playlist playlist)
            throws IOException {

        doc2VecRecommender.setSong(song);
        doc2VecRecommender.setPlaylist(playlist);
        word2VecRecommender.setSong(song);
        word2VecRecommender.setPlaylist(playlist);
        combinedRecommender.setSong(song);
        combinedRecommender.setPlaylist(playlist);
        combinedRecommender.recommendSongs();
        return combinedRecommender.getRecommendedSongs();
    }

    public Song getSong(Long id) {
        return songRepository.getOne(id);
    }
}