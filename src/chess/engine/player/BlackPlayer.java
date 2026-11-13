package chess.engine.player;

import chess.engine.Alliance;
import chess.engine.board.Board;
import chess.engine.board.Move;
import chess.engine.board.Tile;
import chess.engine.pieces.Piece;
import chess.engine.pieces.Rook;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BlackPlayer extends Player {

    public BlackPlayer(final Board board, final Collection<Move> whiteStandardLegalMoves,
                       final Collection<Move> blackStandardLegalMoves) {
        super(board, blackStandardLegalMoves, whiteStandardLegalMoves);
    }

    @Override
    public Collection<Piece> getActivePieces() {
        return this.board.getBlackPieces();
    }

    @Override
    public Alliance getAlliance() {
        return Alliance.BLACK;
    }

    @Override
    public Player getOpponent() {
        return this.board.getWhitePlayer();
    }

    @Override
    protected Collection<Move> calculateKingCastles(final Collection<Move> opponentMoves) {
        final List<Move> kingCastles = new ArrayList<>();
        if (!this.playerKing.isFirstMove() || this.playerKing.getPiecePosition() != 4 || isInCheck()) {
            return kingCastles;
        }

        if (!this.board.getTile(5).isTileOccupied() && !this.board.getTile(6).isTileOccupied()) {
            final Tile rookTile = this.board.getTile(7);
            if (rookTile.isTileOccupied()
                    && rookTile.getPiece().isFirstMove()
                    && rookTile.getPiece().getPieceType().isRook()
                    && rookTile.getPiece().getPieceAlliance() == Alliance.BLACK
                    && !isTileAttacked(5, opponentMoves)
                    && !isTileAttacked(6, opponentMoves)) {
                kingCastles.add(new Move.KingSideCastleMove(this.board, this.playerKing, 6, (Rook) rookTile.getPiece(), 5));
            }
        }

        if (!this.board.getTile(1).isTileOccupied()
                && !this.board.getTile(2).isTileOccupied()
                && !this.board.getTile(3).isTileOccupied()) {
            final Tile rookTile = this.board.getTile(0);
            if (rookTile.isTileOccupied()
                    && rookTile.getPiece().isFirstMove()
                    && rookTile.getPiece().getPieceType().isRook()
                    && rookTile.getPiece().getPieceAlliance() == Alliance.BLACK
                    && !isTileAttacked(2, opponentMoves)
                    && !isTileAttacked(3, opponentMoves)) {
                kingCastles.add(new Move.QueenSideCastleMove(this.board, this.playerKing, 2, (Rook) rookTile.getPiece(), 3));
            }
        }

        return kingCastles;
    }
}
