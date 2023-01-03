package contract;

import piecelist.Piece;

public interface PieceListContract extends TextContract {
    Piece split(int pos);
}
