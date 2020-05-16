package io.github.furstenheim;

import org.jsoup.nodes.Element;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class Rule {
    private Predicate<Element> filter;
    private BiFunction<String, Element, String> replacement;

    public Rule (String filter, BiFunction<String, Element, String> replacement) {
        this.filter = (el) -> el.tagName().toLowerCase() == filter;
        this.replacement = replacement;
    }

    public Rule (String[] filters, BiFunction<String, Element, String> replacement) {
        Set<String> availableFilters = new HashSet<String>(Arrays.asList(filters));
        filter = (element -> availableFilters.contains(element.tagName()));
        this.replacement = replacement;
    }
    public Rule(Predicate<Element> filter, BiFunction<String, Element, String> replacement) {
        this.filter = filter;
        this.replacement = replacement;
    }
}
