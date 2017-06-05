package parsers;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// (least to most important)
//  1. External style sheet
//  2. Internal style sheet (in the style section)
//  3. Inline style (inside an JAML element)

public final class StyleParser {

    public Map<String, Style> Parse(Node style) {
        Map<String, Style> styles = new HashMap<>();
        NamedNodeMap nodeMap = style.getAttributes();
        Node Link = nodeMap.getNamedItem("link");
        if (Link != null) {
            String[] links = Link.getNodeValue().split(" ");
            for (String link : links) {
                String sheet = "";
                try (BufferedReader br = new BufferedReader(new FileReader(link))) {
                    String CurrentLine;
                    while ((CurrentLine = br.readLine()) != null) sheet += CurrentLine;
                } catch (IOException ignored) {
                }
                styles.putAll(GetContent(sheet));
            }
        }
        styles.putAll(GetContent(style.getTextContent()));
        return styles;
    }

    private Map<String, Style> GetContent(String s) {
        Map<String, Style> tokens = new HashMap<>();
        if (!s.isEmpty()) {
            s = s.replaceAll("\\s+", " ").replaceAll(" } ", "}").replaceAll(": ", ":");
            s = s.substring(1, s.length() - 2);
            String[] commands = s.split("}");
            for (String command : commands) {
                String[] parts = command.split(" ?\\{ ?");
                String[] names = parts[0].split(", ");
                for (String name : names) {
                    Style style = new Style(name);
                    style.setAttributes(parts[1]);
                    tokens.put(name.replaceAll("\\.", ""), style);
                }
            }
        }
        return tokens;
    }
}
