package io.github.furstenheim;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main class of the package
 */
public class CopyDown {
    public CopyDown () {
        this.options = OptionsBuilder.anOptions().build();
        setUp();
    }

    public CopyDown (Options options) {
        this.options = options;
        setUp();
    }

    /**
     * Accepts a HTML string and converts it to markdown
     *
     * Note, if LinkStyle is chosen to be REFERENCED the method is not thread safe.
     * @param input html to be converted
     * @return markdown text
     */
    public String convert (String input) {
        references = new ArrayList<>();
        CopyNode copyRootNode = new CopyNode(input);
        String result = process(copyRootNode);
        return postProcess(result);
    }

    private Rules rules;
    private final Options options;
    private List<String> references = null;

    private void setUp () {
        rules = new Rules();
    }
    private static class Escape {
        String pattern;
        String replace;

        public Escape(String pattern, String replace) {
            this.pattern = pattern;
            this.replace = replace;
        }
    }
    private final List<Escape> escapes = Arrays.asList(
            new Escape("\\\\", "\\\\\\\\"),
            new Escape("\\*", "\\\\*"),
            new Escape("^-", "\\\\-"),
            new Escape("^\\+ ", "\\\\+ "),
            new Escape("^(=+)", "\\\\$1"),
            new Escape("^(#{1,6}) ", "\\\\$1 "),
            new Escape("`", "\\\\`"),
            new Escape("^~~~", "\\\\~~~"),
            new Escape("\\[", "\\\\["),
            new Escape("\\]", "\\\\]"),
            new Escape("^>", "\\\\>"),
            new Escape("_", "\\\\_"),
            new Escape("^(\\d+)\\. ", "$1\\\\. ")
    );

    private String postProcess (String output) {
        for (Rule rule: rules.rules) {
            if (rule.getAppend() != null) {
                output = join(output, rule.getAppend().get());
            }
        }
        return output.replaceAll("^[\\t\\n\\r]+", "").replaceAll("[\\t\\r\\n\\s]+$", "");
    }
    private String process (CopyNode node) {
        String result = "";
        for (Node child : node.element.childNodes()) {
            CopyNode copyNodeChild = new CopyNode(child, node);
            String replacement = "";
            if (NodeUtils.isNodeType3(child)) {
                // TODO it should be child.nodeValue
                replacement = copyNodeChild.isCode() ? ((TextNode)child).text() : escape(((TextNode)child).text());
            } else if (NodeUtils.isNodeType1(child)) {
                replacement = replacementForNode(copyNodeChild);
            }
            result = join(result, replacement);
        }
        return result;
    }
    private String replacementForNode (CopyNode node) {
        Rule rule = rules.findRule(node.element);
        String content = process(node);
        CopyNode.FlankingWhiteSpaces flankingWhiteSpaces = node.flankingWhitespace();
        if (flankingWhiteSpaces.getLeading().length() > 0 || flankingWhiteSpaces.getTrailing().length() > 0) {
            content = content.trim();
        }
        return flankingWhiteSpaces.getLeading() + rule.getReplacement().apply(content, node.element)
         + flankingWhiteSpaces.getTrailing();
    }
    private static final Pattern leadingNewLinePattern = Pattern.compile("^(\n*)");
    private static final Pattern trailingNewLinePattern = Pattern.compile("(\n*)$");
    private String join (String string1, String string2) {
        Matcher trailingMatcher = trailingNewLinePattern.matcher(string1);
        trailingMatcher.find();
        Matcher leadingMatcher = leadingNewLinePattern.matcher(string2);
        leadingMatcher.find();
        int nNewLines = Integer.min(2, Integer.max(leadingMatcher.group().length(), trailingMatcher.group().length()));
        String newLineJoin = String.join("", Collections.nCopies(nNewLines, "\n"));
        return trailingMatcher.replaceAll("")
                + newLineJoin
                + leadingMatcher.replaceAll("");
    }

    private String escape (String string) {
        for (Escape escape : escapes) {
            string = string.replaceAll(escape.pattern, escape.replace);
        }
        return string;
    }

    class Rules {
        private List<Rule> rules;

