package chess.engine.pieces;

import chess.engine.Alliance;
import chess.engine.board.Board;
import chess.engine.board.Move;

import java.util.List;

public abstract class Piece {

      protected final int piecePosition;
      protected final Alliance pieceAlliance;

      Piece(final int piecePosition, final Alliance pieceAlliance){
          this.piecePosition = piecePosition;
          this.pieceAlliance = pieceAlliance;
      }

      public abstract List<Move> calculateLegalMove(final Board board);

}