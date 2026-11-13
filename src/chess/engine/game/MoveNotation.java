package chess.engine.game;

import chess.engine.board.Board;
import chess.engine.board.BoardUtils;
import chess.engine.board.Move;
import chess.engine.pieces.Piece;
import chess.engine.player.MoveTransition;

public final class MoveNotation {

    private MoveNotation() {
    }

    public static String format(final Board board, final Move move, final Board nextBoard) {
        final StringBuilder notation = new StringBuilder();
        if (move.isCastlingMove()) {
            notation.append(BoardUtils.getColumn(move.getDestinationCoordinate()) == 6 ? "O-O" : "O-O-O");
        } else {
            final Piece movedPiece = move.getMovedPiece();
            final boolean pawn = movedPiece.getPieceType() == Piece.PieceType.PAWN;
            if (!pawn) {
                notation.append(movedPiece.getPieceType());
                appendDisambiguation(board, move, notation);
            } else if (move.isAttack()) {
                notation.append((char) ('a' + BoardUtils.getColumn(move.getCurrentCoordinate())));
            }
            if (move.isAttack()) {
                notation.append('x');
            }
            notation.append(BoardUtils.getPositionAtCoordinate(move.getDestinationCoordinate()));
            if (move instanceof Move.PawnPromotion) {
                notation.append("=Q");
            }
        }

        if (nextBoard.getCurrentPlayer().isInCheckMate()) {
            notation.append('#');
        } else if (nextBoard.getCurrentPlayer().isInCheck()) {
            notation.append('+');
        }
        return notation.toString();
    }

    private static void appendDisambiguation(final Board board, final Move move, final StringBuilder notation) {
        boolean sameFile = false;
        boolean sameRank = false;
        boolean ambiguous = false;
        for (final Move candidate : board.getCurrentPlayer().getLegalMoves()) {
            if (candidate == move
                    || candidate.getDestinationCoordinate() != move.getDestinationCoordinate()
                    || candidate.getMovedPiece().getPieceType() != move.getMovedPiece().getPieceType()
                    || candidate.getMovedPiece().getPieceAlliance() != move.getMovedPiece().getPieceAlliance()) {
                continue;
            }
            final MoveTransition transition = board.getCurrentPlayer().makeMove(candidate);
            if (!transition.getMoveStatus().isDone()) {
                continue;
            }
            ambiguous = true;
            if (BoardUtils.getColumn(candidate.getCurrentCoordinate()) == BoardUtils.getColumn(move.getCurrentCoordinate())) {
                sameFile = true;
            }
            if (BoardUtils.getRow(candidate.getCurrentCoordinate()) == BoardUtils.getRow(move.getCurrentCoordinate())) {
                sameRank = true;
            }
        }
        if (ambiguous) {
            if (!sameFile) {
                notation.append((char) ('a' + BoardUtils.getColumn(move.getCurrentCoordinate())));
            } else if (!sameRank) {
                notation.append(8 - BoardUtils.getRow(move.getCurrentCoordinate()));
            } else {
                notation.append((char) ('a' + BoardUtils.getColumn(move.getCurrentCoordinate())))
                        .append(8 - BoardUtils.getRow(move.getCurrentCoordinate()));
            }
        }
    }
}
