package io.github.furstenheim;

import org.jsoup.nodes.Node;

// TODO fix
// Methods missing from jsoup
public class NodeUtils {
    // Element node
    public static boolean isNodeType1 (Node element) {
        return element.nodeName() == "p" || element.nodeName() == "div";
    }
    /*public static boolean isNodeType1 (Element element) {
        return element.tagName() == "p" || element.tagName() == "div";
    }
    */// Text node
    public static boolean isNodeType3 (Node element) {
        return element.nodeName() == "text";
    }/*
    public static boolean isNodeType3 (Element element) {
        return element.tagName() == "text";
    }*/
    // CDATA section node
    public static boolean isNodeType4 (Node element) {
        return false;
    }
}
