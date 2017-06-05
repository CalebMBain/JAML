package nodeList;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class nodeList {
    private static Map<String, node> nodeList = new HashMap<>();

    public nodeList(String path) {
        StringBuilder sb = new StringBuilder();
        try(BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
        } catch (IOException e) { e.printStackTrace(); }
        addNodes(sb.toString());
    }

    public node getNode(String name){ return nodeList.get(name); }

    public void addNodes(String lang){
        String[] parts = lang.split("}\r\n(\r\n)?");
        for(String part : parts){
            String[] parts2 = part.split("( )?\\{(\r\n)?(\t)?"), partName = parts2[0].split(":");
            switch(partName[1]){
                case "map": nodeList.put(partName[0], new MapNode(parts2[1]));            break;
                case "component-setup":           nodeList.put(partName[0], new ComponentSetupNode(parts2[1])); break;
                case "component":                 nodeList.put(partName[0], new ComponentNode(parts2[1]));      break;
                case "string":                    nodeList.put(partName[0], new StringNode(parts2[1]));         break;
                case "child":    ((ComponentNode) nodeList.get(partName[0])).addChildCalls(parts2[1]);          break;
                case "function": ((ComponentNode) nodeList.get(partName[0])).addFunctionCalls(parts2[1]);       break;
                case "events":                    nodeList.put(partName[0], new EventNode(parts2[1]));          break;
            }
        }
    }
}
