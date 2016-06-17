package org.taitascioredev.fractal;

/**
 * Created by roberto on 27/05/15.
 */
public class Replacer {

    private String match;
    private String text;
    private String link;

    public Replacer(String match, String text, String link) {
        this.match = match;
        this.text = text;
        this.link = link;
    }

    public Replacer(String match, String text) {
        this(match, text, null);
    }

    public String getMatch() { return match; }
    public String getText() { return text; }
    public String getLink() { return link; }
}
