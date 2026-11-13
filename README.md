# Chess Engine

A Java chess engine and playable chess board implementation built from scratch. This project models the chess rules, legal move generation, check/checkmate detection, castling, en passant, move transitions, and a simple minimax-based AI opponent.

## Features

- Full chess board representation with 8x8 tiles
- Legal move generation for all standard piece types
- Check, checkmate, and stalemate detection
- Castling support for both sides
- En passant capture support
- Pawn promotion to queen
- Human-vs-computer gameplay through the console
- Minimax-based engine with basic positional evaluation

## Project structure

```text
Chess_Engine/
├── src/
│   └── chess/
│       └── engine/
│           ├── Alliance.java
│           ├── Main.java
│           ├── ai/
│           │   ├── BoardEvaluator.java
│           │   └── MiniMax.java
│           ├── board/
│           │   ├── Board.java
│           │   ├── BoardUtils.java
│           │   ├── Move.java
│           │   └── Tile.java
│           ├── pieces/
│           │   ├── Bishop.java
│           │   ├── King.java
│           │   ├── Knight.java
│           │   ├── Pawn.java
│           │   ├── Piece.java
│           │   ├── Queen.java
│           │   └── Rook.java
│           └── player/
│               ├── BlackPlayer.java
│               ├── MoveStatus.java
│               ├── MoveTransition.java
│               ├── Player.java
│               └── WhitePlayer.java
├── .gitignore
└── README.md
```

## How it works

The project is organized around a few core concepts:

- Board: Represents the current state of the game and the board configuration.
- Piece: Abstract base class for all chess pieces, with move generation logic.
- Move: Encapsulates a move, destination, attack information, and special move behavior.
- Player: Tracks legal moves, king safety, and check/checkmate/stalemate state.
- MiniMax + BoardEvaluator: The engine evaluates positions and selects moves for the computer side.

## Running the game

From the repository root, compile the Java source files and run the game:

```bash
javac -d out $(find src -name "*.java")
java -cp out chess.engine.Main
```

When the game starts, the board is displayed in the console. White is controlled by the human player and Black is played by the engine.

### Input format

Moves are entered using standard algebraic-style notation without spaces, for example:

```text
e2e4
```

This means moving from e2 to e4.

## Example gameplay

```text
8  - - - - - - - -
7  - - - - - - - -
6  - - - - - - - -
5  - - - - - - - -
4  - - - - - - - -
3  - - - - - - - -
2  P P P P P P P P
1  R N B Q K B N R
   a b c d e f g h

your move (like e2e4): e2e4
```

## Notes

This engine is designed as a learning project and demonstrates how a chess engine can be structured in Java. It is not a full-strength chess engine like Stockfish, but it includes the fundamental rules and logic needed for a playable game.

## Future improvements

Possible upgrades include:

- stronger AI evaluation and search tuning
- move ordering and alpha-beta pruning improvements
- user interface enhancements
- support for full chess rules beyond the basics
- move history and game replay

## License

This project does not currently declare a license. If you are using or modifying it, check the repository settings and add a license if needed.

## Author

Built as a Java chess engine project in the `Chess_Engine` repository.
