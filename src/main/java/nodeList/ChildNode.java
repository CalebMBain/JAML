package nodeList;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

public class ChildNode implements node {

    public ChildNode() {
    }

    public ChildNode(String prop) {
        addCalls(prop);
    }

    private List<NodeCall> calls = new ArrayList<>();

    public node addCalls(String prop){
        prop = prop.replaceFirst("~", "");
        String[] calls = prop.split("\r\n(\t)?~");
        for(String call: calls){
            String[] p = call.split("::"),
                    p2 = p[0].split(":"), p3 = new String[0];
            if(p.length > 1) p3 = p[1].split(":");
            switch(p2[0]){
                case "tryChildMap":
                    this.calls.add(new NodeCall("tryChildMap", p2[1], p3[0], p3[1]));
                case "tryChildMapDefault":
                    this.calls.add(new NodeCall("tryChildMapDefault", p2[1], p3[0])); break;
                case "tryBoolean":
                case "tryValue":
                case "tryCheck":
                case "trySize":
                case "tryFlags":
                case "tryList":
                case "tryChildParent":
                case "tryChildType":
                case "tryChild":
                    this.calls.add(new NodeCall(p2[0], p3[0], p3[1])); break;
                case "write":
                    if(p2.length == 2) this.calls.add(new NodeCall("writeName",p3[0]));
                    else this.calls.add(new NodeCall("write", p3[0])); break;
                case "tryFindAppend":
                    String[] params = {p2[1]};
                    if(p2.length == 3)
                        this.calls.add(new NodeCall("tryFindAppendNoName", ArrayUtils.addAll(params, p3)));
                    else this.calls.add(new NodeCall("tryFindAppend", ArrayUtils.addAll(params, p3))); break;
            }
        }
        return this;
    }

    public List<NodeCall> getCalls() {
        return calls;
    }

    @Override
    public boolean Type(Class c) {
        return false;
    }
}
