package io.github.furstenheim;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Rules {
    private final Options options;
    private final Map<String, Rule> availableRules;
    private List<Rule> rules;

    public Rules (Options options) {
        this.options = options;
        availableRules = new HashMap<>();

        availableRules.put("blankReplacement", new Rule((element) -> CopyNode.isBlank(element), (content, element) ->
                CopyNode.isBlock(element) ? "\n\n" : ""));
        availableRules.put("paragraph", new Rule("p", (content, element) -> {return "\n\n" + content + "\n\n";}));
        availableRules.put("br", new Rule("br", (content, element) -> {return options.br + "\n";}));
        availableRules.put("heading", new Rule(new String[]{"h1", "h2", "h3", "h4", "h5", "h6" }, (content, element) -> {
            Integer hLevel = Integer.parseInt(element.nodeName().substring(1, 2));
            if (options.headingStyle == HeadingStyle.SETEXT && hLevel < 3) {
                String underline = String.join("", Collections.nCopies(content.length(), hLevel == 1 ? "=" : "-"));
                return "\n\n" + content + "\n" + underline + "\n\n";
            } else {
                return "\n\n" + String.join("", Collections.nCopies(hLevel, "#")) + " " + content + "\n\n";
            }
        }));
        availableRules.put("blockquote", new Rule("blockquote", (content, element) -> {
            content = content.replaceAll("^\n+|\n+$", "");
            content = content.replaceAll("(?m)^\n+|\n+$", "");
            return "\n\n" + content + "\n\n";
        }));
        availableRules.put("list", new Rule(new String[] { "ul", "ol" }, (content, element) -> {
            Element parent = (Element) element.parentNode();
            if (parent.nodeName() == "LI" && parent.child(parent.childrenSize() - 1) == element) {
                return "\n" + content;
            } else {
                return "\n\n" + content + "\n\n";
            }
        }));
        availableRules.put("listItem", new Rule("li", (content, element) -> {
            content = content.replaceAll("^\n+", "") // remove leading new lines
            .replaceAll("\n+$", "\n") // remove trailing new lines with just a single one
            .replaceAll("(?m)\n", "\n    "); // indent
            String prefix = options.bulletListMaker + "    ";
            Element parent = (Element)element.parentNode();
            if (parent.nodeName() == "OL") {
                String start = parent.attr("start");
                int index = parent.children().indexOf(element);
                int parsedStart = 1;
                if (start != "") {
                    try {
                        parsedStart = Integer.valueOf(start);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                prefix = String.valueOf(parsedStart + index) + ". ";
            }
            return prefix + content + ((element.nextSibling() != null && !content.matches("\n$")) ? "\n": "");
        }));
        availableRules.put("indentedCodeBlock", new Rule((element) -> {
            return options.codeBlockStyle == CodeBlockStyle.INDENTED
                && element.nodeName() == "PRE"
                && element.childNodeSize() > 0
                && element.childNode(0).nodeName() == "CODE";
        }, (content, element) -> {
            // TODO check textContent
            return "\n\n    " + element.childNode(0).outerHtml().replaceAll("/\n/", "\n    ");
        }));

        // TODO fencedCodeBlock

        availableRules.put("horizontalRule", new Rule("hr", (content, element) -> {
            return "\n\n" + options.hr + "\n\n";
        }));
        availableRules.put("inlineLink", new Rule((element) -> {
            return options.linkStyle == LinkStyle.INLINED
                    && element.nodeName() == "A"
                    && element.attr("href") != "";
        }, (content, element) -> {
            String href = element.attr("href");
            String title = cleanAttribute(element.attr("title"));
            if (title != "") {
                title = " \"" + title + "\"";
            }
            return "["+ content + "](" + href + title + ")";
        }));
        // TODO referenced link
        availableRules.put("emphasis", new Rule(new String[]{"em", "li"}, (content, element) -> {
            if (content.trim().length() == 0) {
                return "";
            }
            return options.emDelimiter + content + options.emDelimiter;
        }));
        availableRules.put("strong", new Rule(new String[]{"strong", "b"}, (content, element) -> {
            if (content.trim().length() == 0) {
                return "";
            }
            return options.strongDelimiter + content + options.strongDelimiter;
        }));
        // TODO code
        availableRules.put("img", new Rule("img", (content, element) -> {
            String alt = cleanAttribute(element.attr("alt"));
            String src = element.attr("src");
            if (src == "") {
                return "";
            }
            String title = cleanAttribute(element.attr("title"));
            String titlePart = "";
            if (title != "") {
                titlePart = " \"" + title + "\"";
            }
            return "![" + alt + "]" + "(" + src + titlePart + ")";
        }));
        availableRules.put("default", new Rule((element -> true), (content, element) -> CopyNode.isBlock(element) ? "\n\n" + content + "\n\n" : content));
        rules = new ArrayList<Rule>(availableRules.values());


    }

    public Rule findRule (Node node) {
        // TODO blank rule
        for (Rule rule : rules) {
            if (rule.getFilter().test(node)) {
                return rule;
            }
        }
        return null;
    }

    private String cleanAttribute (String attribute) {
        return attribute.replaceAll("(\n+\\s*)+", "\n");
    }

}
