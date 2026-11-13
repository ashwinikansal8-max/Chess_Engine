package chess.engine.player;

import chess.engine.Alliance;
import chess.engine.board.Board;
import chess.engine.board.BoardUtils;
import chess.engine.board.Move;
import chess.engine.pieces.King;
import chess.engine.pieces.Piece;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class Player {

    protected final Board board;
    protected final King playerKing;
    private final Collection<Move> pseudoLegalMoves;
    private volatile Collection<Move> legalMoves;
    private final boolean isInCheck;

    Player(final Board board, final Collection<Move> playerMoves, final Collection<Move> opponentMoves) {
        this.board = board;
        this.playerKing = establishKing();
        this.isInCheck = !calculateAttacksOnTile(this.playerKing.getPiecePosition(), opponentMoves).isEmpty();
        final List<Move> allMoves = new ArrayList<>(playerMoves);
        allMoves.addAll(calculateKingCastles(opponentMoves));
        this.pseudoLegalMoves = Collections.unmodifiableList(allMoves);
    }

    public abstract Collection<Piece> getActivePieces();

    public abstract Alliance getAlliance();

    public abstract Player getOpponent();

    protected abstract Collection<Move> calculateKingCastles(Collection<Move> opponentMoves);

    public King getPlayerKing() {
        return this.playerKing;
    }

    public Collection<Move> getLegalMoves() {
        Collection<Move> moves = this.legalMoves;
        if (moves == null) {
            synchronized (this) {
                moves = this.legalMoves;
                if (moves == null) {
                    final List<Move> legal = new ArrayList<>();
                    for (final Move move : this.pseudoLegalMoves) {
                        if (makeMove(move).getMoveStatus().isDone()) {
                            legal.add(move);
                        }
                    }
                    moves = Collections.unmodifiableList(legal);
                    this.legalMoves = moves;
                }
            }
        }
        return moves;
    }

    public boolean isMoveLegal(final Move move) {
        return getLegalMoves().contains(move);
    }

    public boolean isInCheck() {
        return this.isInCheck;
    }

    public boolean isInCheckMate() {
        return this.isInCheck && !hasEscapeMoves();
    }

    public boolean isInStaleMate() {
        return !this.isInCheck && !hasEscapeMoves();
    }

    public MoveTransition makeMove(final Move move) {
        if (!this.pseudoLegalMoves.contains(move)
                || move.getAttackedPiece() != null && move.getAttackedPiece().getPieceType().isKing()) {
            return new MoveTransition(this.board, move, MoveStatus.ILLEGAL_MOVE);
        }

        final Board transitionBoard = move.execute();
        final Player opponentToMove = transitionBoard.getCurrentPlayer();
        final Player moveMaker = opponentToMove.getOpponent();
        final Collection<Move> attacksOnKing = calculateAttacksOnTile(
                moveMaker.getPlayerKing().getPiecePosition(), opponentToMove.pseudoLegalMoves);

        if (!attacksOnKing.isEmpty()) {
            return new MoveTransition(this.board, move, MoveStatus.LEAVES_PLAYER_IN_CHECK);
        }
        return new MoveTransition(transitionBoard, move, MoveStatus.DONE);
    }

    protected boolean hasEscapeMoves() {
        for (final Move move : getLegalMoves()) {
            if (makeMove(move).getMoveStatus().isDone()) {
                return true;
            }
        }
        return false;
    }

    protected boolean isTileAttacked(final int tile, final Collection<Move> opponentMoves) {
        if (!calculateAttacksOnTile(tile, opponentMoves).isEmpty()) {
            return true;
        }
        for (final Piece piece : this.board.getPieces(getAlliance().getOpposite())) {
            if (piece.getPieceType() != Piece.PieceType.PAWN) {
                continue;
            }
            final int ahead = piece.getPiecePosition()
                    + piece.getPieceAlliance().getDirection() * BoardUtils.NUM_TILES_PER_ROW;
            final boolean diagonal = tile == ahead - 1 || tile == ahead + 1;
            final int columnGap = Math.abs(BoardUtils.getColumn(tile) - BoardUtils.getColumn(piece.getPiecePosition()));
            if (diagonal && columnGap == 1) {
                return true;
            }
        }
        return false;
    }

    protected static Collection<Move> calculateAttacksOnTile(final int tile, final Collection<Move> moves) {
        final List<Move> attackMoves = new ArrayList<>();
        for (final Move move : moves) {
            if (tile == move.getDestinationCoordinate()
                    && (move.getMovedPiece().getPieceType() != Piece.PieceType.PAWN || move.isAttack())) {
                attackMoves.add(move);
            }
        }
        return Collections.unmodifiableList(attackMoves);
    }

    private King establishKing() {
        for (final Piece piece : getActivePieces()) {
            if (piece.getPieceType().isKing()) {
                return (King) piece;
            }
        }
        throw new RuntimeException("This board has no king for " + getAlliance());
    }
}
