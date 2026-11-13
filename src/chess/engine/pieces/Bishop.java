package chess.engine.pieces;

import chess.engine.Alliance;
import chess.engine.board.Board;
import chess.engine.board.Move;

import java.util.Collections;
import java.util.List;

public class Bishop extends Piece {

    private static final int[] CANDIDATE_MOVE_DIRECTIONS = {-9, -7, 7, 9};

    public Bishop(final Alliance pieceAlliance, final int piecePosition) {
        this(pieceAlliance, piecePosition, true);
    }

    public Bishop(final Alliance pieceAlliance, final int piecePosition, final boolean isFirstMove) {
        super(PieceType.BISHOP, piecePosition, pieceAlliance, isFirstMove);
    }

    @Override
    public List<Move> calculateLegalMove(final Board board) {
        return Collections.unmodifiableList(calculateSlidingMoves(board, CANDIDATE_MOVE_DIRECTIONS));
    }

    @Override
    public Bishop movePiece(final Move move) {
        return new Bishop(move.getMovedPiece().getPieceAlliance(), move.getDestinationCoordinate(), false);
    }
}
