package org.taitascioredev.fractal;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by roberto on 25/05/15.
 */
public class FormattedLink {

    public static final int TYPE_SUBREDDIT = 1;
    public static final int TYPE_USER = 2;
    public static final int TYPE_MULTIREDDIT = 3;

    private String text;
    private int type = -1;
    private int start = -1;
    private int end = -1;

    public FormattedLink(String text, int type) {
        this.text = text;
        this.type = type;
    }

    public FormattedLink(String text) { this.text = text; }

    public void setText(String text) { this.text = text; }

    public void setType(int type) { this.type = type; }

    public void setStart(int start) { this.start = start; }

    public void setEnd(int end) { this.end = end; }

    public String getText() { return text; }

    public int getType() { return type; }

    public int getStart() { return start; }

    public int getEnd() { return end; }

    public static List<FormattedLink> extract(String str) {
        List<FormattedLink> list = new ArrayList<>();
        String regexp = "/[rum]/[\\w[-]]{3,20}";
        Pattern p = Pattern.compile(regexp);
        Matcher m = p.matcher(str);
        while (m.find()) {
            int start = m.start();
            int end = m.end();
            String match = str.substring(start, end);
            FormattedLink link = new FormattedLink(match);
            link.setStart(start);
            link.setEnd(end);
            if (match.startsWith("/r/")) link.setType(TYPE_SUBREDDIT);
            else if (match.startsWith("/u/")) link.setType(TYPE_USER);
            else link.setType(TYPE_MULTIREDDIT);
            list.add(link);
        }
        return list;
    }
}
