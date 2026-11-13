package chess.engine.ai;

import chess.engine.board.Board;
import chess.engine.board.Move;
import chess.engine.player.MoveTransition;

public class MiniMax {

    private static final int INFINITY = 1_000_000;
    private static final int MATE = 100_000;

    private final BoardEvaluator evaluator;
    private final int searchDepth;

    public MiniMax(final int searchDepth) {
        this.evaluator = new BoardEvaluator();
        this.searchDepth = searchDepth;
    }

    public Move findBestMove(final Board board) {
        Move bestMove = null;
        int alpha = -INFINITY;

        for (final Move move : board.getCurrentPlayer().getLegalMoves()) {
            final MoveTransition transition = board.getCurrentPlayer().makeMove(move);
            if (!transition.getMoveStatus().isDone()) {
                continue;
            }
            final int score = -search(transition.getTransitionBoard(), this.searchDepth - 1, -INFINITY, -alpha);
            if (bestMove == null || score > alpha) {
                bestMove = move;
                alpha = score;
            }
        }
        return bestMove;
    }

    private int search(final Board board, final int depth, int alpha, final int beta) {
        if (depth == 0) {
            return this.evaluator.evaluate(board);
        }

        boolean foundMove = false;
        for (final Move move : board.getCurrentPlayer().getLegalMoves()) {
            final MoveTransition transition = board.getCurrentPlayer().makeMove(move);
            if (!transition.getMoveStatus().isDone()) {
                continue;
            }
            foundMove = true;
            final int score = -search(transition.getTransitionBoard(), depth - 1, -beta, -alpha);
            if (score >= beta) {
                return beta;
            }
            if (score > alpha) {
                alpha = score;
            }
        }

        if (!foundMove) {
            return board.getCurrentPlayer().isInCheck() ? -(MATE + depth) : 0;
        }
        return alpha;
    }
}
