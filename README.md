# Chess Engine

A standalone Java desktop chess application with a custom immutable-board chess engine, a JavaFX interface, and a Minimax computer opponent.

## Features

- Human as White against the engine as Black.
- Click-to-move board with selected-piece, legal-destination, last-move, and check highlights.
- Bundled vector chess-piece assets and coordinate labels.
- Move history in algebraic notation, including basic disambiguation, castling, promotion, check, and checkmate markers.
- Engine support for captures, check/checkmate, stalemate, castling, en passant, and queen promotion.
- Minimax with alpha-beta pruning runs on a background thread; controls are disabled while the engine is thinking.
- Restartable game and portable, runtime-bundled application images.

## Technologies

- Java 24
- JavaFX 24
- Maven
- JUnit 5
- `jlink` and `jpackage`

## Screenshots

Add current application screenshots under `docs/screenshots/` and embed them here when available.

## Architecture

The existing engine remains independent of JavaFX:

- `chess.engine.board`, `chess.engine.pieces`, and `chess.engine.player` implement the immutable board, move generation, piece movement, and turn rules.
- `chess.engine.ai` contains the material/mobility evaluator and cancellable Minimax search.
- `chess.engine.game` manages a game session and algebraic move history.
- `chess.engine.gui` contains the JavaFX application and board presentation. It delegates all rule decisions to the engine and submits computer searches to a background task.

The repository retains its original `src/chess/engine` source root; `pom.xml` configures Maven to compile it and to load UI resources from `src/main/resources`.

## Run from source

Install JDK 24 and Maven 3.9 or later, then run:

```shell
mvn clean test javafx:run
```

Build the application JAR with:

```shell
mvn clean package
```

## Build a portable application

The package scripts run the tests, create a custom runtime image containing Java and JavaFX, and use `jpackage` to produce a portable application image:

**Windows PowerShell**

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\package-windows.ps1
```

**Linux or macOS**

```shell
sh scripts/package-unix.sh
```

The resulting ZIP is written to `target/`. On Windows, extract `Chess-Engine-windows-x64.zip` and launch `ChessEngine\ChessEngine.exe`. The bundled runtime means the recipient does not need Java, Maven, or IntelliJ installed.

`jpackage` creates native application images for the operating system where it runs. The GitHub Actions workflow builds Windows, Linux, and macOS images on their respective runners; separate platform builds are required, and one platform's package is not a substitute for another's. The workflow currently creates portable images rather than signed installers.

## Tests

Run the automated engine suite with:

```shell
mvn test
```

Tests cover initial setup, legal and illegal moves, captures, check, checkmate, stalemate, castling, en passant, promotion, immutable transitions, and AI move selection.

## Future improvements

- Broader chess conformance coverage, including repetition and move-count draw rules.
- Choice of underpromotion piece.
- Stronger search evaluation, transposition tables, and configurable difficulty.
- Installer signing and application icons for each platform.
- Add annotated screenshots and expanded accessibility support.
