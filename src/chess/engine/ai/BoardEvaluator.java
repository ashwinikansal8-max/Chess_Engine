package chess.engine.ai;

import chess.engine.board.Board;
import chess.engine.pieces.Piece;
import chess.engine.player.Player;

public class BoardEvaluator {

    public int evaluate(final Board board) {
        final Player player = board.getCurrentPlayer();
        return score(player) - score(player.getOpponent());
    }

    private int score(final Player player) {
        int total = 0;
        for (final Piece piece : player.getActivePieces()) {
            total += piece.getPieceType().getPieceValue();
        }
        return total + player.getLegalMoves().size() * 3;
    }
}
