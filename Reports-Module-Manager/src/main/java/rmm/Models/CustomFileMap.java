package rmm.Models;


import java.util.Date;

public class CustomFileMap {
    public final Date docDay;
    public final java.nio.file.Path filepath;

    public CustomFileMap(Date docDay, java.nio.file.Path filepath) {
        this.docDay = docDay;
        this.filepath = filepath;
    }
}
