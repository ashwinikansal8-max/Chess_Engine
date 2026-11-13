package chess.engine.pieces;

import chess.engine.Alliance;
import chess.engine.board.Board;
import chess.engine.board.BoardUtils;
import chess.engine.board.Move;
import chess.engine.board.Tile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Pawn extends Piece {

    public Pawn(final Alliance pieceAlliance, final int piecePosition) {
        this(pieceAlliance, piecePosition, true);
    }

    public Pawn(final Alliance pieceAlliance, final int piecePosition, final boolean isFirstMove) {
        super(PieceType.PAWN, piecePosition, pieceAlliance, isFirstMove);
    }

    @Override
    public List<Move> calculateLegalMove(final Board board) {
        final List<Move> legalMoves = new ArrayList<>();
        final int rowStep = this.pieceAlliance.getDirection() * BoardUtils.NUM_TILES_PER_ROW;
        final int oneStep = this.piecePosition + rowStep;

        if (!BoardUtils.isValidTileCoordinate(oneStep)) {
            return Collections.unmodifiableList(legalMoves);
        }

        if (!board.getTile(oneStep).isTileOccupied()) {
            legalMoves.add(createPushMove(board, oneStep));
            final int twoSteps = oneStep + rowStep;
            final int startingRow = this.pieceAlliance.isWhite() ? 6 : 1;
            if (this.isFirstMove
                    && BoardUtils.getRow(this.piecePosition) == startingRow
                    && BoardUtils.isValidTileCoordinate(twoSteps)
                    && !board.getTile(twoSteps).isTileOccupied()) {
                legalMoves.add(new Move.PawnJump(board, this, twoSteps));
            }
        }

        for (final int sideStep : new int[]{-1, 1}) {
            final int attackSquare = oneStep + sideStep;
            if (!BoardUtils.isValidTileCoordinate(attackSquare)) {
                continue;
            }
            if (Math.abs(BoardUtils.getColumn(attackSquare) - BoardUtils.getColumn(this.piecePosition)) != 1) {
                continue;
            }

            final Tile attackTile = board.getTile(attackSquare);
            if (attackTile.isTileOccupied()) {
                final Piece target = attackTile.getPiece();
                if (target.getPieceAlliance() != this.pieceAlliance) {
                    legalMoves.add(createAttackMove(board, attackSquare, target));
                }
            } else {
                final Pawn enPassantPawn = board.getEnPassantPawn();
                if (enPassantPawn != null
                        && enPassantPawn.getPiecePosition() == this.piecePosition + sideStep
                        && enPassantPawn.getPieceAlliance() != this.pieceAlliance) {
                    legalMoves.add(new Move.PawnEnPassantAttack(board, this, attackSquare, enPassantPawn));
                }
            }
        }

        return Collections.unmodifiableList(legalMoves);
    }

    @Override
    public Pawn movePiece(final Move move) {
        return new Pawn(move.getMovedPiece().getPieceAlliance(), move.getDestinationCoordinate(), false);
    }

    private Move createPushMove(final Board board, final int destination) {
        final Move push = new Move.PawnMove(board, this, destination);
        return isPromotionRow(destination) ? new Move.PawnPromotion(push) : push;
    }

    private Move createAttackMove(final Board board, final int destination, final Piece target) {
        final Move attack = new Move.PawnAttackMove(board, this, destination, target);
        return isPromotionRow(destination) ? new Move.PawnPromotion(attack) : attack;
    }

    private boolean isPromotionRow(final int destination) {
        final int row = BoardUtils.getRow(destination);
        return row == 0 || row == 7;
    }
}
