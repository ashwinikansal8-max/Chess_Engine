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

public class WhitePlayer extends Player {

    public WhitePlayer(final Board board, final Collection<Move> whiteStandardLegalMoves,
                       final Collection<Move> blackStandardLegalMoves) {
        super(board, whiteStandardLegalMoves, blackStandardLegalMoves);
    }

    @Override
    public Collection<Piece> getActivePieces() {
        return this.board.getWhitePieces();
    }

    @Override
    public Alliance getAlliance() {
        return Alliance.WHITE;
    }

    @Override
    public Player getOpponent() {
        return this.board.getBlackPlayer();
    }

    @Override
    protected Collection<Move> calculateKingCastles(final Collection<Move> opponentMoves) {
        final List<Move> kingCastles = new ArrayList<>();
        if (!this.playerKing.isFirstMove() || this.playerKing.getPiecePosition() != 60 || isInCheck()) {
            return kingCastles;
        }

        if (!this.board.getTile(61).isTileOccupied() && !this.board.getTile(62).isTileOccupied()) {
            final Tile rookTile = this.board.getTile(63);
            if (rookTile.isTileOccupied()
                    && rookTile.getPiece().isFirstMove()
                    && rookTile.getPiece().getPieceType().isRook()
                    && rookTile.getPiece().getPieceAlliance() == Alliance.WHITE
                    && !isTileAttacked(61, opponentMoves)
                    && !isTileAttacked(62, opponentMoves)) {
                kingCastles.add(new Move.KingSideCastleMove(this.board, this.playerKing, 62, (Rook) rookTile.getPiece(), 61));
            }
        }

        if (!this.board.getTile(57).isTileOccupied()
                && !this.board.getTile(58).isTileOccupied()
                && !this.board.getTile(59).isTileOccupied()) {
            final Tile rookTile = this.board.getTile(56);
            if (rookTile.isTileOccupied()
                    && rookTile.getPiece().isFirstMove()
                    && rookTile.getPiece().getPieceType().isRook()
                    && rookTile.getPiece().getPieceAlliance() == Alliance.WHITE
                    && !isTileAttacked(58, opponentMoves)
                    && !isTileAttacked(59, opponentMoves)) {
                kingCastles.add(new Move.QueenSideCastleMove(this.board, this.playerKing, 58, (Rook) rookTile.getPiece(), 59));
            }
        }

        return kingCastles;
    }
}
