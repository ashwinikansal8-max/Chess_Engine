package chess.engine.pieces;

import chess.engine.Alliance;
import chess.engine.board.Board;
import chess.engine.board.Move;

import java.util.Collections;
import java.util.List;

public class Rook extends Piece {

    private static final int[] CANDIDATE_MOVE_DIRECTIONS = {-8, -1, 1, 8};

    public Rook(final Alliance pieceAlliance, final int piecePosition) {
        this(pieceAlliance, piecePosition, true);
    }

    public Rook(final Alliance pieceAlliance, final int piecePosition, final boolean isFirstMove) {
        super(PieceType.ROOK, piecePosition, pieceAlliance, isFirstMove);
    }

    @Override
    public List<Move> calculateLegalMove(final Board board) {
        return Collections.unmodifiableList(calculateSlidingMoves(board, CANDIDATE_MOVE_DIRECTIONS));
    }

    @Override
    public Rook movePiece(final Move move) {
        return new Rook(move.getMovedPiece().getPieceAlliance(), move.getDestinationCoordinate(), false);
    }
}
