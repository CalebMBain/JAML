package nodeList;

import java.util.ArrayList;
import java.util.List;

public class FunctionNode implements node {

    public FunctionNode() {}
    public FunctionNode(String prop) { addCalls(prop); }

    private List<NodeCall> calls = new ArrayList<>();

    public node addCalls(String prop){
        prop = prop.replaceFirst("~", "");
        String[] calls = prop.split("\r\n(\t)?~");
        for(String call: calls){
            String[] p = call.split("::"), p2 = p[0].split(":"), p3 = new String[0];
            if(p.length > 1) p3 = p[1].split(":");
            p3[1] = p3[1].replaceAll("o=>", "{").replaceAll("<=o", "}");
            if(p2[0].equals("func") && p2.length == 2) this.calls.add(new NodeCall("functionRef", p3[0], p3[1]));
            else if(p2[0].equals("func")) this.calls.add(new NodeCall("function", p3[0], p3[1]));
        }
        return this;
    }

    public List<NodeCall> getCalls(){ return calls; }

    public boolean Type(Class c) {
        return false;
    }
}
