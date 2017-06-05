package parsers;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import nodeList.*;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Compiler {
    private String prop;
    private nodeList nodeList;
    private String flavor;
    private String file;
    private Map<String, String> Components = new HashMap<>();
    private Map<String, String> classes = new HashMap<>();
    private Map<String, Style> styles = new HashMap<>();
    private Map<String, ComponentNode> namedComponents = new HashMap<>();
    private Map<String, ComponentNode> components = new HashMap<>();
    private Map<String, Map<String, Map.Entry<String, String>>> methodCalls = new HashMap<>();
    private StringBuilder sb = new StringBuilder();
    private Map<String, String> paths = new HashMap<String, String>(){{
        put("qt", "E:\\school\\capstone\\java-application-markup-langange\\src\\Language\\Qt.txt");
        put("fx", "E:\\school\\capstone\\java-application-markup-langange\\src\\Language\\Fx.txt");
    }};

    public static void main(String[] args) throws Exception {
        new Compiler().Compile(args[0]);
    }

    public void Compile(String arg){
        Node node = xmlParse(arg);
        nodeList = new nodeList(paths.get(flavor));
        Components = ((MapNode)nodeList.getNode("Components")).getMap();
        parse(node);
        String code = sb.toString();
        new GroovyShell(new Binding()).evaluate(code);
    }

    private Node xmlParse(String file) {
        try {
            File fXmlFile = new File(file);
            this.file = fXmlFile.getName().replaceAll(".jaml", "");
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();
            if (doc.hasChildNodes()){
                NodeList nodeList = doc.getChildNodes();
                Node node = nodeList.item(0);
                if (node instanceof Element && node.getNodeName().equals("jaml")) {
                    flavor = ((Element) node).getAttribute("flavor");
                    Node methods = ((Element) node).getElementsByTagName("methods").item(0);
                    if (methods != null) methodCalls = Parse(methods);
                    Node window = ((Element)node).getElementsByTagName("window").item(0);
                    if (window != null) return window;
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    public void parse(Node node){
        node beginning = nodeList.getNode("beginning");
        if(beginning.Type(StringNode.class)) PrintBeginning(((StringNode) beginning));
        writeClasses();
        parse("window", "", getNode("window"), node);
        node ending = nodeList.getNode("ending");
        if(ending.Type(StringNode.class)) PrintEnding(((StringNode) ending));
    }

    private void PrintEnding(StringNode ending){
        String content = ending.getContent();
        sb.append(String.format("%s\n", content));
        String[] lines = content.split("\n");
        System.out.println(lineCount++ + "\t\t\t" + lines[0]);
        for(int i = 1; i < lines.length; i++) System.out.println(lineCount++ + "\t" + lines[i]);
    }

    private void PrintBeginning(StringNode node){
        int i = 2;
        String content = node.getContent();
        Matcher m = Pattern.compile("\n").matcher(content);
        while (m.find()) i++;
        AppendAndPrint(content, "\t");
        lineCount = i;
    }

    private void writeClasses(){
        for(Map.Entry<String, String> pair : classes.entrySet()){
            String content = String.format("%s %s = new %s();", pair.getValue(), pair.getKey(), pair.getValue());
            AppendAndPrint(content, "\t\t\t");
        }
    }

    private void parse(String n, String parent, ComponentNode component, Node node){
        NamedNodeMap nodeMap = node.getAttributes();
        if(component.Type(ComponentNode.class)||component.Type(ComponentSetupNode.class)){
            String name = node.getNodeName();
            for(NodeCall call : component.getCalls()){
                List<String> params = call.getParams();
                switch(call.getMethod()){
                    case "trySetNameNoName": n = Nones(name, params.get(0), "missing text", "", nodeMap); break;
                    case "trySetName": n = Nones(n, name, params.get(0), "missing text", "", nodeMap); break;
                    case "trySetNameEvents": n = Events(n, name, params.get(0), "missing text", "", nodeMap); break;
                    case "trySetNameMethods":
                        n = Methods(n, name, params.get(0), "missing text", "", nodeMap); break;
                    case "trySetNameMethodsEvents":
                        n = MethodEvents(n, name, params.get(0), "missing text", "", nodeMap); break;
                    case "trySetNameText":
                        n = Nones(n, name, params.get(0), "missing text", node.getTextContent(), nodeMap); break;
                    case "trySetNameTextEvents":
                        n = Events(n, name, params.get(0), "missing text", node.getTextContent(), nodeMap); break;
                    case "trySetNameTextMethods":
                        n = Methods(n, name, params.get(0), "missing text", node.getTextContent(), nodeMap); break;
                    case "trySetNameTextMethodsEvents":
                        n = MethodEvents(n, name, params.get(0), "missing text", node.getTextContent(), nodeMap); break;
                    case "trySetNameValue":
                        n = Nones(n, name, params.get(1), "missing text", check(params.get(0), "", nodeMap), nodeMap); break;
                    case "trySetNameValueEvents":
                        n = Events(n, name, params.get(1), "missing text", check(params.get(0), "", nodeMap), nodeMap); break;
                    case "trySetNameValueMethods":
                        n = Methods(n, name, params.get(1), "missing text", check(params.get(0), "", nodeMap), nodeMap); break;
                    case "trySetNameValueMethodsEvents":
                        n = MethodEvents(n, name, params.get(1), "missing text", check(params.get(0), "", nodeMap), nodeMap); break;
                    case "writeName": int i = countWildCards(params.get(0));
                        if (i == 2) AppendAndPrint(String.format(params.get(0), n, n), "\t\t\t");
                        else if(i == 1) AppendAndPrint(String.format(params.get(0), n), "\t\t\t"); break;
                    case "tryBoolean": tryBoolean(n, params.get(0), params.get(1), nodeMap); break;
                    case "tryBooleanNoName":
                        tryBooleanNoName(params.get(0), params.get(1), nodeMap); break;
                    case "tryMap": tryMap(n, params.get(1), params.get(2), params.get(0), nodeMap); break;
                    case "tryMapNoName": tryMap(params.get(1), params.get(2), params.get(0), nodeMap); break;
                    case "tryCheck":
                        if(params.size() == 3) tryCheck(n, params.get(0), params.get(1), params.get(2), nodeMap);
                        else if(params.size() == 2) tryCheck(n, params.get(0), params.get(1), nodeMap); break;
                    case "tryCheckNoName":
                        if(params.size() == 3) tryCheck(params.get(0), params.get(1), params.get(2), nodeMap);
                        else if(params.size() == 2) tryCheck(params.get(0), params.get(1), nodeMap); break;
                    case "tryValue": tryValue(n, params.get(0), params.get(1), nodeMap); break;
                    case "tryValueNoName": tryValue(params.get(0), params.get(1), nodeMap); break;
                    case "tryFlags": setFlags(n, params.get(0), params.get(1), nodeMap); break;
                    case "tryFlagsNoName": setFlags(params.get(0), params.get(1), nodeMap); break;
                    case "trySize": trySize(n, params.get(0), params.get(1), nodeMap); break;
                    case "trySizeNoName": trySize(params.get(0), params.get(1), nodeMap); break;
                    case "tryList": tryList(n, params.get(0), params.get(1), nodeMap); break;
                    case "call": parse(n, parent, (ComponentNode) nodeList.getNode(params.get(0).replaceAll("\r\n", "")), node); break;
                    case "addChild": parse(n, params.get(0).replaceAll("\r\n", ""), parent, nodeMap); break;
                    case "addChildLayout": parse(n, params.get(0).replaceAll("\r\n", ""), parent, nodeMap);
                        for(Node child : XmlUtil.asList(node.getChildNodes())){
                            String cName = child.getNodeName();
                            if (!cName.equals("#text")) {
                                if (!(prop = check("name", "", child.getAttributes())).isEmpty())
                                    parse(prop, n, getNode(cName), child);
                                else parse(cName, n, getNode(cName), child);
                            }
                        } break;
                    case "write": AppendAndPrint(params.get(0), "\t\t\t"); break;
                    case "functions":
                        for(NodeCall func : component.getFunctionCalls().getCalls()){
                        List<String> pars = func.getParams();
                        if(func.getMethod().equals("function")) MakeFunc(n, pars.get(0), pars.get(1), nodeMap);
                        else if(func.getMethod().equals("functionRef")) MakeRef(n, pars.get(0), pars.get(1), nodeMap);


                        } break;
                    case "setStyle": prop = n.replaceAll("\\d", "").replaceAll("-", "_");
                        if(Components.containsKey(prop)) prop = Components.get(prop);
                        else prop = n;
                        setStyle(new Style(prop, Components.get(name.replaceAll("-", "_"))), nodeMap); break;
                    case "setStylesheet": setStylesheet(params.get(0)); break;
                }
            }
        }
    }

    private void setStyle(Style style, NamedNodeMap nodeMap) {
        Map<String, String> Styles = ((MapNode) nodeList.getNode("Styles")).getMap();
        for (Map.Entry<String, String> entry : Styles.entrySet())
            if (!(prop = check(entry.getKey(), "", nodeMap)).isEmpty()) style.addAttribute(entry.getValue(), prop);
        if(!style.getAttributes().isEmpty()) styles.put(style.getName(), style);
    }

    private void parse(String n, String t, String p, NamedNodeMap nodeMap){
        if(!p.isEmpty()){
            List<NodeCall> childCalls  = FindComponent(p).getChildCalls().getCalls();
            for(NodeCall call : childCalls){
                List<String> params = call.getParams();
                switch(call.getMethod()){
                    case "tryChildParent": tryChildParent(p, t, n, params.get(0), params.get(1)); break;
                    case "tryChildType": tryChildType(p, t, n, params.get(0), params.get(1), nodeMap); break;
                    case "tryChildMap":
                        tryChildMap(p, params.get(1), n, params.get(2), params.get(0), nodeMap); break;
                    case "tryChildMapDefault":
                        tryChildMapDefault(p, t, n, params.get(0), params.get(1), nodeMap); break;
                    case "tryChild": tryChild(n, t, params.get(0), params.get(1)); break;
                    case "tryFindAppend":
                        tryFindAppend(n, t, p, nodeMap, params.toArray(new String[params.size()])); break;
                    case "tryFindAppendNoName":
                        tryFindAppend(n, t, nodeMap, params.toArray(new String[params.size()])); break;
                    case "tryBoolean": tryBoolean(p, params.get(0), n, params.get(1), nodeMap); break;
                    case "tryCheck":
                        if(params.size() == 3) tryCheck(n, params.get(0), params.get(1), params.get(2), nodeMap);
                        else if(params.size() == 2) tryCheck(n, params.get(0), params.get(1), nodeMap); break;
                    case "tryList": tryList(n, params.get(0), params.get(1), nodeMap); break;
                    case "tryValue": tryValue(n, params.get(0), params.get(1), nodeMap); break;
                    case "trySize": trySize(n, params.get(0), params.get(1), nodeMap); break;
                    case "writeName": AppendAndPrint(String.format(params.get(0), n), "\t\t\t"); break;
                    case "write": AppendAndPrint(params.get(0), "\t\t\t"); break;
                }
            }
        }
    }

    private void setStylesheet(String command){
        final StringBuilder sb = new StringBuilder();
        Collection<Style> temp = styles.values();
        Style[] styles = temp.toArray(new Style[temp.size()]);
        for(int i = 0; i < styles.length;){
            sb.append(styles[i].toString());
            if(++i < styles.length) sb.append("\\n\"+\n\"");
        }
        if (!sb.toString().isEmpty()) AppendAndPrint(String.format(command, sb.toString()), "\t\t\t");
    }

    private void MakeFunc(String name, String p, String command, NamedNodeMap nodeMap){
        String[] callParts = new String[0];
        if (!(prop = check(p, "", nodeMap)).isEmpty()){
            if (!prop.isEmpty()) callParts = prop.split(":");
            if(callParts.length == 1) AppendAndPrint(String.format(command, name, "this", callParts[0]), "\t\t\t");
            else if(callParts.length == 2)
                AppendAndPrint(String.format(command, name, callParts[0], callParts[1]), "\t\t\t");
        }
    }

    private void MakeRef(String name, String p, String command, NamedNodeMap nodeMap){
        String[] callParts = new String[0];
        if (!(prop = check(p, "", nodeMap)).isEmpty()){
            if (!prop.isEmpty()) callParts = prop.split(":");
            Map<String, Map.Entry<String, String>> methods = new HashMap<>();
            String method = "";
            if(callParts.length == 1){
                methods = methodCalls.get("window");
                method = callParts[0];
            }
            else if(callParts.length == 2){
                methods = methodCalls.get(callParts[0]);
                method = callParts[1];
            }
            String m = method.replaceAll("\\(\\)", "");
            if(methods.containsKey(m)){
                Map.Entry<String, String> e = methods.get(m);
                AppendAndPrint(String.format(command, name, e.getKey()), "\t\t\t");
            }
        }
    }

    private String Nones(String type, String command, String replacement, String text, NamedNodeMap nodeMap){
        String name = trySetName("name", "window", type, nodeMap);
        String.format(command, name, (text.isEmpty()? replacement : text));
        return name;
    }

    private String trySetName(String p, String replacement, String type, NamedNodeMap nodeMap){
        replacement = replacement.replaceAll("-", "_");
        ComponentNode result = getNode(type);
        if((prop = check(p, "", nodeMap)).isEmpty()) prop = replacement;
        if(!Components.containsKey(prop)) namedComponents.put(prop, result);
        else if(!components.containsKey(prop)) components.put(prop, result);
        else{
            int count = 0;
            for(String comp : components.keySet()) if(comp.startsWith(prop)) count++;
            components.put(prop += count, result);
        }
        return prop;
    }

    private String Nones(String name, String type, String command, String replacement, String text, NamedNodeMap nodeMap){
        name = trySetName("name", name, type, nodeMap);
        String Result = String.format(command, name, (text.isEmpty()? replacement : text));
        AppendAndPrint(Result, "\t\t\t");
        return name;
    }

    private String Events(String name, String type, String command, String replacement, String text, NamedNodeMap nodeMap){
        name = trySetName("name", name, type, nodeMap);
        String Result = String.format(command, name, (text.isEmpty()? replacement : text));
        String events = EventParser(name, nodeMap);
        if(events != null) Result += "{";
        if(events == null) Result += ";";
        AppendAndPrint(Result, "\t\t\t");
        if(events != null) AppendAndPrint(events, "\t\t\t\t");
        if(events != null) AppendAndPrint("};", "\t\t\t");
        return name;
    }

    private String Methods(String name, String type, String command, String replacement, String text, NamedNodeMap nodeMap){
        name = trySetName("name", name, type, nodeMap);
        String Result = String.format(command, name, (text.isEmpty()? replacement : text));
        Map<String, Map.Entry<String, String>> methods = methodCalls.get(name);
        if(methods != null) Result += "{";
        if(methods == null) Result += ";";
        AppendAndPrint(Result, "\t\t\t");
        if(methods != null) for(Map.Entry<String, String> method : methods.values())
            AppendAndPrint(method.getValue(), "\t\t\t\t");
        if(methods != null) AppendAndPrint("};", "\t\t\t");
        return name;
    }

    private String MethodEvents(String name, String type, String command, String replacement, String text, NamedNodeMap nodeMap){
        name = trySetName("name", name, type, nodeMap);
        String Result = String.format(command, name, (text.isEmpty()? replacement : text));
        Map<String, Map.Entry<String, String>> methods = methodCalls.get(name);
        String events = EventParser(name, nodeMap);
        if(methods != null||events != null) Result += "{";
        if(methods == null&&events == null) Result += ";";
        AppendAndPrint(Result, "\t\t\t");
        if(methods != null) for(Map.Entry<String, String> method : methods.values())
            AppendAndPrint(method.getValue(), "\t\t\t\t");
        if(events != null) AppendAndPrint(events, "\t\t\t\t");
        if(methods != null||events != null) AppendAndPrint("};", "\t\t\t");
        return name;
    }

    private String EventParser(String name, NamedNodeMap nodeMap){
        EventNode events = (EventNode) nodeList.getNode("Events");
        String code = null;
        for(NodeCall event : events.getCalls()){
            List<String> params = event.getParams();
            if(!check(params.get(0), "", nodeMap).isEmpty()){
                String Class = file, Event = params.get(0),
                        Method = name + "_" + Event.replaceAll("when_", "").replaceAll("-", "_");
                for (String param : check(Event, "", nodeMap).split(" ")){
                    String[] parts = param.split(":");
                    if(parts[0].equals("class")) Class = parts[1];
                    if(parts[0].equals("method")) Method = parts[1];
                }
                if(code == null) code = "";
                code += String.format(events.getTemplate(), params.get(2), params.get(1), Class, Method);
            }
        }
        return code;
    }

    private ComponentNode FindComponent(String component){
        ComponentNode node = null;
        if(namedComponents.containsKey(component)) node = namedComponents.get(component);
        else if(components.containsKey(component)) node = components.get(component);
        return node;
    }

    private ComponentNode getNode(String name){
        return (ComponentNode) nodeList.getNode(Components.get(name.replaceAll("-", "_")));
    }

    private int lineCount = 1;
    private void AppendAndPrint(String content, String tabs){
        sb.append(String.format("%s\n", content));
        for(String c : content.split("\n")) System.out.println(lineCount++ + tabs + c);
    }

    private int countWildCards(String input){
        int i = 0;
        Matcher m = Pattern.compile("%s").matcher(input);
        while (m.find()) i++;
        return i;
    }

    private void tryChildMap(String name, String p, String child, String command, String Map, NamedNodeMap nodeMap){
        Map<String, String> map = ((MapNode)nodeList.getNode(Map)).getMap();
        if(!(prop = check(p, "", nodeMap)).isEmpty())
            AppendAndPrint(String.format(command, name, child, map.get(prop)), "\t\t\t");
    }

    private void tryChildMapDefault(String name, String component, String child, String Map, String Default, NamedNodeMap nodeMap){
        boolean result = false;
        Map<String, String> map = ((MapNode)nodeList.getNode(Map)).getMap();
        for(String key : map.keySet()) {
            result = true;
            tryChildType(name, component, child, key, map.get(key), nodeMap);
        }
        if(!result) AppendAndPrint(String.format(Default, name, child), "\t\t\t");
    }

    private void tryFindAppend(String name, String component, String parent, NamedNodeMap nodeMap, String... params){
        if(component.equals(params[0])){
            Map<Integer, Map.Entry<String, Object>> values = new HashMap<Integer, Map.Entry<String, Object>>(){{
                put(0, new AbstractMap.SimpleEntry<>("name", parent));
                put(1, new AbstractMap.SimpleEntry<>("child", name));
            }};
            for(int i = 2; i < params.length; i++){
                String[] s1 = params[i].split("/");
                if(s1.length == 1) values.put(i, new AbstractMap.SimpleEntry<>(s1[0], s1[0]));
                else if(s1[1].equals("map")){
                    String value = "";
                    Map<String, String> map = ((MapNode)nodeList.getNode(s1[2])).getMap();
                    if(!(prop = check(s1[0], "", nodeMap)).isEmpty()) value = map.get(prop);
                    else if(s1.length == 4) value = map.get(s1[3]);
                    values.put(i, new AbstractMap.SimpleEntry<>(s1[0], value));
                } else if(s1[1].equals("int")) {
                    if(!(prop = check(s1[0], "", nodeMap)).isEmpty())
                        values.put(i, new AbstractMap.SimpleEntry<>(s1[0], Integer.parseInt(prop)));
                    else if(tryInteger(s1[2]))
                        values.put(i, new AbstractMap.SimpleEntry<>(s1[0], Integer.parseInt(s1[2])));
                    else for(Map.Entry<String, Object> entry : values.values()) if(entry.getKey().equals(s1[2])){
                        values.put(i, new AbstractMap.SimpleEntry<>(s1[0], entry.getValue())); break;
                    }
                }
            }
            Object[] list = new Object[values.size()];
            for(Map.Entry<Integer, Map.Entry<String, Object>> entry : values.entrySet())
                list[entry.getKey()] = (entry.getValue()).getValue();
            String comp = String.format(params[1], list);
            AppendAndPrint(comp, "\t\t\t");
        }
    }

    private void tryFindAppend(String name, String component, NamedNodeMap nodeMap, String... params){
        if(component.equals(params[0])){
            Map<Integer, Map.Entry<String, Object>> values = new HashMap<Integer, Map.Entry<String, Object>>(){{
                put(0, new AbstractMap.SimpleEntry<>("child", name));
            }};
            int count = 1;
            for(int i = 2; i < params.length; i++){
                String[] s1 = params[i].split("/");
                if(s1.length == 1) values.put(count, new AbstractMap.SimpleEntry<>(s1[0], s1[0]));
                else if(s1[1].equals("map")){
                    s1[2] = s1[2].replaceAll("\r\n", "");
                    String value = "";
                    Map<String, String> map = ((MapNode)nodeList.getNode(s1[2])).getMap();
                    if(!(prop = check(s1[0], "", nodeMap)).isEmpty()) value = map.get(prop);
                    else if(s1.length == 4) value = map.get(s1[3]);
                    values.put(count, new AbstractMap.SimpleEntry<>(s1[0], value));
                } else if(s1[1].equals("int")) {
                    s1[2] = s1[2].replaceAll("\r\n", "");
                    if(!(prop = check(s1[0], "", nodeMap)).isEmpty())
                        values.put(count, new AbstractMap.SimpleEntry<>(s1[0], Integer.parseInt(prop)));
                    else if(tryInteger(s1[2]))
                        values.put(count, new AbstractMap.SimpleEntry<>(s1[0], Integer.parseInt(s1[2])));
                    else for(Map.Entry<String, Object> entry : values.values()) if(entry.getKey().equals(s1[2])){
                        values.put(count++, new AbstractMap.SimpleEntry<>(s1[0], entry.getValue())); break;
                    }
                }
            }
            Object[] list = new Object[values.size()];
            for(Map.Entry<Integer, Map.Entry<String, Object>> entry : values.entrySet())
                list[entry.getKey()] = (entry.getValue()).getValue();
            String comp = String.format(params[1], list);
            AppendAndPrint(comp, "\t\t\t");
        }
    }

    private void tryChild(String name, String component, String prop, String command){
        if(prop.equals(component)) AppendAndPrint(String.format(command, name), "\t\t\t");
    }

    private boolean tryChildType(String name, String component, String child, String prop, String command, NamedNodeMap nodeMap){
        String[] nameNtype = component.split("&");
        boolean result = false;
        if(prop.equals(nameNtype[0])) if(check("type", "", nodeMap).equals(nameNtype[1])){
            AppendAndPrint(String.format(command, name, child), "\t\t\t");
            result = true;
        }
        return result;
    }

    private void tryChildParent(String name, String component, String child, String prop, String command){
        if(prop.equals(component)) AppendAndPrint(String.format(command, name, child), "\t\t\t");
    }

    private void trySize(String n, String prop, String command, NamedNodeMap nodeMap){
        if (!(prop = check(prop, "", nodeMap)).isEmpty()){
            String[] parts = prop.split(" ");
            AppendAndPrint(String.format(command, n, parts[0], parts[1]), "\t\t\t");
        }
    }

    private void trySize(String prop, String command, NamedNodeMap nodeMap){
        if (!(prop = check(prop, "", nodeMap)).isEmpty()){
            String[] parts = prop.split(" ");
            AppendAndPrint(String.format(command, parts[0], parts[1]), "\t\t\t");
        }
    }

    private void setFlags(String n, String command, String map, NamedNodeMap nodeMap){
        boolean hasFlags = false;
        Map<String, String> flags = ((MapNode)nodeList.getNode(map)).getMap();
        for(String p : flags.keySet())
            if(!(prop = check(p, "", nodeMap)).isEmpty()) if(prop.equals("true")){
                if(hasFlags) AppendAndPrint(String.format(", %s", flags.get(p)), "\t\t\t");
                else AppendAndPrint(String.format(command, n, flags.get(p)), "\t\t\t");
                hasFlags = true;
            }
        if (hasFlags) AppendAndPrint(");\n", "\t\t\t");
    }

    private void setFlags(String command, String map, NamedNodeMap nodeMap){
        boolean hasFlags = false;
        Map<String, String> flags = ((MapNode)nodeList.getNode(map)).getMap();
        for(String p : flags.keySet())
            if(!(prop = check(p, "", nodeMap)).isEmpty()) if(prop.equals("true")){
                if(hasFlags) AppendAndPrint(String.format(", %s", flags.get(p)), "\t\t\t");
                else AppendAndPrint(String.format(command, flags.get(p)), "\t\t\t");
                hasFlags = true;
            }
        if (hasFlags) AppendAndPrint(");\n", "\t\t\t");
    }

    private void tryValue(String name, String p, String command, NamedNodeMap nodeMap){
        if (tryInteger(prop = check(p, "", nodeMap))) AppendAndPrint(String.format(command, name, prop), "\t\t\t");
        else if(tryDouble(prop = check(p, "", nodeMap)))
            AppendAndPrint(String.format(command, name, prop), "\t\t\t");
    }

    private void tryValue(String p, String command, NamedNodeMap nodeMap){
        if (tryInteger(prop = check(p, "", nodeMap))) AppendAndPrint(String.format(command, prop), "\t\t\t");
        else if(tryDouble(prop = check(p, "", nodeMap))) AppendAndPrint(String.format(command, prop), "\t\t\t");
    }

    private void tryCheck(String name, String p, String replacement, String command, NamedNodeMap nodeMap){
        if (!(prop = check(p, "", nodeMap)).isEmpty()) AppendAndPrint(String.format(command, name, prop), "\t\t\t");
        else AppendAndPrint(String.format(command, name, replacement), "\t\t\t");
    }

    private void tryCheck(String name, String p, String command, NamedNodeMap nodeMap){
        if (!(prop = check(p, "", nodeMap)).isEmpty()) AppendAndPrint(String.format(command, name, prop), "\t\t\t");
    }

    private void tryCheck(String p, String command, NamedNodeMap nodeMap){
        if (!(prop = check(p, "", nodeMap)).isEmpty()) AppendAndPrint(String.format(command, prop), "\t\t\t");
    }

    private void tryMap(String n, String p, String command, String Map, NamedNodeMap nodeMap){
        Map<String, String> map = ((MapNode)nodeList.getNode(Map)).getMap();
        if (!(prop = check(p, "", nodeMap)).isEmpty()) tryEmptyAppend(n, map.get(prop), command);
    }

    private void tryMap(String p, String command, String Map, NamedNodeMap nodeMap){
        Map<String, String> map = ((MapNode)nodeList.getNode(Map)).getMap();
        if (!(prop = check(p, "", nodeMap)).isEmpty()) tryEmptyAppend(map.get(prop), command);
    }

    private void tryEmptyAppend(String name, String value, String command){
        if(!value.isEmpty()) AppendAndPrint(String.format(command, name, value), "\t\t\t");
    }

    private void tryEmptyAppend(String value, String command){
        if(!value.isEmpty()) AppendAndPrint(String.format(command, value), "\t\t\t");
    }

    private void tryBoolean(String name, String p, String command, NamedNodeMap nodeMap){
        if (tryBoolean(prop = check(p, "", nodeMap))) AppendAndPrint(String.format(command, name, prop), "\t\t\t");
    }

    private void tryBoolean(String name, String prop, String child, String command, NamedNodeMap nodeMap){
        if (tryBoolean(check(prop, "", nodeMap))) AppendAndPrint(String.format(command, name, child), "\t\t\t");
    }

    private void tryBooleanNoName(String p, String command, NamedNodeMap nodeMap){
        if (tryBoolean(prop = check(p, "", nodeMap))) AppendAndPrint(String.format(command, prop), "\t\t\t");
    }

    private void tryList(String n, String p, String method, NamedNodeMap nodeMap){
        StringBuilder value = new StringBuilder();
        if (!(prop = check(p, "", nodeMap)).isEmpty()) {
            value.append(String.format("%s.%s(new ArrayList<String>() {{", n, method));
            for(String s : prop.split(" ")) value.append(String.format("add(\"%s\");\n", s));
            value.append("}});\n");
        }
        AppendAndPrint(value.toString(), "\t\t\t");
    }

    private boolean tryDouble(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    private boolean tryInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    private String check(String keyword, String replacement, NamedNodeMap nodeMap) {
        try {
            Node word = nodeMap.getNamedItem(keyword);
            return (word != null) ? word.getNodeValue() : "";
        } catch (NullPointerException ignored) {
            return replacement;
        }
    }

    private boolean tryBoolean(String value) {
        return (value.equals("true") || value.equals("false"));
    }

    public String capitalize(final String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }

    public String tryEmpty(String p, String replacement, NamedNodeMap nodeMap){
        return (!(prop = check(p, "", nodeMap)).isEmpty()) ? prop : replacement;
    }

    private boolean hasProps(StringBuilder sb, String command, boolean hasProps){
        if(hasProps) sb.append(command);
        return true;
    }

    private List<String> types = new ArrayList<String>(){{
        add("int"); add("short"); add("long"); add("byte");
        add("float"); add("double"); add("char"); add("boolean");
    }};

    public Map<String, Map<String, Map.Entry<String, String>>> Parse(Node methods){
        Map<String, Map<String, Map.Entry<String, String>>> result = new HashMap<>();
        NodeList methodCalls = methods.getChildNodes();
        for(int i = 0; i < methodCalls.getLength(); i++){
            Node method = methodCalls.item(i);
            NamedNodeMap attributes = method.getAttributes();
            if(method.getNodeName().equals("method")){
                String name = tryEmpty("name", "method", attributes);
                if(name.contains("-")) name = name.replaceAll("-", "_");
                String mCall = check("call", "", attributes), cCall = check("class", "", attributes);
                String params = ParamsParser(attributes), methodParams = ParamsMethodParser(attributes);
                String refs = check("ref", "", attributes);
                if(!refs.isEmpty()) refs = ", " + refs;
                String owner = tryEmpty("owner", "window", attributes);
                Map<String, Map.Entry<String, String>> Method = (result.containsKey(owner)) ? result.get(owner) : new HashMap<>();
                if(!mCall.isEmpty() && !cCall.isEmpty()){
                    String methodCall = String.format("%s.%s(%s%s)", cCall, mCall, params, refs);
                    String code = String.format("public Object %s(%s){ %s; return null; }\n", name, methodParams, methodCall);
                    Method.put(name, new AbstractMap.SimpleEntry<>(methodCall.replaceAll("this", owner), code));
                } result.put(owner, Method);
            }else if(method.getNodeName().equals("class"))
                classes.put(tryEmpty("name", "method", attributes), check("call", "", attributes));
        }
        return result;
    }

    private String ParamsParser(NamedNodeMap attributes){
        boolean hasProps = false;
        String s = check("params", "", attributes);
        StringBuilder value = new StringBuilder();
        for(String p : s.split(",( )?")){
            if(types.contains(p)){
                hasProps = hasProps(value, ", ", hasProps);
                value.append(String.format("%s", capitalize(p)));
            }else if(!p.isEmpty() && !p.equals("this")){
                hasProps = hasProps(value, ", ", hasProps);
                value.append(String.format("%s", p));
            }else if(p.equals("this")){
                hasProps = hasProps(value, ", ", hasProps);
                value.append("this");
            }
        }
        return value.toString();
    }

    private String ParamsMethodParser(NamedNodeMap attributes){
        boolean hasProps = false;
        StringBuilder value = new StringBuilder();
        String params = check("params", "", attributes);
        for(String p : params.split(",( )?")){
            if(types.contains(p)){
                hasProps = hasProps(value, ", ", hasProps);
                value.append(String.format("%s %s", p, capitalize(p)));
            }else if(!p.isEmpty() && !p.equals("this")){
                String name = p;
                if(Components.containsKey(p)) p = Components.get(p);
                hasProps = hasProps(value, ", ", hasProps);
                value.append(String.format("%s %s", p, name));
            }
        }
        return value.toString();
    }
}

final class XmlUtil {

    private XmlUtil(){}

    public static List<Node> asList(NodeList n) {
        return n.getLength()==0 ? Collections.<Node>emptyList(): new NodeListWrapper(n);
    }

    static final class NodeListWrapper extends AbstractList<Node> implements RandomAccess {
        private final NodeList list;

        NodeListWrapper(NodeList l) { list = l; }

        public Node get(int index) { return list.item(index); }

        public int size() { return list.getLength(); }
    }
}