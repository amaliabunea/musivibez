package backend.domain;

import java.io.Serializable;

public class SongToRecommend implements Serializable {
    private String title;
    private String artist;
    private String method;

    public SongToRecommend(String title, String method) {
        this.title = title;
        this.method = method;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
