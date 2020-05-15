package io.github.furstenheim;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CopyDown {
    public static void main (String[] args) throws IOException {
        String htmlFile = new String(Files.readAllBytes(Paths.get(
                "src/main/resources/gastronomia_y_cia_1.html")));
        Document document = Jsoup.parse(htmlFile);
        System.out.println("a");

    }
}
