package com.phuthanh.update;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phuthanh.network.ApiClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

public class CheckUpdate {

    private ApiClient apiClient = new ApiClient();
    private final String VERSION_APP = "1.0.0";

    // Folder chứa bản update trên server LAN
    // private final String UPDATE_FOLDER = "C:\\fileExcel";
    private final String UPDATE_FOLDER = "\\\\SERVER\\Phòng CN&KT-Hưng\\file_update_wh";

    // Folder app hiện tại
    private final String APP_FOLDER = "C:\\";

    public void checkAndUpdate() {

        try {

            String response = apiClient.get("dynamic/version-warehouse");

            System.out.println("API Response:");
            System.out.println(response);

            String apiVersion = parseVersion(response);

            System.out.println("APP VERSION: " + VERSION_APP);
            System.out.println("API VERSION: " + apiVersion);
            System.out.println("APP_FOLDER: " + APP_FOLDER);

            if (apiVersion != null && !VERSION_APP.equals(apiVersion)) {

                System.out.println("Có phiên bản mới -> Update");
                Path p = Paths.get("\\\\SERVER\\newtest");
                System.out.println("Exists: " + Files.exists(p));
                copyFolder(
                        Paths.get(UPDATE_FOLDER),
                        Paths.get(APP_FOLDER));

                System.out.println("Update xong -> restart");

                restartApp();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String parseVersion(String response) {

        try {

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(response);

            return node.get("VERSION_WAREHOUSE").asText();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // COPY FOLDER
    private void copyFolder(Path source, Path target) throws IOException {

        Files.walk(source).forEach(path -> {

            try {

                Path destination = target.resolve(source.relativize(path));

                if (Files.isDirectory(path)) {

                    if (!Files.exists(destination)) {
                        Files.createDirectories(destination);
                    }

                } else {

                    Files.copy(
                            path,
                            destination,
                            StandardCopyOption.REPLACE_EXISTING);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        });
    }

    // RESTART APP
    private void restartApp() {

        try {

            String javaBin = System.getProperty("java.home")
                    + File.separator + "bin"
                    + File.separator + "java";

            String jarPath = new File(
                    CheckUpdate.class
                            .getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI())
                    .getPath();

            new ProcessBuilder(
                    javaBin,
                    "-jar",
                    jarPath).start();

            System.exit(0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}