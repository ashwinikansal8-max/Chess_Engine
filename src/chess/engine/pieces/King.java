package chess.engine.pieces;

import chess.engine.Alliance;
import chess.engine.board.Board;
import chess.engine.board.Move;

import java.util.Collections;
import java.util.List;

public class King extends Piece {

    private static final int[] CANDIDATE_MOVE_OFFSETS = {-9, -8, -7, -1, 1, 7, 8, 9};

    public King(final Alliance pieceAlliance, final int piecePosition) {
        this(pieceAlliance, piecePosition, true);
    }

    public King(final Alliance pieceAlliance, final int piecePosition, final boolean isFirstMove) {
        super(PieceType.KING, piecePosition, pieceAlliance, isFirstMove);
    }

    @Override
    public List<Move> calculateLegalMove(final Board board) {
        return Collections.unmodifiableList(calculateStepMoves(board, CANDIDATE_MOVE_OFFSETS, 1));
    }

    @Override
    public King movePiece(final Move move) {
        return new King(move.getMovedPiece().getPieceAlliance(), move.getDestinationCoordinate(), false);
    }
}
