package chess.engine.board;

import chess.engine.pieces.Pawn;
import chess.engine.pieces.Piece;
import chess.engine.pieces.Queen;
import chess.engine.pieces.Rook;

import java.util.Objects;

public abstract class Move {

    protected final Board board;
    protected final Piece movedPiece;
    protected final int destinationCoordinate;

    private Move(final Board board, final Piece movedPiece, final int destinationCoordinate) {
        this.board = board;
        this.movedPiece = movedPiece;
        this.destinationCoordinate = destinationCoordinate;
    }

    public int getCurrentCoordinate() {
        return this.movedPiece.getPiecePosition();
    }

    public int getDestinationCoordinate() {
        return this.destinationCoordinate;
    }

    public Piece getMovedPiece() {
        return this.movedPiece;
    }

    public boolean isAttack() {
        return false;
    }

    public boolean isCastlingMove() {
        return false;
    }

    public Piece getAttackedPiece() {
        return null;
    }

    protected Board.Builder createBuilder() {
        final Board.Builder builder = new Board.Builder();
        for (final Piece piece : this.board.getAllPieces()) {
            if (!this.movedPiece.equals(piece)) {
                builder.setPiece(piece);
            }
        }
        builder.setMoveMaker(this.movedPiece.getPieceAlliance().getOpposite());
        return builder;
    }

    public Board execute() {
        final Board.Builder builder = createBuilder();
        builder.setPiece(this.movedPiece.movePiece(this));
        return builder.build();
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Move)) {
            return false;
        }
        final Move otherMove = (Move) other;
        return getClass() == otherMove.getClass()
                && this.destinationCoordinate == otherMove.destinationCoordinate
                && this.movedPiece.equals(otherMove.movedPiece);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.destinationCoordinate, this.movedPiece);
    }

    @Override
    public String toString() {
        return BoardUtils.getPositionAtCoordinate(getCurrentCoordinate())
                + BoardUtils.getPositionAtCoordinate(this.destinationCoordinate);
    }

    public static final class MajorMove extends Move {

        public MajorMove(final Board board, final Piece movedPiece, final int destinationCoordinate) {
            super(board, movedPiece, destinationCoordinate);
        }
    }

    public static class AttackMove extends Move {

        private final Piece attackedPiece;

        public AttackMove(final Board board, final Piece movedPiece, final int destinationCoordinate, final Piece attackedPiece) {
            super(board, movedPiece, destinationCoordinate);
            this.attackedPiece = attackedPiece;
        }

        @Override
        public boolean isAttack() {
            return true;
        }

        @Override
        public Piece getAttackedPiece() {
            return this.attackedPiece;
        }
    }

    public static final class MajorAttackMove extends AttackMove {

        public MajorAttackMove(final Board board, final Piece movedPiece, final int destinationCoordinate, final Piece attackedPiece) {
            super(board, movedPiece, destinationCoordinate, attackedPiece);
        }
    }

    public static final class PawnMove extends Move {

        public PawnMove(final Board board, final Piece movedPiece, final int destinationCoordinate) {
            super(board, movedPiece, destinationCoordinate);
        }
    }

    public static class PawnAttackMove extends AttackMove {

        public PawnAttackMove(final Board board, final Piece movedPiece, final int destinationCoordinate, final Piece attackedPiece) {
            super(board, movedPiece, destinationCoordinate, attackedPiece);
        }
    }

    public static final class PawnEnPassantAttack extends PawnAttackMove {

        public PawnEnPassantAttack(final Board board, final Piece movedPiece, final int destinationCoordinate, final Piece attackedPiece) {
            super(board, movedPiece, destinationCoordinate, attackedPiece);
        }

        @Override
        public Board execute() {
            final Board.Builder builder = createBuilder();
            builder.removePiece(getAttackedPiece());
            builder.setPiece(this.movedPiece.movePiece(this));
            return builder.build();
        }
    }

    public static final class PawnJump extends Move {

        public PawnJump(final Board board, final Piece movedPiece, final int destinationCoordinate) {
            super(board, movedPiece, destinationCoordinate);
        }

        @Override
        public Board execute() {
            final Board.Builder builder = createBuilder();
            final Pawn movedPawn = (Pawn) this.movedPiece.movePiece(this);
            builder.setPiece(movedPawn);
            builder.setEnPassantPawn(movedPawn);
            return builder.build();
        }
    }

    public static final class PawnPromotion extends Move {

        private final Move decoratedMove;

        public PawnPromotion(final Move decoratedMove) {
            super(decoratedMove.board, decoratedMove.movedPiece, decoratedMove.destinationCoordinate);
            this.decoratedMove = decoratedMove;
        }

        @Override
        public Board execute() {
            final Board.Builder builder = createBuilder();
            builder.setPiece(new Queen(this.movedPiece.getPieceAlliance(), this.destinationCoordinate, false));
            return builder.build();
        }

        @Override
        public boolean isAttack() {
            return this.decoratedMove.isAttack();
        }

        @Override
        public Piece getAttackedPiece() {
            return this.decoratedMove.getAttackedPiece();
        }
    }

    public abstract static class CastleMove extends Move {

        protected final Rook castleRook;
        protected final int castleRookDestination;

        CastleMove(final Board board, final Piece movedPiece, final int destinationCoordinate,
                   final Rook castleRook, final int castleRookDestination) {
            super(board, movedPiece, destinationCoordinate);
            this.castleRook = castleRook;
            this.castleRookDestination = castleRookDestination;
        }

        @Override
        public boolean isCastlingMove() {
            return true;
        }

        @Override
        public Board execute() {
            final Board.Builder builder = createBuilder();
            builder.removePiece(this.castleRook);
            builder.setPiece(this.movedPiece.movePiece(this));
            builder.setPiece(new Rook(this.castleRook.getPieceAlliance(), this.castleRookDestination, false));
            return builder.build();
        }
    }

    public static final class KingSideCastleMove extends CastleMove {

        public KingSideCastleMove(final Board board, final Piece movedPiece, final int destinationCoordinate,
                                  final Rook castleRook, final int castleRookDestination) {
            super(board, movedPiece, destinationCoordinate, castleRook, castleRookDestination);
        }
    }

    public static final class QueenSideCastleMove extends CastleMove {

        public QueenSideCastleMove(final Board board, final Piece movedPiece, final int destinationCoordinate,
                                   final Rook castleRook, final int castleRookDestination) {
            super(board, movedPiece, destinationCoordinate, castleRook, castleRookDestination);
        }
    }
}