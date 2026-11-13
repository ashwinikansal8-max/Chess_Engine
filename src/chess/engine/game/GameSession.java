package chess.engine.game;

import chess.engine.board.Board;
import chess.engine.board.Move;
import chess.engine.player.MoveStatus;
import chess.engine.player.MoveTransition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class GameSession {

    private Board board;
    private final List<String> moveHistory = new ArrayList<>();

    public GameSession() {
        this(Board.createStandardBoard());
    }

    public GameSession(final Board initialBoard) {
        this.board = Objects.requireNonNull(initialBoard, "initialBoard");
    }

    public Board getBoard() {
        return this.board;
    }

    public List<String> getMoveHistory() {
        return Collections.unmodifiableList(this.moveHistory);
    }

    public MoveStatus play(final Move move) {
        Objects.requireNonNull(move, "move");
        final MoveTransition transition = this.board.getCurrentPlayer().makeMove(move);
        if (transition.getMoveStatus().isDone()) {
            final String notation = MoveNotation.format(this.board, move, transition.getTransitionBoard());
            this.moveHistory.add(notation);
            this.board = transition.getTransitionBoard();
        }
        return transition.getMoveStatus();
    }
}
