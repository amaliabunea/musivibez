package backend.service;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import backend.domain.Playlist;
import backend.domain.Song;
import backend.domain.User;
import backend.repository.PlaylistRepository;
import backend.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@Data
public class PlaylistService {

    private PlaylistRepository playlistRepository;
    private UserRepository userRepository;

    @Autowired
    public PlaylistService(PlaylistRepository playlistRepository, UserRepository userRepository){
        this.playlistRepository = playlistRepository;
        this.userRepository = userRepository;
    }

    public Playlist addPlaylist(Playlist playlist) {
        return playlistRepository.save(playlist);
    }

    public User getUserByUsername(String username) {
        return userRepository.getOne(username);
    }

    public Playlist getPlaylist(Long id) {
        return playlistRepository.getOne(id);
    }

    public List<Song> getSongsOfPlaylist(Long id) {
        return playlistRepository.getSongsByPlaylistId(id);
    }

    public List<Playlist> getAllPlaylistsOfUser(String username) {
        List<Playlist> playlists =  playlistRepository.getByUser_Username(username);
        return playlists;
    }

    public List<Playlist> getAllPlaylistsFiltered(String username, String q) {
        return playlistRepository.getByUser_UsernameAndTitleContains(username, q);
    }

    public List<Song> getAllSongsForAllPlaylists(String username) {
        List<Playlist> playlists = playlistRepository.getByUser_Username(username);
        List<Song> songs = new ArrayList<>();
        for (Playlist playlist : playlists) {
            songs.addAll(playlistRepository.getSongsByPlaylistId(playlist.getId()));
        }
        return songs;
    }

    public Playlist getPlaylistIdForSong(String username, Song song) {
        List<Playlist> playlists = playlistRepository.getByUser_Username(username);
        for (Playlist playlist : playlists) {
            if (playlist.getSongs().contains(song))
                return playlist;
        }
        return null;
    }
}
