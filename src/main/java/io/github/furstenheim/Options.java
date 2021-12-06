package io.github.furstenheim;

public class Options {
    final String br;
    final String hr;
    final String emDelimiter;
    final String strongDelimiter;
    final String subDelimiter;
    final String supDelimiter;
    final HeadingStyle headingStyle;
    final String bulletListMaker;
    final CodeBlockStyle codeBlockStyle;
    final LinkStyle linkStyle;
    final LinkReferenceStyle linkReferenceStyle;
    final String fence;

    public Options(String br, String hr, String emDelimiter, String strongDelimiter, String subDelimiter,
            String supDelimiter, HeadingStyle headingStyle, String bulletListMaker, CodeBlockStyle codeBlockStyle,
            LinkStyle linkStyle, LinkReferenceStyle linkReferenceStyle, String fence) {
        this.br = br;
        this.hr = hr;
        this.emDelimiter = emDelimiter;
        this.strongDelimiter = strongDelimiter;
        this.subDelimiter = subDelimiter;
        this.supDelimiter = supDelimiter;
        this.headingStyle = headingStyle;
        this.bulletListMaker = bulletListMaker;
        this.codeBlockStyle = codeBlockStyle;
        this.linkStyle = linkStyle;
        this.linkReferenceStyle = linkReferenceStyle;
        this.fence = fence;
    }
}
