package backend.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.KeywordsResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import backend.domain.Playlist;
import backend.domain.Song;
import backend.domain.SongToRecommend;
import backend.domain.User;
import backend.dto.PlaylistDTO;
import backend.dto.SongDTO;
import backend.dto.SongToRecommendDTO;
import backend.service.PlaylistService;
import backend.service.SongService;
import java.util.ArrayList;
import java.util.List;

@RequestMapping("/playlists")
@RestController
@CrossOrigin
public class PlaylistController {

    private PlaylistService playlistService;
    private SongService songService;

    @Autowired
    public PlaylistController(PlaylistService playlistService, SongService songService) {
        this.songService = songService;
        this.playlistService = playlistService;
    }

    @GetMapping(value = "/{username}")
    public ResponseEntity<List<PlaylistDTO>> getPlaylistsOfUser(@RequestParam(required = false) String q, @PathVariable String username) {
        if (username == null) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        if (q.length() == 0) {
            List<Playlist> playlists = playlistService.getAllPlaylistsOfUser(username);
            List<PlaylistDTO> playlistDTOS = new ArrayList<>();
            for (Playlist playlist : playlists) {
                playlistDTOS.add(playlistDTOConverter(playlist));
            }
            return new ResponseEntity<>(playlistDTOS, HttpStatus.OK);
        } else {
            List<Playlist> playlists = playlistService.getAllPlaylistsFiltered(username, q);
            List<PlaylistDTO> playlistDTOS = new ArrayList<>();
            for (Playlist playlist : playlists) {
                playlistDTOS.add(playlistDTOConverter(playlist));
            }
            return new ResponseEntity<>(playlistDTOS, HttpStatus.OK);
        }
    }

    @PostMapping(value = "/new")
    public ResponseEntity<PlaylistDTO> addPlaylist(@RequestBody ObjectNode objectNode) {
        String playlistName = objectNode.get("title").asText();
        String username = objectNode.get("username").asText();

        User user = playlistService.getUserByUsername(username);

        Playlist playlist = playlistService.addPlaylist(new Playlist(playlistName, user));
        PlaylistDTO playlistDTO = playlistDTOConverter(playlist);
        return new ResponseEntity<PlaylistDTO>(playlistDTO, HttpStatus.OK);

    }

    @GetMapping(value = "/{username}/{id}")
    public ResponseEntity<PlaylistDTO> getPlaylistById(@PathVariable String username, @PathVariable Long id) {
        Playlist playlist = playlistService.getPlaylist(id);
        return new ResponseEntity<>(playlistDTOConverter(playlist), HttpStatus.OK);
    }

    @GetMapping(value = "/{username}/songs")
    public ResponseEntity<List<SongDTO>> getAllSongsForUser(@PathVariable String username) {
        List<Song> songs = playlistService.getAllSongsForAllPlaylists(username);
        if (songs.size() == 0)
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);

