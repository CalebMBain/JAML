package nodeList;

import java.util.ArrayList;
import java.util.List;

public class NodeCall {
    private String Method;
    private List<String> params = new ArrayList<>();

    public NodeCall(String method, String... params) {
        Method = method;
        for(String param : params) this.params.add(param);
    }

    public List<String> getParams() {
        return params;
    }

    public String getMethod() {
        return Method;
    }
}
