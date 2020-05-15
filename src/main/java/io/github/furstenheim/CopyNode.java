package io.github.furstenheim;

import org.jsoup.nodes.Element;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CopyNode {
    private static final String[] VOID_ELEMENTS = {
            "AREA", "BASE", "BR", "COL", "COMMAND", "EMBED", "HR", "IMG", "INPUT",
            "KEYGEN", "LINK", "META", "PARAM", "SOURCE", "TRACK", "WBR"
    };
    private static final String[] MEANINGFUL_WHEN_BLANK_ELEMENTS = {
            "A", "TABLE", "THEAD", "TBODY", "TFOOT", "TH", "TD", "IFRAME", "SCRIPT",
            "AUDIO", "VIDEO"
            };

    private static final String[] BLOCK_ELEMENTS = {
            "ADDRESS", "ARTICLE", "ASIDE", "AUDIO", "BLOCKQUOTE", "BODY", "CANVAS",
            "CENTER", "DD", "DIR", "DIV", "DL", "DT", "FIELDSET", "FIGCAPTION", "FIGURE",
            "FOOTER", "FORM", "FRAMESET", "H1", "H2", "H3", "H4", "H5", "H6", "HEADER",
            "HGROUP", "HR", "HTML", "ISINDEX", "LI", "MAIN", "MENU", "NAV", "NOFRAMES",
            "NOSCRIPT", "OL", "OUTPUT", "P", "PRE", "SECTION", "TABLE", "TBODY", "TD",
            "TFOOT", "TH", "THEAD", "TR", "UL"
            };

    private static Set<String> VOID_ELEMENTS_SET = null;
    private static Set<String> MEANINGFUL_WHEN_BLANK_ELEMENTS_SET = null;
    private static Set<String> BLOCK_ELEMENTS_SET = null;

    Element element;
    CopyNode parent;

    public boolean isCode () {
        return element.nodeName().toLowerCase() == "code" || parent.isCode();
    }

    public boolean isBlank () {
        return !isVoid() &&
               !isMeaningfulWhenBlank() &&
               // TODO check text is the same as textContent in browser
               element.text().matches("/^\\s*$/i") &&
               ! hasVoidElementsSet() &&
               ! hasMeaningfulWhenBlankElementsSet();
    }
    public FlankingWhiteSpaces flankingWhitespace () {
        String leading = "";
        String trailing = "";
        if (!element.isBlock()) {
            boolean hasLeading = element.text().matches("^\\s");
            boolean hasTrailing = element.text().matches("\\s$");
            // TODO maybe make node property and avoid recomputing
            boolean blankWithSpaces = isBlank() && hasLeading && hasTrailing;
            if (hasLeading && !isLeftFlankedByWhitespaces()) {
                leading = " ";
            }
            if (!blankWithSpaces && hasTrailing && !isRightFlankedByWhitespaces()) {
                trailing = " ";
            }
        }
        return new FlankingWhiteSpaces(leading, trailing);
    }

    private boolean isLeftFlankedByWhitespaces () {
        return isChildFlankedByWhitespaces(" $", element.previousElementSibling());
    }
    private boolean isRightFlankedByWhitespaces () {
        return isChildFlankedByWhitespaces("^ ", element.nextElementSibling());
    }
    private boolean isChildFlankedByWhitespaces (String regex, Element sibling) {
        if (sibling == null) {
            return false;
        }
        if (isTextNodeType(sibling)) {
            // TODO fix. Originally sibling.nodeValue
            return sibling.text().matches(regex);
        }
        if (isElementNodeType(sibling)) {
            // TODO fix. Originally textContent
            return sibling.text().matches(regex);
        }
        return false;
    }
    // TODO fix original nodeType 3
    private boolean isTextNodeType (Element element) {
        return element.tagName() == "text";
    }
    // TODO fix original nodeType 1
    private boolean isElementNodeType (Element element) {
        return element.tagName() == "p" || element.tagName() == "div";

    }

    private boolean hasVoidElementsSet () {
        for (String tagName: VOID_ELEMENTS_SET) {
            if (element.getElementsByTag(tagName).size() != 0) {
                return true;
            }
        }
        return false;
    }
    private boolean isVoid () {
        return getVoidElementsSet().contains(element.tagName());
    }
    private static Set<String> getVoidElementsSet() {
        if (VOID_ELEMENTS_SET != null) {
            return VOID_ELEMENTS_SET;
        }
        VOID_ELEMENTS_SET = new HashSet<>(Arrays.asList(VOID_ELEMENTS));
        return VOID_ELEMENTS_SET;
    }

    private boolean hasMeaningfulWhenBlankElementsSet () {
        for (String tagName: MEANINGFUL_WHEN_BLANK_ELEMENTS_SET) {
            if (element.getElementsByTag(tagName).size() != 0) {
                return true;
            }
        }
        return false;
    }
    private boolean isMeaningfulWhenBlank () {
        return getMeaningfulWhenBlankElementsSet().contains(element.tagName());
    }
    private static Set<String> getMeaningfulWhenBlankElementsSet() {
        if (MEANINGFUL_WHEN_BLANK_ELEMENTS_SET != null) {
            return MEANINGFUL_WHEN_BLANK_ELEMENTS_SET;
        }
        MEANINGFUL_WHEN_BLANK_ELEMENTS_SET = new HashSet<>(Arrays.asList(MEANINGFUL_WHEN_BLANK_ELEMENTS));
        return MEANINGFUL_WHEN_BLANK_ELEMENTS_SET;
    }

    private boolean hasBlockElementsSet () {
        for (String tagName: BLOCK_ELEMENTS_SET) {
            if (element.getElementsByTag(tagName).size() != 0) {
                return true;
            }
        }
        return false;
    }
    private boolean isBlock () {
        return getBlockElementsSet().contains(element.tagName());
    }
    private static Set<String> getBlockElementsSet() {
        if (BLOCK_ELEMENTS_SET != null) {
            return BLOCK_ELEMENTS_SET;
        }
        BLOCK_ELEMENTS_SET = new HashSet<>(Arrays.asList(BLOCK_ELEMENTS));
        return BLOCK_ELEMENTS_SET;
    }

    public static class FlankingWhiteSpaces {
        public String getLeading() {
            return leading;
        }

        public String getTrailing() {
            return trailing;
        }

        private final String leading;
        private final String trailing;

        public FlankingWhiteSpaces(String leading, String trailing) {
            this.leading = leading;
            this.trailing = trailing;
        }
    }
}
