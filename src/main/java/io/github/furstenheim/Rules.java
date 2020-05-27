package io.github.furstenheim;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Rules {
    private final Options options;
    private List<Rule> rules;

    public Rules (Options options) {
        this.rules = new ArrayList<>();
        this.options = options;

        addRule("blankReplacement", new Rule((element) -> CopyNode.isBlank(element), (content, element) ->
                CopyNode.isBlock(element) ? "\n\n" : ""));
        addRule("paragraph", new Rule("p", (content, element) -> {return "\n\n" + content + "\n\n";}));
        addRule("br", new Rule("br", (content, element) -> {return options.br + "\n";}));
        addRule("heading", new Rule(new String[]{"h1", "h2", "h3", "h4", "h5", "h6" }, (content, element) -> {
            Integer hLevel = Integer.parseInt(element.nodeName().substring(1, 2));
            if (options.headingStyle == HeadingStyle.SETEXT && hLevel < 3) {
                String underline = String.join("", Collections.nCopies(content.length(), hLevel == 1 ? "=" : "-"));
                return "\n\n" + content + "\n" + underline + "\n\n";
            } else {
                return "\n\n" + String.join("", Collections.nCopies(hLevel, "#")) + " " + content + "\n\n";
            }
        }));
        addRule("blockquote", new Rule("blockquote", (content, element) -> {
            content = content.replaceAll("^\n+|\n+$", "");
            content = content.replaceAll("(?m)^\n+|\n+$", "");
            return "\n\n" + content + "\n\n";
        }));
        addRule("list", new Rule(new String[] { "ul", "ol" }, (content, element) -> {
            Element parent = (Element) element.parentNode();
            if (parent.nodeName().equals("li") && parent.child(parent.childrenSize() - 1) == element) {
                return "\n" + content;
            } else {
                return "\n\n" + content + "\n\n";
            }
        }));
        addRule("listItem", new Rule("li", (content, element) -> {
            content = content.replaceAll("^\n+", "") // remove leading new lines
            .replaceAll("\n+$", "\n") // remove trailing new lines with just a single one
            .replaceAll("(?m)\n", "\n    "); // indent
            String prefix = options.bulletListMaker + "    ";
            Element parent = (Element)element.parentNode();
            if (parent.nodeName().equals("ol")) {
                String start = parent.attr("start");
                int index = parent.children().indexOf(element);
                int parsedStart = 1;
                if (start.length() != 0) {
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
        addRule("indentedCodeBlock", new Rule((element) -> {
            return options.codeBlockStyle == CodeBlockStyle.INDENTED
                && element.nodeName().equals("pre")
                && element.childNodeSize() > 0
                && element.childNode(0).nodeName().equals("code");
        }, (content, element) -> {
            // TODO check textContent
            return "\n\n    " + element.childNode(0).outerHtml().replaceAll("/\n/", "\n    ");
        }));

        // TODO fencedCodeBlock

        addRule("horizontalRule", new Rule("hr", (content, element) -> {
            return "\n\n" + options.hr + "\n\n";
        }));
        addRule("inlineLink", new Rule((element) -> {
            return options.linkStyle == LinkStyle.INLINED
                    && element.nodeName().equals("a")
                    && element.attr("href").length() != 0;
        }, (content, element) -> {
            String href = element.attr("href");
            String title = cleanAttribute(element.attr("title"));
            if (title.length() != 0) {
                title = " \"" + title + "\"";
            }
            return "["+ content + "](" + href + title + ")";
        }));
        // TODO referenced link
        addRule("emphasis", new Rule(new String[]{"em", "i"}, (content, element) -> {
            if (content.trim().length() == 0) {
                return "";
            }
            return options.emDelimiter + content + options.emDelimiter;
        }));
        addRule("strong", new Rule(new String[]{"strong", "b"}, (content, element) -> {
            if (content.trim().length() == 0) {
                return "";
            }
            return options.strongDelimiter + content + options.strongDelimiter;
        }));
        addRule("code", new Rule((element) -> {
            boolean hasSiblings = element.previousSibling() != null || element.nextSibling() != null;
            boolean isCodeBlock = element.parentNode().nodeName().equals("pre") && !hasSiblings;
            return element.nodeName().equals("code") && !isCodeBlock;
        }, (content, element) -> {
            if (content.trim().length() == 0) {
                return "";
            }
            String delimiter = "`";
            String leadingSpace = "";
            String trailingSpace = "";
            Pattern pattern = Pattern.compile("(?m)(`)+");
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                if (content.matches("^`")) {
                    leadingSpace = " ";
                }
                if (content.matches("`$")) {
                    trailingSpace = " ";
                }
                int counter = 1;
                if (delimiter.equals(matcher.group())) {
                    counter++;
                }
                while (matcher.find()) {
                    if (delimiter.equals(matcher.group())) {
                        counter++;
                    }
                }
                delimiter = String.join("", Collections.nCopies(counter, "`"));
            }
            return delimiter + leadingSpace + content + trailingSpace + delimiter;
        }));
        addRule("img", new Rule("img", (content, element) -> {
            String alt = cleanAttribute(element.attr("alt"));
            String src = element.attr("src");
            if (src.length() == 0) {
                return "";
            }
            String title = cleanAttribute(element.attr("title"));
            String titlePart = "";
            if (title.length() != 0) {
                titlePart = " \"" + title + "\"";
            }
            return "![" + alt + "]" + "(" + src + titlePart + ")";
        }));
        addRule("default", new Rule((element -> true), (content, element) -> CopyNode.isBlock(element) ? "\n\n" + content + "\n\n" : content));
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

    private void addRule (String name, Rule rule) {
        rule.name = name;
        rules.add(rule);
    }
    private String cleanAttribute (String attribute) {
        return attribute.replaceAll("(\n+\\s*)+", "\n");
    }

}
