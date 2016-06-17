package org.taitascioredev.fractal;

import java.io.File;

/**
 * Created by roberto on 28/05/15.
 */
public class Upload {

    private File image;
    private String albumId;
    private String name;
    private String title;
    private String description;

    public Upload(File image) { this.image = image; }

    public void setImage(File image) { this.image = image; }
    public void setAlbumId(String albumId) { this.albumId = albumId; }
    public void setName(String name) { this.name = name; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }

    public File getImage() { return image; }
    public String getAlbumId() { return albumId; }
    public String getName() { return name; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
}
