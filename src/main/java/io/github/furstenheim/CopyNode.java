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

    public boolean isBlank () {
        element.tag()
        return true;
    }

    private boolean isVoidElementsSet () {
        return getVoidElementsSet().contains(element.tagName());
    }
    private static Set<String> getVoidElementsSet() {
        if (VOID_ELEMENTS_SET != null) {
            return VOID_ELEMENTS_SET;
        }
        VOID_ELEMENTS_SET = new HashSet<>(Arrays.asList(VOID_ELEMENTS));
        return VOID_ELEMENTS_SET;
    }

    private boolean isMeaningfulWhenBlankElementsSet () {
        return getMeaningfulWhenBlankElementsSet().contains(element.tagName());
    }
    private static Set<String> getMeaningfulWhenBlankElementsSet() {
        if (MEANINGFUL_WHEN_BLANK_ELEMENTS_SET != null) {
            return MEANINGFUL_WHEN_BLANK_ELEMENTS_SET;
        }
        MEANINGFUL_WHEN_BLANK_ELEMENTS_SET = new HashSet<>(Arrays.asList(MEANINGFUL_WHEN_BLANK_ELEMENTS));
        return MEANINGFUL_WHEN_BLANK_ELEMENTS_SET;
    }

    private boolean isBlockElementsSet () {
        return getBlockElementsSet().contains(element.tagName());
    }
    private static Set<String> getBlockElementsSet() {
        if (BLOCK_ELEMENTS_SET != null) {
            return BLOCK_ELEMENTS_SET;
        }
        BLOCK_ELEMENTS_SET = new HashSet<>(Arrays.asList(BLOCK_ELEMENTS));
        return BLOCK_ELEMENTS_SET;
    }
}
