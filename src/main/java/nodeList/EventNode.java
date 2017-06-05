package nodeList;

import java.util.ArrayList;
import java.util.List;

public class EventNode implements node{

    private List<NodeCall> calls = new ArrayList<>();

    private String template;

    public EventNode() {}
    public EventNode(String prop) { addCalls(prop); }

    public node addCalls(String prop){
        prop = prop.replaceFirst("~", "");
        String[] calls = prop.split("\r\n(\t)?~");
        for(String call: calls){
            String[] p = call.split("::"), p2 = p[0].split(":"), p3 = new String[0];
            if(p.length > 1) p3 = p[1].split(":");
            if(p2[0].equals("template"))
                template = p3[0].replaceAll("o=>", "{").replaceAll("<=o", "}");
            if(p2[0].equals("event")) this.calls.add(new NodeCall("event", p2[1], p3[0], p3[1]));
        }
        return this;
    }

    public String getTemplate() { return template; }

    public List<NodeCall> getCalls(){ return calls; }

    public boolean Type(Class c) {
        return false;
    }
}
