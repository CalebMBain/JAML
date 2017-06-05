package nodeList;

import java.util.HashMap;
import java.util.Map;

public class MapNode implements node {

    private Map<String, String> map = new HashMap<>();

    public MapNode() {}
    public MapNode(String prop) { addMap(prop); }

    public String getitem(String key) {
        return map.get(key);
    }

    public MapNode addMap(String map) {
        String[] entries = map.split("\r\n(\t)?");
        for(String entry : entries){
            String[] parts = entry.split(":");
            this.map.put(parts[0], parts[1]);
        }
        return this;
    }

    public Map<String, String> getMap() {
        return map;
    }

    public boolean containsKey(String key){
        return map.containsKey(key);
    }

    public boolean Type(Class c) {
        return (this.getClass() == c);
    }
}
