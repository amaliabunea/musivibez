package backend.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import backend.domain.Rating;
import backend.domain.Song;
import backend.domain.User;
import backend.dto.RatingDTO;
import backend.service.PlaylistService;
import backend.service.RatingService;
import backend.service.SongService;

import java.util.ArrayList;
import java.util.List;

@RequestMapping("/ratings")
@RestController
@CrossOrigin
public class RatingController {

    private RatingService ratingService;
    private PlaylistService playlistService;
    private SongService songService;

    @Autowired
    public RatingController(RatingService ratingService, PlaylistService playlistService, SongService songService) {
        this.ratingService = ratingService;
        this.playlistService = playlistService;
        this.songService = songService;
    }

    @GetMapping
    public ResponseEntity<List<RatingDTO>> getRatings() {
        List<Rating> ratings = ratingService.getAllRatings();
        if (ratings.size() == 0)
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        List<RatingDTO> ratingDTOS = new ArrayList<>();
        for (Rating rating : ratings) {
            ratingDTOS.add(ratingDTOConverter(rating));
        }
        return new ResponseEntity<>(ratingDTOS, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<RatingDTO> addRating(@RequestBody ObjectNode objectNode) {
        String songTitle = objectNode.get("songTitle").asText();
        String songArtist = objectNode.get("songArtist").asText();
        Long songId = objectNode.get("songId").asLong();
        String username = objectNode.get("username").asText();
        Long method = objectNode.get("method").asLong();
        double ratingUser = objectNode.get("ratingUser").asDouble();
        double ratingAlg = objectNode.get("ratingAlg").asDouble();

        User user = playlistService.getUserByUsername(username);
        Song song = songService.getSong(songId);

        Rating rating = new Rating(songTitle, songArtist, method, ratingAlg, ratingUser, user, song);
        RatingDTO ratingDTO = ratingDTOConverter(ratingService.addRating(rating));
        return new ResponseEntity<>(ratingDTO, HttpStatus.OK);
    }
    private RatingDTO ratingDTOConverter(Rating rating) {
        return RatingDTO.builder()
                .id(rating.getId().toString())
                .songTitle(rating.getSongTitle())
                .songArtist(rating.getSongArtist())
                .method(rating.getMethod().toString())
                .ratingAlg(String.valueOf(rating.getRatingAlg()))
                .ratingUser(String.valueOf(rating.getRatingUser()))
                .songId(rating.getSong().getId().toString())
                .username(rating.getUser().getUsername())
                .build();
    }
}
