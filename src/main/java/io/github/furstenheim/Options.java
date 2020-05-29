package io.github.furstenheim;

public class Options {
    public final String br;
    public final String hr;
    public final String emDelimiter;
    public final String strongDelimiter;
    public final HeadingStyle headingStyle;
    public final String bulletListMaker;
    public final CodeBlockStyle codeBlockStyle;
    public final LinkStyle linkStyle;

    public Options(String br, String hr, String emDelimiter, String strongDelimiter,
            HeadingStyle headingStyle, String bulletListMaker, CodeBlockStyle codeBlockStyle,
            LinkStyle linkStyle) {
        this.br = br;
        this.hr = hr;
        this.emDelimiter = emDelimiter;
        this.strongDelimiter = strongDelimiter;
        this.headingStyle = headingStyle;
        this.bulletListMaker = bulletListMaker;
        this.codeBlockStyle = codeBlockStyle;
        this.linkStyle = linkStyle;
    }

    public static final class OptionsBuilder {
        public String br = "  ";
        public String hr = "* * *";
        public String emDelimiter = "_";
        public String strongDelimiter = "**";
        public HeadingStyle headingStyle = HeadingStyle.SETEXT;
        public String bulletListMaker = "*";
        public CodeBlockStyle codeBlockStyle = CodeBlockStyle.INDENTED;
        public LinkStyle linkStyle = LinkStyle.INLINED;

        public OptionsBuilder() {
        }

        public static OptionsBuilder anOptions() {
            return new OptionsBuilder();
        }

        public OptionsBuilder withBr(String br) {
            this.br = br;
            return this;
        }

        public OptionsBuilder withHr(String hr) {
            this.hr = hr;
            return this;
        }

        public OptionsBuilder withEmDelimiter(String emDelimiter) {
            this.emDelimiter = emDelimiter;
            return this;
        }

        public OptionsBuilder withStrongDelimiter(String strongDelimiter) {
            this.strongDelimiter = strongDelimiter;
            return this;
        }

        public OptionsBuilder withHeadingStyle(HeadingStyle headingStyle) {
            this.headingStyle = headingStyle;
            return this;
        }

        public OptionsBuilder withBulletListMaker(String bulletListMaker) {
            this.bulletListMaker = bulletListMaker;
            return this;
        }

        public OptionsBuilder withCodeBlockStyle(CodeBlockStyle codeBlockStyle) {
            this.codeBlockStyle = codeBlockStyle;
            return this;
        }

        public OptionsBuilder withLinkStyle(LinkStyle linkStyle) {
            this.linkStyle = linkStyle;
            return this;
        }

        public Options build() {
            return new Options(br, hr, emDelimiter, strongDelimiter, headingStyle, bulletListMaker, codeBlockStyle,
                               linkStyle);
        }
    }
}