        List<SongDTO> songsDTO = new ArrayList<>();
        for (Song song : songs) {
            songsDTO.add(songDTOConverter(song));
        }
        return new ResponseEntity<>(songsDTO, HttpStatus.OK);
    }

    @PostMapping(value = "/{username}/{id}/songs")
    public ResponseEntity<String> addSong(@RequestBody ObjectNode objectNode) {
        Long playlistId = objectNode.get("id").asLong();
        String title = objectNode.get("title").asText();
        String artist = objectNode.get("artist").asText();
        String channelTitle = objectNode.get("channelTitle").asText();
        String thumbnail = objectNode.get("thumbnail").asText();
        String youtubeId = objectNode.get("youtubeId").asText();

        songService.addSongToPlaylist(new Song(title, artist, youtubeId, thumbnail, channelTitle), playlistId);

        return new ResponseEntity<String>(playlistId.toString(), HttpStatus.OK);

    }

    @GetMapping(value = "/{username}/{id}/songs")
    public ResponseEntity<List<SongDTO>> getSongsOfPlaylist(@PathVariable Long id) {
        List<Song> songs = playlistService.getSongsOfPlaylist(id);
        List<SongDTO> songsDTO = new ArrayList<>();
        for (Song song : songs) {
            songsDTO.add(songDTOConverter(song));
        }
        return new ResponseEntity<>(songsDTO, HttpStatus.OK);
    }

    @DeleteMapping(value = "/{username}/{idP}/songs/{idS}")
    public ResponseEntity<String> deleteSongById(@PathVariable Long idP, @PathVariable Long idS) {
        Song song = songService.getSong(idS);
        Playlist playlist = playlistService.getPlaylist(idP);

        songService.deleteSongFromPlaylist(song, playlist);

        return new ResponseEntity<>("deleted", HttpStatus.OK);
    }

    @GetMapping(value = "/{username}/{idP}/songs/{idS}")
    public ResponseEntity<SongDTO> getSongById(@PathVariable Long idP, @PathVariable Long idS) {
        Playlist playlist = playlistService.getPlaylist(idP);

        Song song = songService.getSongFromPlaylist(playlist, idS);
        if (song != null) {
            return new ResponseEntity<>(songDTOConverter(song), HttpStatus.OK);
        } else
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @GetMapping(value = "/{username}/{idP}/songs/{idS}/emotions")
    public ResponseEntity<String> getSongEmotions(@PathVariable Long idP, @PathVariable Long idS) {
        Playlist playlist = playlistService.getPlaylist(idP);
        Song song = songService.getSongFromPlaylist(playlist, idS);
        try {
            String lyrics = songService.getLyricsText(song);
            String emotions = songService.getEmotions(lyrics);
            return new ResponseEntity<>(emotions, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Song or lyrics not found!", HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/{username}/{idP}/songs/{idS}/lyrics", produces = "text/plain")
    public ResponseEntity<String> getSongLyrics(@PathVariable Long idP, @PathVariable Long idS) {
        Playlist playlist = playlistService.getPlaylist(idP);
        Song song = songService.getSongFromPlaylist(playlist, idS);
        try {
            String lyrics = songService.getLyricsText(song);
            return new ResponseEntity<>(lyrics, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Lyrics not found!", HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/{username}/{idP}/songs/{idS}/keywords")
    public ResponseEntity<List<KeywordsResult>> getSongKeywords(@PathVariable Long idP, @PathVariable Long idS) {
        Playlist playlist = playlistService.getPlaylist(idP);
        Song song = songService.getSongFromPlaylist(playlist, idS);
        try {
            String lyrics = songService.getLyricsText(song);
            List<KeywordsResult> keywords = songService.getKeywords(lyrics);
            return new ResponseEntity<>(keywords, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/{username}/{idP}/songs/{idS}/recommendations")
    public ResponseEntity<List<SongToRecommendDTO>> getRecommendedSongs(@PathVariable Long idP, @PathVariable Long idS) {
        Playlist playlist = playlistService.getPlaylist(idP);
        Song song = songService.getSongFromPlaylist(playlist, idS);
        List<SongToRecommendDTO> recommendedSongs = new ArrayList<>();
        try {
            List<SongToRecommend> songs = songService.recommendSongs(song, playlist);
            for (SongToRecommend s : songs) {
                recommendedSongs.add(songToRecommendDTOConverter(s));
            }
            return new ResponseEntity<>(recommendedSongs, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/{username}/{idP}/songs/{idS}/recommendations2")
    public ResponseEntity<List<SongToRecommendDTO>> getRecommendedSongs2(@PathVariable Long idP, @PathVariable Long idS) {
        Playlist playlist = playlistService.getPlaylist(idP);
        Song song = songService.getSongFromPlaylist(playlist, idS);
        List<SongToRecommendDTO> recommendedSongs = new ArrayList<>();
        try {
            List<SongToRecommend> songs = songService.recommendSongsBasedOnKeywords(song, playlist);
            for (SongToRecommend s : songs) {
                recommendedSongs.add(songToRecommendDTOConverter(s));
            }
            return new ResponseEntity<>(recommendedSongs, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/{username}/{idP}/songs/{idS}/recommendations3")
    public ResponseEntity<List<SongToRecommendDTO>> getRecommendedSongs3(@PathVariable Long idP, @PathVariable Long idS) {
        Playlist playlist = playlistService.getPlaylist(idP);
        Song song = songService.getSongFromPlaylist(playlist, idS);
        List<SongToRecommendDTO> recommendedSongs = new ArrayList<>();
        try {
            List<SongToRecommend> songs = songService.recommendSongsBasedOnKeywordsAndLyrics(song, playlist);
            for (SongToRecommend s : songs) {
                recommendedSongs.add(songToRecommendDTOConverter(s));
            }
            return new ResponseEntity<>(recommendedSongs, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value="/{username}/songs/{idS}/allRecommendations")
    public ResponseEntity<List<SongToRecommendDTO>> getAllRecommendedSongs(@PathVariable String username, @PathVariable Long idS) {
        Song song = songService.getSong(idS);
        Playlist playlist = playlistService.getPlaylistIdForSong(username, song);
        if (playlist == null)
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        List<SongToRecommend> songs = new ArrayList<>();
        List<SongToRecommendDTO> songToRecommendDTOS = new ArrayList<>();
        try {
            songs.addAll(songService.recommendSongs(song, playlist));
            songs.addAll(songService.recommendSongsBasedOnKeywords(song, playlist));
            songs.addAll(songService.recommendSongsBasedOnKeywordsAndLyrics(song, playlist));

            for (SongToRecommend s : songs) {
                songToRecommendDTOS.add(songToRecommendDTOConverter(s));
            }
            return new ResponseEntity<>(songToRecommendDTOS, HttpStatus.OK);
        }
        catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value="/{username}/songs/{idS}/playlist")
    public ResponseEntity<Long> getPlaylistIdForSong(@PathVariable String username, @PathVariable Long idS) {
        Song song = songService.getSong(idS);
        Playlist playlist = playlistService.getPlaylistIdForSong(username, song);
        if (playlist == null)
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(playlist.getId(), HttpStatus.OK);
    }


    private PlaylistDTO playlistDTOConverter(Playlist playlist) {
        return PlaylistDTO.builder()
                .id(playlist.getId().toString())
                .username(playlist.getUser().getUsername())
                .title(playlist.getTitle())
                .build();
    }

    private SongDTO songDTOConverter(Song song) {
        return SongDTO.builder()
                .id(song.getId().toString())
                .title(song.getTitle())
                .artist(song.getArtist())
                .channelTitle(song.getChannelTitle())
                .thumbnail(song.getThumbnail())
                .youtubeId(song.getYoutubeId())
                .build();
    }

    private SongToRecommendDTO songToRecommendDTOConverter(SongToRecommend song) {
        return SongToRecommendDTO.builder()
                .title(song.getTitle())
                .method(song.getMethod())
                .build();
    }
}