        public Rules () {
            this.rules = new ArrayList<>();

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
                content = content.replaceAll("(?m)^", "> ");
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
                String prefix = options.bulletListMaker + "   ";
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
                    prefix = String.valueOf(parsedStart + index) + ".  ";
                }
                return prefix + content + (element.nextSibling() != null && !Pattern.compile("\n$").matcher(content).find() ? "\n": "");
            }));
            addRule("indentedCodeBlock", new Rule((element) -> {
                return options.codeBlockStyle == CodeBlockStyle.INDENTED
                       && element.nodeName().equals("pre")
                       && element.childNodeSize() > 0
                       && element.childNode(0).nodeName().equals("code");
            }, (content, element) -> {
                // TODO check textContent
                return "\n\n    " + ((Element)element.childNode(0)).wholeText().replaceAll("\n", "\n    ");
            }));
            addRule("fencedCodeBock", new Rule((element) -> {
                return options.codeBlockStyle == CodeBlockStyle.FENCED
                       && element.nodeName().equals("pre")
                       && element.childNodeSize() > 0
                       && element.childNode(0).nodeName().equals("code");
            }, (content, element) -> {
                String childClass = element.childNode(0).attr("class");
                if (childClass == null) {
                    childClass = "";
                }
                Matcher languageMatcher = Pattern.compile("language-(\\S+)").matcher(childClass);
                String language = "";
                if (languageMatcher.find()) {
                    language = languageMatcher.group(1);
                }

                String code;
                if (element.childNode(0) instanceof Element) {
                    code = ((Element)element.childNode(0)).wholeText();
                } else {
                    code = element.childNode(0).outerHtml();
                }

                String fenceChar = options.fence.substring(0, 1);
                int fenceSize = 3;
                Matcher fenceMatcher = Pattern.compile("(?m)^(" + fenceChar + "{3,})").matcher(content);
                while (fenceMatcher.find()) {
                    String group = fenceMatcher.group(1);
                    fenceSize = Math.max(group.length() + 1, fenceSize);
                }
                String fence = String.join("", Collections.nCopies(fenceSize, fenceChar));
                if (code.length() > 0 && code.charAt(code.length() - 1) == '\n') {
                    code = code.substring(0, code.length() - 1);
                }
                return (
                        "\n\n" + fence + language + "\n" + code
                         + "\n" + fence + "\n\n"
                        );
            }));

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
            addRule("referenceLink", new Rule((element) -> {
                return options.linkStyle == LinkStyle.REFERENCED
                       && element.nodeName().equals("a")
                       && element.attr("href").length() != 0;
            }, (content, element) -> {
                String href = element.attr("href");
                String title = cleanAttribute(element.attr("title"));
                if (title.length() != 0) {
                    title = " \"" + title + "\"";
                }
                String replacement;
                String reference;
                switch (options.linkReferenceStyle) {
                    case COLLAPSED:
                        replacement = "[" + content + "][]";
                        reference = "[" + content + "]: " + href + title;
                        break;
                    case SHORTCUT:
                        replacement = "[" + content + "]";
                        reference = "[" + content + "]: " + href + title;
                        break;
                    case DEFAULT:
                    default:
                        int id = references.size() + 1;
                        replacement = "[" + content + "][" + id + "]";
                        reference = "[" + id + "]: " + href + title;
                }
                references.add(reference);
                return replacement;
            }, () -> {
                String referenceString = "";
                if (references.size() > 0) {
                    referenceString = "\n\n" + String.join("\n", references) + "\n\n";
                }
                return referenceString;
            }));
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
            addRule("sup", new Rule(new String[]{"sup"}, (content, element) -> {
                if (content.trim().length() == 0) {
                    return "";
                }
                return options.supDelimiter + content + options.supDelimiter.replace("<", "</");
            }));
            addRule("sub", new Rule(new String[]{"sub"}, (content, element) -> {
                if (content.trim().length() == 0) {
                    return "";
                }
                return options.subDelimiter + content + options.subDelimiter.replace("<", "</");
            }));
            addRule("code", new Rule((element) -> {
                boolean hasSiblings = element.previousSibling() != null || element.nextSibling() != null;
                boolean isCodeBlock = element.parentNode().nodeName().equals("pre") && !hasSiblings;
                return element.nodeName().equals("code") && !isCodeBlock;
            }, (content, element) -> {
                if (content.trim().length() == 0) {
                    return "";
                }
                String delimiter = content.contains("<sup") || content.contains("<sub") ? "<code>" : "`";
                String leadingSpace = "";
                String trailingSpace = "";
                Pattern pattern = Pattern.compile("(?m)(`)+");
                Matcher matcher = pattern.matcher(content);
                if (matcher.find()) {
                    if (Pattern.compile("^`").matcher(content).find()) {
                        leadingSpace = " ";
                    }
                    if (Pattern.compile("`$").matcher(content).find()) {
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
                return delimiter + leadingSpace + content + trailingSpace + delimiter.replace("<", "</");
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
            for (Rule rule : rules) {
                if (rule.getFilter().test(node)) {
                    return rule;
                }
            }
            return null;
        }

        private void addRule (String name, Rule rule) {
            rule.setName(name);
            rules.add(rule);
        }
        private String cleanAttribute (String attribute) {
            return attribute.replaceAll("(\n+\\s*)+", "\n");
        }
    }
}
