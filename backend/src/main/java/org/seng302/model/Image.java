package org.seng302.model;


import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class Image {
    // Id field for an Image entity
    @Id
    @GeneratedValue
    @Column(name = "image_id", nullable = false)
    private Integer id;

    // The file name in case we need to resolve it.
    @Column(name = "filename", nullable = false)
    private String filename;

    @Column(name = "thumbnailFilename")
    private String thumbnailFilename;

    // Extension type of the file
    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary;

    public Image(String filename, String thumbnailFilename, Boolean isPrimary) {
        this.filename = filename;
        this.thumbnailFilename = thumbnailFilename;
        this.isPrimary = isPrimary;
    }

    public Image(Integer id, String filename, String thumbnailFilename, Boolean isPrimary) {
        this.id = id;
        this.filename = filename;
        this.thumbnailFilename = thumbnailFilename;
        this.isPrimary = isPrimary;
    }

    public Image() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getThumbnailFilename() {
        return thumbnailFilename;
    }

    public void setThumbnailFilename(String thumbnailFilename) {
        this.thumbnailFilename = thumbnailFilename;
    }

    public Boolean getIsPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(Boolean primary) {
        isPrimary = primary;
    }

    public String toString() {
        return "{" +
                "\"id\":\"" + id + "\"," +
                "\"filename\":\"" + filename + "\"," +
                "\"thumbnailFilename\":\"" + thumbnailFilename + "\"" +
                "\"isPrimary\":\"" + isPrimary + "\"," +
                "}";
    }
}
