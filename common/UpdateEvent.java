package common;

public class UpdateEvent {  // [from..to[ was replaced by text
    public final int from;
    public final int to;
    public final String text;

    public UpdateEvent(int a, int b, String text) {
        from = a;
        to = b;
        this.text = text;
    }
}