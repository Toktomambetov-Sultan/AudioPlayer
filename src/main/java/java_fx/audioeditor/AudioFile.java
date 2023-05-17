package java_fx.audioeditor;

import java.io.File;
import java.nio.file.Path;

public class AudioFile {
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AudioFile(String url) {
        this.url = url;
        file = new File(url);
        this.name = file.getName();
        System.out.println(this.name);

    }
    public File GetFile(){
        return this.file;
    }

    private File file;
    private String url = "";
    private String author = "";
    private String name = "";


}
