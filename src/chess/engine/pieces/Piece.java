package chess.engine.pieces;

import chess.engine.Alliance;
import chess.engine.board.Board;
import chess.engine.board.BoardUtils;
import chess.engine.board.Move;
import chess.engine.board.Tile;

import java.util.ArrayList;
import java.util.List;

public abstract class Piece {

    protected final PieceType pieceType;
    protected final int piecePosition;
    protected final Alliance pieceAlliance;
    protected final boolean isFirstMove;
    private final int cachedHashCode;

    Piece(final PieceType pieceType, final int piecePosition, final Alliance pieceAlliance, final boolean isFirstMove) {
        this.pieceType = pieceType;
        this.piecePosition = piecePosition;
        this.pieceAlliance = pieceAlliance;
        this.isFirstMove = isFirstMove;
        this.cachedHashCode = computeHashCode();
    }

    public abstract List<Move> calculateLegalMove(final Board board);

    public abstract Piece movePiece(final Move move);

    public int getPiecePosition() {
        return this.piecePosition;
    }

    public Alliance getPieceAlliance() {
        return this.pieceAlliance;
    }

    public PieceType getPieceType() {
        return this.pieceType;
    }

    public boolean isFirstMove() {
        return this.isFirstMove;
    }

    protected List<Move> calculateStepMoves(final Board board, final int[] offsets, final int maxColumnShift) {
        final List<Move> legalMoves = new ArrayList<>();
        for (final int offset : offsets) {
            final int destination = this.piecePosition + offset;
            if (!BoardUtils.isValidTileCoordinate(destination)) {
                continue;
            }
            final int columnShift = Math.abs(BoardUtils.getColumn(destination) - BoardUtils.getColumn(this.piecePosition));
            if (columnShift > maxColumnShift) {
                continue;
            }
            addMoveIfPossible(board, destination, legalMoves);
        }
        return legalMoves;
    }

    protected List<Move> calculateSlidingMoves(final Board board, final int[] directions) {
        final List<Move> legalMoves = new ArrayList<>();
        for (final int direction : directions) {
            int current = this.piecePosition;
            while (true) {
                final int next = current + direction;
                if (!BoardUtils.isValidTileCoordinate(next)) {
                    break;
                }
                if (Math.abs(BoardUtils.getColumn(next) - BoardUtils.getColumn(current)) > 1) {
                    break;
                }
                final boolean blocked = addMoveIfPossible(board, next, legalMoves);
                if (blocked) {
                    break;
                }
                current = next;
            }
        }
        return legalMoves;
    }

    private boolean addMoveIfPossible(final Board board, final int destination, final List<Move> moves) {
        final Tile destinationTile = board.getTile(destination);
        if (!destinationTile.isTileOccupied()) {
            moves.add(new Move.MajorMove(board, this, destination));
            return false;
        }
        final Piece pieceAtDestination = destinationTile.getPiece();
        if (this.pieceAlliance != pieceAtDestination.getPieceAlliance()) {
            moves.add(new Move.MajorAttackMove(board, this, destination, pieceAtDestination));
        }
        return true;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Piece)) {
            return false;
        }
        final Piece otherPiece = (Piece) other;
        return this.piecePosition == otherPiece.piecePosition
                && this.pieceType == otherPiece.pieceType
                && this.pieceAlliance == otherPiece.pieceAlliance
                && this.isFirstMove == otherPiece.isFirstMove;
    }

    @Override
    public int hashCode() {
        return this.cachedHashCode;
    }

    @Override
    public String toString() {
        return this.pieceAlliance.isBlack() ? this.pieceType.toString().toLowerCase() : this.pieceType.toString();
    }

    private int computeHashCode() {
        int result = this.pieceType.hashCode();
        result = 31 * result + this.pieceAlliance.hashCode();
        result = 31 * result + this.piecePosition;
        result = 31 * result + (this.isFirstMove ? 1 : 0);
        return result;
    }

    public enum PieceType {

        PAWN("P", 100),
        KNIGHT("N", 300),
        BISHOP("B", 300),
        ROOK("R", 500),
        QUEEN("Q", 900),
        KING("K", 10000);

        private final String pieceName;
        private final int pieceValue;

        PieceType(final String pieceName, final int pieceValue) {
            this.pieceName = pieceName;
            this.pieceValue = pieceValue;
        }

        public int getPieceValue() {
            return this.pieceValue;
        }

        public boolean isKing() {
            return this == KING;
        }

        public boolean isRook() {
            return this == ROOK;
        }

        @Override
        public String toString() {
            return this.pieceName;
        }
    }
}