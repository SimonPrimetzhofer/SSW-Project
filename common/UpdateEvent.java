package common;

public class UpdateEvent {  // [from..to[ was replaced by text
    private final int from;
    private final int to;
    private final char ch;

    public UpdateEvent(int a, int b, char ch) {
        from = a;
        to = b;
        this.ch = ch;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public char getCh() {
        return ch;
    }
}