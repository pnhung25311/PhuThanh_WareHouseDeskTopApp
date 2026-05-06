package com.phuthanh.network;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Map;

public class ApiClient {

    private final String localIP = "http://192.168.1.54:2010/api/";
    private final String publicIP = "http://14.224.207.115:2010/api/";
    // private final String localIP = "http://192.168.1.11:8080/api/";
    // private final String publicIP = "http://14.224.207.115:8080/api/";

    // Giả lập getBaseUrl() async Dart
    public String getBaseUrl() {
        try {
            URL url = new URI("http://checkip.amazonaws.com/").toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String ip = reader.readLine().trim();
            reader.close();

            if ("14.224.207.115".equals(ip) || "Unknown".equals(ip)) {
                return localIP;
            } else {
                return publicIP;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return localIP;
        }
    }

    // GET request
    public String get(String endpoint) throws IOException {
        String urlString = getBaseUrl() + endpoint;
        try {
            URL url = new URI(urlString).toURL(); // ✅ Không deprecated
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            return readResponse(conn);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return "Invalid URL: " + e.getMessage();
        }
    }

    // POST request
    public String post(String endpoint, String jsonBody) throws IOException {
        String urlString = getBaseUrl() + endpoint;
        System.out.println("POST URL: " + urlString);
        System.out.println("POST Body: " + jsonBody);
        try {
            URL url = new URI(urlString).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes());
                os.flush();
            }

            return readResponse(conn);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return "Invalid URL: " + e.getMessage();
        }
    }

    // PUT request
    public String put(String endpoint, String jsonBody) throws IOException {
        String urlString = getBaseUrl() + endpoint;
        try {
            URL url = new URI(urlString).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes());
                os.flush();
            }

            return readResponse(conn);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return "Invalid URL: " + e.getMessage();
        }
    }

    // DELETE request
    public String delete(String endpoint) throws IOException {
        String urlString = getBaseUrl() + endpoint;
        try {
            URL url = new URI(urlString).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("Accept", "application/json");

            return readResponse(conn);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return "Invalid URL: " + e.getMessage();
        }
    }

    // POST file (multipart/form-data)
    public String postFile(String endpoint, File file, Map<String, String> fields, String fileField)
            throws IOException {
        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        String LINE_FEED = "\r\n";
        System.out.println("POST File URL: " + getBaseUrl() + endpoint);
        System.out.println("POST File: " + file.getName());

        try {
            URL url = new URI(getBaseUrl() + endpoint).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            conn.setDoOutput(true);

            try (OutputStream output = conn.getOutputStream();
                    PrintWriter writer = new PrintWriter(new OutputStreamWriter(output), true)) {

                // Add text fields
                if (fields != null) {
                    for (Map.Entry<String, String> entry : fields.entrySet()) {
                        writer.append("--").append(boundary).append(LINE_FEED);
                        writer.append("Content-Disposition: form-data; name=\"").append(entry.getKey()).append("\"")
                                .append(LINE_FEED);
                        writer.append("Content-Type: text/plain; charset=UTF-8").append(LINE_FEED);
                        writer.append(LINE_FEED);
                        writer.append(entry.getValue()).append(LINE_FEED);
                        writer.flush();
                    }
                }

                // Add file
                writer.append("--").append(boundary).append(LINE_FEED);
                writer.append("Content-Disposition: form-data; name=\"").append(fileField)
                        .append("\"; filename=\"").append(file.getName()).append("\"").append(LINE_FEED);
                writer.append("Content-Type: ").append(Files.probeContentType(file.toPath())).append(LINE_FEED);
                writer.append(LINE_FEED);
                writer.flush();

                Files.copy(file.toPath(), output);
                output.flush();

                writer.append(LINE_FEED).flush();
                writer.append("--").append(boundary).append("--").append(LINE_FEED);
                writer.flush();
            }

            return readResponse(conn);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return "Invalid URL: " + e.getMessage();
        }
    }

    private String readResponse(HttpURLConnection conn) throws IOException {

        int status = conn.getResponseCode();
        System.out.println("HTTP Status: " + conn.getResponseCode());
        InputStream stream;

        if (status >= 200 && status < 300) {
            stream = conn.getInputStream();
        } else {
            stream = conn.getErrorStream();
        }

        if (stream == null) {
            conn.disconnect();
            return "HTTP Error: " + status + " (no response body)";
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line).append("\n");
        }

        reader.close();
        conn.disconnect();

        return response.toString();
    }

}
