package common;

public class UpdateEvent {  // [from..to[ was replaced by text
    private int from;
    private int to;
    private String text;
    UpdateEvent(int a, int b, String t) { from = a; to = b; text = t; }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public String getText() {
        return text;
    }
}