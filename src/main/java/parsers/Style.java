package parsers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Style {
    private String name = "";
    private List<String> nameAttributes = new ArrayList<>();
    private String subControl = "";
    private String component;
    private Map<String, String> attributes = new HashMap<>();

    public Style(String name) { parseName(name); }

    public Style(String name, String component) {
        parseName(name);
        this.component = component;
    }

    private void parseName(String name) {
        name = name.replaceAll("\\.", "");
        String[] temp;
        if (name.contains("::")) {
            temp = name.split("::");
            this.name = temp[0];
            if (temp[1].contains(":")) {
                temp = name.split(":");
                setSubControl(temp[0]);
                for (int i = 1; i < temp.length; i++) if (!nameAttributes.contains(temp[i]))
                    this.addNameAttributes(temp[i]);
            }
        } else if (name.contains(":")) {
            temp = name.split(":");
            this.name = temp[0];
            for (int i = 1; i < temp.length; i++) if (!nameAttributes.contains(temp[i]))
                this.addNameAttributes(temp[i]);
        } else this.name = name;
    }

    public boolean isEmpty(){ return attributes.isEmpty(); }

    public void addAll(Style style) { this.attributes.putAll(style.getAttributes()); }

    public String getName() { return name; }

    public Map<String, String> getAttributes() { return attributes; }

    public void setAttributes(String attributes) {
        String[] parts = attributes.split("; ");
        for (String part : parts) {
            String[] params = part.replaceAll(";", "").split(":");
            this.attributes.put(params[0], params[1]);
        }
    }

    public void addAttribute(String key, String value) { attributes.put(key, value); }

    public void setComponent(String component) { this.component = component; }

    public void setSubControl(String subControl) { this.subControl = subControl; }

    public void addNameAttributes(String prop) { if (!nameAttributes.contains(prop)) nameAttributes.add(prop); }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!attributes.isEmpty()) {
            sb.append(String.format("%s", component));
            if (!name.isEmpty() && !name.equals(component)) sb.append(String.format("#%s", name));
            if (!subControl.isEmpty()) sb.append(String.format("::%s", subControl));
            for (String nameAttribute : nameAttributes) sb.append(String.format(":%s", nameAttribute));
            sb.append(" { ");
            for (Map.Entry<String, String> attribute : attributes.entrySet()) {
                sb.append(String.format("%s:", attribute.getKey()));
                sb.append(String.format("%s; ", attribute.getValue()));
            }
            sb.append("}");
        }
        return sb.toString();
    }
}
