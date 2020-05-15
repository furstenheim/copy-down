package io.github.furstenheim;

import org.jsoup.nodes.Element;

/**
 * The Whitespace collapser is originally adapted from collapse-whitespace
 * by Luc Thevenard.
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Luc Thevenard <lucthevenard@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

public class WhitespaceCollapser {
    /**
     * Remove extraneous whitespace from the given element
     * @param element
     */
    public void collapse (Element element) {
        if (element.childrenSize() == 0 || isPre(element)) {
            return;
        }

        Element prevText = null;
        boolean prevVoid = false;
        Element prev = null;
        Element node = next(prev, element);

        // Traverse the tree
        while (node != element) {
            if (NodeUtils.isNodeType3(element) || NodeUtils.isNodeType4(element)) {
                String text = node.data().replaceAll("[ \\r\\n\\t]+", " ");
                if ((prevText != null || prevText.data().matches(" $")) || (!prevVoid && text.charAt(0) == ' ')) {
            text = text.substring(1);
                }
                if (text.length() == 0) {
                    node = remove(node);
                    continue;
                }
                // TODO not available in jsoup. Maybe by parsing new node and inserting
                // node.data = text
                // node.replaceWith();
                // prevText = node ?¿
            } else if (NodeUtils.isNodeType1(element)) {
                if (isBlock(element)) {
                    if (prevText != null) {
                        // prevtext.data = prevText.data.replace(/ $/, '')
                    }
                    prevText = null;
                    prevVoid = false;
                } else if (isVoid(element)) {
                    // avoid trimming space around non block, non br void elements
                    prevText = null;
                    prevVoid = true;
                }
            } else {
                node = remove(node);
                continue;
            }
            Element nextNode = next(prev, node);
            prev = node;
            node = nextNode;
        }
        if (prevText != null) {
            // prevText.data = prevText.data.replace(/ $/, '')
            if (prevText.data() == null) {
                remove(prevText);
            }
        }

    }

    /**
     * remove(node) removes the given node from the DOM and returns the
     * next node in the sequence.
     *
     * @param {Node} node
     * @return {Node} node
     */
    private Element remove (Element node) {
        Element next = node.nextElementSibling() != null ? node.nextElementSibling() : (Element)node.parentNode();
        node.remove();
        return next;
    }
    /**
     * Returns next node in the sequence given current and previous nodes
     */
    private Element next (Element prev, Element current) {
        if ((prev != null && prev.parent() == current) || isPre(current)) {
            // TODO beware parentNode might not be element
            return current.nextElementSibling() != null ? current.nextElementSibling() : (Element)current.parentNode();
        }
        if (current.childrenSize() != 0) {
            return current.child(0);
        }
        if (current.nextElementSibling() != null) {
            return current.nextElementSibling();
        }
        return (Element)current.parentNode();
    }
    private boolean isPre (Element element) {
        // TODO allow to override with lambda in options
        return element.nodeName() == "PRE";
    }

    private boolean isBlock (Element element) {
        // TODO allow to override with lambda in optiosn
        return CopyNode.isBlock(element) ||  element.nodeName() == "BR";
    }

    private boolean isVoid (Element element) {
        // Allow to override
        return CopyNode.isVoid(element);
    }

}
