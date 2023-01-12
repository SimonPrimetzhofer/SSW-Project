package viewer;

public class Position {
    public Line line; // line containing this position
    public int x, y;  // base line point corresponding to this position
    public int tpos;  // text position (relative to start of text)
    public int org;   // origin (text position of first character in this line)
    public int off;   // text offset from org
}
