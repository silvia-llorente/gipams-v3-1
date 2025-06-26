package rmm.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtils {

    public FileUtils() {
    }

    public static void createDirectory(String path) {
        new File(path).mkdirs();
    }

    public static void editFile(String path, String logLine) {
        try {
            File f = new File(path);
            f.createNewFile();
            try {
                FileWriter fw = new FileWriter(path, true);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.newLine();
                bw.write(logLine);
                bw.close();
                fw.close();
            } catch (IOException ex) {
                Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException ex) {
            Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static List<File> getFiles(String dirPath) {
        File dir = new File(dirPath);
        if (dir.exists()) {
            return Stream.of(Objects.requireNonNull(dir.listFiles()))
                    .filter(f -> !f.isDirectory())
                    .collect(Collectors.toList());

        } else {
            Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, "DIRECTORY IN-EXISTENT");
        }

        return new ArrayList<>();
    }

    public static List<Path> getFilesNames(String dirPath) {
        Path path = Paths.get(dirPath).normalize();

        ArrayList<Path> files = new ArrayList<>();
        try {
            for (Path filePath : Files.newDirectoryStream(path)) {
                if (!Files.isDirectory(filePath))
                    files.add(filePath);
            }

        } catch (IOException e) {
            Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, e.getMessage(), "DIRECTORY IN-EXISTENT");
        }

        return files;
    }

    public static String genFileNameWithDate(Properties props, String fileName, String extension) {
        SimpleDateFormat sdf = new SimpleDateFormat(props.getProperty("logsDaysFormat"));
        String date = sdf.format(DateUtils.GetNowTime());

        return String.format("%s%s.%s", date, fileName, extension);
    }
}
