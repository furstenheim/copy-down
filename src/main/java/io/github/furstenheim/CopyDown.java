package io.github.furstenheim;

import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CopyDown {
    Rules rules;
    Options options;
    public CopyDown (Options options) {
        this.options = options;
        setUp();
    }
    public CopyDown () {
        this.options = OptionsBuilder.anOptions().build();
        setUp();
    }
    private void setUp () {
        rules = new Rules(options);
    }
    public String convert (String input) {
        CopyNode copyRootNode = new CopyNode(input);
        String result = process(copyRootNode);
        return postProcess(result);
    }
    private class Escape {
        String pattern;
        String replace;

        public Escape(String pattern, String replace) {
            this.pattern = pattern;
            this.replace = replace;
        }
    }
    List<Escape> escapes = Arrays.asList(
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
        // TODO append logic
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
}
