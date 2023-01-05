package viewer;

import common.UpdateEvent;
import piecelist.PieceListText;

import java.awt.*;
import java.awt.event.*;
import javax.swing.JScrollBar;
import java.util.Collections;

class Line {
    String text;    // text of this line
    int len;        // length of this line (including CRLF)
    int x, y, w, h; // top left corner, width, height
    int base;       // base line
    Line prev, next;
}

class Position {
    Line line; // line containing this position
    int x, y;  // base line point corresponding to this position
    int tpos;  // text position (relative to start of text)
    int org;   // origin (text position of first character in this line)
    int off;   // text offset from org
}

class Selection {
    Position beg, end;
    Selection (Position a, Position b) { beg = a; end = b; }
}

/**********************************************************************
 *  legacy.Viewer
 **********************************************************************/

public class PieceListViewer extends Canvas {
    static final int    TOP = 5;    // top margin
    static final int    BOTTOM = 5; // bottom margin
    static final int    LEFT = 5;   // left margin
    static final int    EOF = '\0';
    static final char CR = '\r';
    static final char LF = '\n';

    PieceListText text;
    Line firstLine = null; // the lines in this viewer
    int firstTpos = 0;     // first text position in this viewer
    int lastTpos;          // last text position in this viewer
    Selection sel = null;	 // current selection
    Position caret;				 // current caret
    Position lastPos;      // last mouse position: used during mouse dragging
    JScrollBar scrollBar;
    Graphics g;

