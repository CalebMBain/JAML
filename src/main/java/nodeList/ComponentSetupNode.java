package nodeList;

import java.util.ArrayList;
import java.util.List;

public class ComponentSetupNode extends ComponentNode {

    private List<NodeCall> calls = new ArrayList<>();

    public ComponentSetupNode() {}
    public ComponentSetupNode(String prop) { addCalls(prop); }

    public node addCalls(String prop){
        prop = prop.replaceFirst("~", "");
        String[] calls = prop.split("\r\n(\t)?~");
        for(String call: calls){
            String[] p = call.split("::"), p2 = p[0].split(":"), p3 = new String[0];
            if(p.length > 1) p3 = p[1].split(":");
            switch(p2[0]){
                case "tryMap": this.calls.add(new NodeCall("tryMap", p2[1], p3[0], p3[1])); break;
                case "tryList": case "tryFlags": case "trySize": case "tryCheck": case "tryValue":
                case "tryBoolean": this.calls.add(new NodeCall(p2[0], p3[0], p3[1])); break;
                case "write": if(p2.length == 2) this.calls.add(new NodeCall("writeName", p3[0]));
                    else this.calls.add(new NodeCall("write", p3[0])); break;
                case "call": this.calls.add(new NodeCall("call", p2[1])); break;
            }
        }
        return this;
    }

    public List<NodeCall> getCalls() {
        return calls;
    }

    public boolean Type(Class c) {
        return (this.getClass() == c);
    }
}
