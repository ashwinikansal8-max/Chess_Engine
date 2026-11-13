package chess.engine;

import chess.engine.ai.MiniMax;
import chess.engine.board.Board;
import chess.engine.board.Move;
import chess.engine.game.GameSession;
import chess.engine.pieces.Bishop;
import chess.engine.pieces.King;
import chess.engine.pieces.Pawn;
import chess.engine.pieces.Piece;
import chess.engine.pieces.Queen;
import chess.engine.pieces.Rook;
import chess.engine.player.MoveStatus;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChessEngineTest {

    @Test
    void standardBoardHasExpectedPiecesAndOpeningMoves() {
        final Board board = Board.createStandardBoard();

        assertEquals(16, board.getWhitePieces().size());
        assertEquals(16, board.getBlackPieces().size());
        assertEquals(20, board.getCurrentPlayer().getLegalMoves().size());
        assertEquals(Alliance.WHITE, board.getCurrentPlayer().getAlliance());
    }

    @Test
    void pawnMayMoveTwoSquaresOnlyFromItsStartingRank() {
        final Board board = board(Alliance.WHITE,
                new King(Alliance.WHITE, 60), new King(Alliance.BLACK, 4),
                new Pawn(Alliance.WHITE, 32, true));

        assertTrue(board.getCurrentPlayer().getLegalMoves().stream()
                .noneMatch(move -> move.getCurrentCoordinate() == 32 && move.getDestinationCoordinate() == 16));
    }

    @Test
    void normalMoveIsAppliedWithoutMutatingOriginalBoard() {
        final Board board = Board.createStandardBoard();
        final Move move = findMove(board, "e2e4");
        final GameSession game = new GameSession(board);

        assertEquals(MoveStatus.DONE, game.play(move));
        assertTrue(board.getTile(52).isTileOccupied());
        assertFalse(board.getTile(36).isTileOccupied());
        assertEquals(Piece.PieceType.PAWN, game.getBoard().getTile(36).getPiece().getPieceType());
        assertEquals(Alliance.BLACK, game.getBoard().getCurrentPlayer().getAlliance());
        assertEquals("e4", game.getMoveHistory().get(0));
    }

    @Test
    void opponentMoveCannotBePlayedOutOfTurn() {
        final Board board = Board.createStandardBoard();
        final Move blackMove = board.getBlackPlayer().getLegalMoves().iterator().next();

        assertEquals(MoveStatus.ILLEGAL_MOVE, board.getCurrentPlayer().makeMove(blackMove).getMoveStatus());
    }

    @Test
    void kingCannotBeCaptured() {
        final Board board = board(Alliance.WHITE,
                new King(Alliance.WHITE, 60), new King(Alliance.BLACK, 4),
                new Rook(Alliance.WHITE, 12));
        final Move kingCapture = board.getWhitePieces().stream()
                .filter(piece -> piece.getPieceType().isRook())
                .flatMap(piece -> piece.calculateLegalMove(board).stream())
                .filter(move -> move.toString().equals("e7e8"))
                .findFirst()
                .orElseThrow();

        assertEquals(MoveStatus.ILLEGAL_MOVE, board.getCurrentPlayer().makeMove(kingCapture).getMoveStatus());
    }

    @Test
    void moveThatExposesOwnKingIsRejected() {
        final Board board = board(Alliance.WHITE,
                new King(Alliance.WHITE, 60), new King(Alliance.BLACK, 0),
                new Rook(Alliance.WHITE, 52), new Rook(Alliance.BLACK, 4));
        final Move pinnedRookMove = board.getWhitePieces().stream()
                .filter(piece -> piece.getPieceType().isRook())
                .flatMap(piece -> piece.calculateLegalMove(board).stream())
                .filter(move -> move.toString().equals("e2f2"))
                .findFirst()
                .orElseThrow();

        assertFalse(board.getCurrentPlayer().isMoveLegal(pinnedRookMove));
        assertEquals(MoveStatus.LEAVES_PLAYER_IN_CHECK,
                board.getCurrentPlayer().makeMove(pinnedRookMove).getMoveStatus());
    }

    @Test
    void rookCaptureRemovesCapturedPiece() {
        final Board board = board(Alliance.WHITE,
                new King(Alliance.WHITE, 60), new King(Alliance.BLACK, 4),
                new Rook(Alliance.WHITE, 56), new Pawn(Alliance.BLACK, 0));
        final Board after = play(board, "a1a8");

        assertEquals(Alliance.WHITE, after.getTile(0).getPiece().getPieceAlliance());
        assertTrue(after.getBlackPieces().stream().noneMatch(piece -> piece.getPiecePosition() == 0));
    }

    @Test
    void rookGivesCheckOnOpenFile() {
        final Board board = board(Alliance.WHITE,
                new King(Alliance.WHITE, 60), new King(Alliance.BLACK, 0),
                new Rook(Alliance.BLACK, 4));

        assertTrue(board.getWhitePlayer().isInCheck());
    }

    @Test
    void checkmateAndStalemateAreDistinguished() {
        final Board checkmate = board(Alliance.BLACK,
                new King(Alliance.BLACK, 0), new King(Alliance.WHITE, 18), new Queen(Alliance.WHITE, 9));
        final Board stalemate = board(Alliance.BLACK,
                new King(Alliance.BLACK, 0), new King(Alliance.WHITE, 18), new Queen(Alliance.WHITE, 10));

        assertTrue(checkmate.getCurrentPlayer().isInCheckMate());
        assertFalse(checkmate.getCurrentPlayer().isInStaleMate());
        assertFalse(stalemate.getCurrentPlayer().isInCheck());
        assertTrue(stalemate.getCurrentPlayer().isInStaleMate());
    }

    @Test
    void whiteMayCastleWhenPathIsClearAndSafe() {
        final Board board = board(Alliance.WHITE,
                new King(Alliance.WHITE, 60), new King(Alliance.BLACK, 4),
                new Rook(Alliance.WHITE, 63));
        final Move castle = board.getCurrentPlayer().getLegalMoves().stream()
                .filter(Move::isCastlingMove)
                .findFirst()
                .orElseThrow();
        final Board after = board.getCurrentPlayer().makeMove(castle).getTransitionBoard();

        assertEquals(62, after.getWhitePlayer().getPlayerKing().getPiecePosition());
        assertEquals(Piece.PieceType.ROOK, after.getTile(61).getPiece().getPieceType());
    }

    @Test
    void whiteCannotCastleThroughAnAttackedSquare() {
        final Board board = board(Alliance.WHITE,
                new King(Alliance.WHITE, 60), new King(Alliance.BLACK, 0),
                new Rook(Alliance.WHITE, 63), new Rook(Alliance.BLACK, 5));

        assertTrue(board.getCurrentPlayer().getLegalMoves().stream().noneMatch(Move::isCastlingMove));
    }

    @Test
    void enPassantRemovesPawnFromItsOriginalSquare() {
        final Pawn jumpedPawn = new Pawn(Alliance.BLACK, 27, false);
        final Board board = new Board.Builder()
                .setPiece(new King(Alliance.WHITE, 60))
                .setPiece(new King(Alliance.BLACK, 4))
                .setPiece(new Pawn(Alliance.WHITE, 28, false))
                .setPiece(jumpedPawn)
                .setEnPassantPawn(jumpedPawn)
                .setMoveMaker(Alliance.WHITE)
                .build();
        final Board after = play(board, "e5d6");

        assertFalse(after.getTile(27).isTileOccupied());
        assertEquals(Piece.PieceType.PAWN, after.getTile(19).getPiece().getPieceType());
        assertEquals(1, after.getBlackPieces().size());
    }

    @Test
    void pawnPromotesToQueen() {
        final Board board = board(Alliance.WHITE,
                new King(Alliance.WHITE, 60), new King(Alliance.BLACK, 7),
                new Pawn(Alliance.WHITE, 8, false));
        final GameSession game = new GameSession(board);
        final Move promotion = findMove(board, "a7a8");

        assertEquals(MoveStatus.DONE, game.play(promotion));
        assertEquals(Piece.PieceType.QUEEN, game.getBoard().getTile(0).getPiece().getPieceType());
        assertEquals("a8=Q+", game.getMoveHistory().get(0));
    }

    @Test
    void minimaxSelectsALegalMoveWithoutChangingInputPosition() {
        final Board board = Board.createStandardBoard();
        final Move move = new MiniMax(1).findBestMove(board);

        assertNotNull(move);
        assertEquals(MoveStatus.DONE, board.getCurrentPlayer().makeMove(move).getMoveStatus());
        assertEquals(Alliance.WHITE, board.getCurrentPlayer().getAlliance());
        assertEquals(16, board.getWhitePieces().size());
    }

    @Test
    void minimaxReturnsNoMoveFromTerminalPosition() {
        final Board board = board(Alliance.BLACK,
                new King(Alliance.BLACK, 0), new King(Alliance.WHITE, 18), new Queen(Alliance.WHITE, 9));

        assertNull(new MiniMax(1).findBestMove(board));
    }

    @Test
    void bishopMovesAlongDiagonalAndStopsAtBlockingPiece() {
        final Board board = board(Alliance.WHITE,
                new King(Alliance.WHITE, 60), new King(Alliance.BLACK, 4),
                new Bishop(Alliance.WHITE, 42), new Pawn(Alliance.WHITE, 33));

        assertTrue(board.getCurrentPlayer().getLegalMoves().stream()
                .anyMatch(move -> move.getCurrentCoordinate() == 42 && move.getDestinationCoordinate() == 35));
        assertFalse(board.getCurrentPlayer().getLegalMoves().stream()
                .anyMatch(move -> move.getCurrentCoordinate() == 42 && move.getDestinationCoordinate() == 24));
    }

    private static Board board(final Alliance moveMaker, final Piece... pieces) {
        final Board.Builder builder = new Board.Builder();
        Arrays.stream(pieces).forEach(builder::setPiece);
        return builder.setMoveMaker(moveMaker).build();
    }

    private static Board play(final Board board, final String coordinates) {
        final Move move = findMove(board, coordinates);
        final var transition = board.getCurrentPlayer().makeMove(move);
        assertEquals(MoveStatus.DONE, transition.getMoveStatus(), "Expected legal move " + coordinates);
        return transition.getTransitionBoard();
    }

    private static Move findMove(final Board board, final String coordinates) {
        return board.getCurrentPlayer().getLegalMoves().stream()
                .filter(move -> move.toString().equals(coordinates))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Move not found: " + coordinates));
    }
}