    public PieceListViewer(PieceListText t, JScrollBar sb) {
        scrollBar = sb;
        scrollBar.setMaximum(t.getLen());
        scrollBar.setUnitIncrement(50);
        scrollBar.setBlockIncrement(500);
        scrollBar.addAdjustmentListener(new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent e) { doScroll(e); }
        });
        text = t;
        text.addUpdateEventListener(e -> doUpdate(e));
        this.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) { doKeyTyped(e); }
            public void keyPressed(KeyEvent e) { doKeyPressed(e); }
        });
        this.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2) {
                    doMouseDoubleClicked(e);
                }
            }
            public void mousePressed(MouseEvent e) { doMousePressed(e); }
            public void mouseReleased(MouseEvent e) { doMouseReleased(e); }
        });
        this.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) { doMouseDragged(e); }
        });
        // disable TAB as a focus traversal key
        setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.<AWTKeyStroke>emptySet());
    }

    /*------------------------------------------------------------
     *  scrolling
     *-----------------------------------------------------------*/

    public void doScroll(AdjustmentEvent e) {
        int pos = e.getValue();
        if (pos > 0) { // find start of line
            char ch;
            do { ch = text.charAt(--pos); } while (pos > 0 && ch != '\n');
            if (pos > 0) pos++;
        }
        if (pos != firstTpos) { // scroll
            Position caret0 = caret;
            Selection sel0 = sel;
            removeSelection();
            removeCaret();
            firstTpos = pos;
            firstLine = fill(TOP, getHeight() - BOTTOM, firstTpos);
            repaint();
            if (caret0 != null) setCaret(caret0.tpos);
            if (sel0 != null) setSelection(sel0.beg.tpos, sel0.end.tpos);
        }
    }

    /*------------------------------------------------------------
     *  position handling
     *-----------------------------------------------------------*/

    private Position Pos(int tpos) {
        if (tpos < firstTpos) tpos = firstTpos;
        if (tpos > lastTpos) tpos = lastTpos;
        Position pos = new Position();
        setDefaultFont(g);
        Line line = firstLine, last = null;
        pos.org = firstTpos;
        while (line != null && tpos >= pos.org + line.len) {
            pos.org += line.len;
            last = line;
            line = line.next;
        }
        if (line == null) {
            pos.x = last.x + last.w;
            pos.y = last.base;
            pos.line = last;
            pos.org -= last.len;
            pos.off = last.len;
        } else {
            pos.x = line.x;
            pos.y = line.base;
            pos.line = line;
            pos.off = tpos - pos.org;
            FontMetrics m = g.getFontMetrics();
            int i = pos.org;
            while (i < tpos) {
                char ch = text.charAt(i); i++;
                pos.x += charWidth(m, ch);
            }
        }
        pos.tpos = pos.org + pos.off;
        return pos;
    }

    private Position Pos(int x, int y) {
        Position pos = new Position();
        setDefaultFont(g);
        if (y >= getHeight() - BOTTOM) y = getHeight() - BOTTOM - 1;
        Line line = firstLine, last = null;
        pos.org = firstTpos;
        while (line != null && y >= line.y + line.h) {
            pos.org += line.len;
            last = line;
            line = line.next;
        }
        if (line == null) { line = last; pos.org -= last.len; }
        pos.y = line.base;
        pos.line = line;
        if (x >= line.x + line.w) {
            pos.x = line.x + line.w;
            pos.off = line.len;
            if (pos.org + line.len < text.getLen()) pos.off -= 2;
        } else {
            pos.x = line.x;
            FontMetrics m = g.getFontMetrics();
            int i = pos.org;
            char ch = text.charAt(i);
            int w = charWidth(m, ch);
            while (x >= pos.x + w) {
                pos.x += w;
                i++; ch = text.charAt(i);
                w = charWidth(m, ch);
            }
            pos.off = i - pos.org;
        }
        pos.tpos = pos.org + pos.off;
        return pos;
    }

    /*------------------------------------------------------------
     *  caret handling
     *-----------------------------------------------------------*/

    private void invertCaret() {
        g = getGraphics();
        g.setXORMode(Color.WHITE);
        int x = caret.x;
        int y = caret.y;
        g.drawLine(x, y, x, y); y++;
        g.drawLine(x, y, x + 1, y); y++;
        g.drawLine(x, y, x + 2, y); y++;
        g.drawLine(x, y, x + 3, y); y++;
        g.drawLine(x, y, x + 4, y); y++;
        g.drawLine(x, y, x + 5, y);
        g.setPaintMode();
    }

    private void setCaret(Position pos) {
        removeCaret(); removeSelection();
        caret = pos;
        invertCaret();
    }

    public void setCaret(int tpos) {
        if (tpos >= firstTpos && tpos <= lastTpos) {
            setCaret(Pos(tpos));
        } else caret = null;
    }

    public void setCaret(int x, int y) {
        setCaret(Pos(x, y));
    }

    public void removeCaret() {
        if (caret != null) invertCaret();
        caret = null;
    }

    /*------------------------------------------------------------
     *  selection handling
     *-----------------------------------------------------------*/

    private void invertSelection(Position beg, Position end) {
        g = getGraphics();
        setDefaultFont(g);
        g.setXORMode(Color.WHITE);
        Line line = beg.line;
        int x = beg.x;
        int y = line.y;
        int w;
        int h = line.h;
        while (line != end.line) {
            w = line.w + LEFT - x;
            g.fillRect(x, y, w, h);
            line = line.next;
            x = line.x; y = line.y;
        }
        w = end.x - x;
        g.fillRect(x, y, w, h);
        g.setPaintMode();
    }

    public void setSelection(int from, int to) {
        if (from < to) {
            removeCaret();
            Position beg = Pos(from);
            Position end = Pos(to);
            sel = new Selection(beg, end);
            invertSelection(beg, end);
        } else sel = null;
    }

    public void removeSelection() {
        if (sel != null) invertSelection(sel.beg, sel.end);
        sel = null;
    }

    /*------------------------------------------------------------
     *  keyboard handling
     *-----------------------------------------------------------*/

    private void doKeyTyped(KeyEvent e) {
        boolean selection = sel != null;
        if (selection) {
            text.delete(sel.beg.tpos, sel.end.tpos);
            // selection is removed; caret is set at sel.beg.tpos
        }
        if (caret != null) {
            char ch = e.getKeyChar();
            if (ch == KeyEvent.VK_BACK_SPACE) {
                if (caret.tpos > 0 && !selection) {
                    int d = caret.off == 0 ? 2 : 1;
                    text.delete(caret.tpos - d, caret.tpos);
                }
            } else if (ch == KeyEvent.VK_ESCAPE) {
            } else if (ch == KeyEvent.VK_ENTER) {
                text.insert(caret.tpos, CR);
                text.insert(caret.tpos, LF);
            } else {
                text.insert(caret.tpos, ch);
            }
            scrollBar.setValues(firstTpos, 0, 0, text.getLen());
        }
    }

    private void doKeyPressed(KeyEvent e) { // for cursor keys
        if (caret != null) {
            int key = e.getKeyCode();
            int pos = caret.tpos;
            char ch;
            if (key == KeyEvent.VK_RIGHT) {
                pos++; ch = text.charAt(pos);
                if (ch == '\n') pos++;
                setCaret(pos);
            } else if (key == KeyEvent.VK_LEFT) {
                pos--; ch = text.charAt(pos);
                if (ch == '\n') pos--;
                setCaret(pos);
            } else if (key == KeyEvent.VK_UP) {
                setCaret(caret.x, caret.y - caret.line.h);
            } else if (key == KeyEvent.VK_DOWN) {
                setCaret(caret.x, caret.y + caret.line.h);
            }
        }
    }

    /*------------------------------------------------------------
     *  mouse handling
     *-----------------------------------------------------------*/

    private void doMouseDoubleClicked(MouseEvent e) {
        Position pos = Pos(e.getX(), e.getY());

        // TODO: code verschönern -> zweifacher zugriff auf charAt
        // find left border of word
        int leftCharCount = 0;
        while (!Character.isWhitespace(text.charAt(pos.tpos - 1 - leftCharCount)) && text.charAt(pos.tpos - 1 - leftCharCount) != '\0') {
            leftCharCount++;
        }

        // find right border of word
        int rightCharCount = 0;
        while (!Character.isWhitespace(text.charAt(pos.tpos + rightCharCount)) && text.charAt(pos.tpos + rightCharCount) != '\0') {
            rightCharCount++;
        }
        setSelection(pos.tpos - leftCharCount, pos.tpos + rightCharCount);
        lastPos = pos;
    }

    private void doMousePressed(MouseEvent e) {
        removeCaret(); removeSelection();
        Position pos = Pos(e.getX(), e.getY());
        sel = new Selection(pos, pos);
        lastPos = pos;
    }

    private void doMouseDragged(MouseEvent e) {
        if (sel == null) return;
        Position pos = Pos(e.getX(), e.getY());
        if (pos.tpos < sel.beg.tpos) {
            if (lastPos.tpos == sel.end.tpos) {
                invertSelection(sel.beg, lastPos);
                sel.end = sel.beg;
            }
            invertSelection(pos, sel.beg);
            sel.beg = pos;
        } else if (pos.tpos > sel.end.tpos) {
            if (lastPos.tpos == sel.beg.tpos) {
                invertSelection(lastPos, sel.end);
                sel.beg = sel.end;
            }
            invertSelection(sel.end, pos);
            sel.end = pos;
        } else if (pos.tpos < lastPos.tpos) { // beg <= pos <= end; clear pos..end
            invertSelection(pos, sel.end);
            sel.end = pos;
        } else if (lastPos.tpos < pos.tpos) { // beg <= pos <= end; clear beg..pos
            invertSelection(sel.beg, pos);
            sel.beg = pos;
        }
        lastPos = pos;
    }

    private void doMouseReleased(MouseEvent e) {
        if (sel.beg.tpos == sel.end.tpos) setCaret(sel.beg);
        lastPos = null;
    }

    /*------------------------------------------------------------
     *  TAB handling
     *-----------------------------------------------------------*/

    private int charWidth(FontMetrics m, char ch) {
        if (ch == '\t') return 4 * m.charWidth(' '); else return m.charWidth(ch);
    }

    private int stringWidth(FontMetrics m, String s) {
        String s1 = s.replaceAll("\t", "    ");
        return m.stringWidth(s1);
    }

    private void drawString(Graphics g, String s, int x, int y) {
        String s1 = s.replaceAll("\t", "    ");
        g.drawString(s1, x, y);
    }

    /*------------------------------------------------------------
     *  line handling
     *-----------------------------------------------------------*/

    private Line fill(int top, int bottom, int pos) {
        g = getGraphics();
        setDefaultFont(g);
        FontMetrics m = g.getFontMetrics();
        Line first = null, line = null;
        int y = top;
        lastTpos = pos;
        char ch = text.charAt(pos);
        while (y < bottom) {
            if (first == null) {
                first = line = new Line();
            } else {
                Line prev = line;
                line.next = new Line(); line = line.next; line.prev = prev;
            }
            StringBuffer buf = new StringBuffer();
            while (ch != '\n' && ch != EOF) {
                buf.append(ch);
                pos++;
                ch = text.charAt(pos);
            }
            boolean eol = ch == '\n';
            if (eol) { buf.append(ch); pos++; ch = text.charAt(pos); }
            line.len = buf.length();
            line.text = buf.toString();
            line.x = LEFT;
            line.y = y;
            line.w = stringWidth(m, line.text);
            line.h = m.getHeight();
            line.base = y + m.getAscent();
            y += line.h;
            lastTpos += line.len;
            if (!eol) break;
        }
        return first;
    }

    private void rebuildFrom(Position pos) {
        Line line = pos.line;
        Line prev = line.prev;
        line = fill(line.y, getHeight() - BOTTOM, pos.org);
        if (prev == null) firstLine = line; else { prev.next = line; line.prev = prev; }
        repaint(LEFT, line.y, getWidth(), getHeight());
    }

    /*------------------------------------------------------------
     *  text drawing
     *-----------------------------------------------------------*/

    public void doUpdate(UpdateEvent e) {
        StringBuffer b;
        g = getGraphics();
        setDefaultFont(g);
        FontMetrics m = g.getFontMetrics();
        Position pos = caret;

        if (e.getFrom() == e.getTo()) { // insert
            if (e.getFrom() != caret.tpos) pos = Pos(e.getFrom());
            int newCarPos = pos.tpos + e.getText().length();
            if (e.getText().indexOf(CR) >= 0) {
                rebuildFrom(pos);
                if (pos.y + pos.line.h > getHeight() - BOTTOM)
                    scrollBar.setValue(firstTpos + firstLine.len);
            } else {
                b = new StringBuffer(pos.line.text);
                b.insert(pos.off, e.getText());
                pos.line.text = b.toString();
                pos.line.w += stringWidth(m, e.getText());
                pos.line.len += e.getText().length();
                lastTpos += e.getText().length();
                repaint(pos.line.x, pos.line.y, getWidth(), pos.line.h+1);
            }
            setCaret(newCarPos);

        } else if (e.getText() == null) { // delete
            if (caret == null || e.getTo() != caret.tpos) pos = Pos(e.getTo());
            int d = e.getTo() - e.getFrom();
            if (pos.off - d < 0) { // delete across lines
                rebuildFrom(Pos(e.getFrom()));
            } else { // delete within a line
                b = new StringBuffer(pos.line.text);
                b.delete(pos.off - d, pos.off);
                pos.line.text = b.toString();
                pos.line.w = stringWidth(m, pos.line.text);
                pos.line.len -= d;
                lastTpos -= d;
                repaint(pos.line.x, pos.line.y, getWidth(), pos.line.h+1);
            }
            setCaret(e.getFrom());
        }
    }

    private void setDefaultFont(Graphics g) {
        g.setFont(g.getFont().deriveFont(18f));
    }

    public void paint(Graphics g) {
        setDefaultFont(g);
        if (firstLine == null) {
            firstLine = fill(TOP, getHeight() - BOTTOM, 0);
            caret = Pos(0);
        }
        Line line = firstLine;
        while (line != null) {
            drawString(g, line.text, line.x, line.base);
            line = line.next;
        }
        if (caret != null) invertCaret();
        if (sel != null) invertSelection(sel.beg, sel.end);
    }
}