package nodeList;

public class StringNode implements node {

    private String content;

    public StringNode(String content) {
        this.content = content.replaceAll("<=o", "}").replaceAll("o=>", "{");
    }

    public String getContent() {
        return content;
    }

    public boolean Type(Class c) {
        return (this.getClass() == c);
    }
}
