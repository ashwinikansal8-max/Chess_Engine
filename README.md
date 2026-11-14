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
mvn clean javafx:run
```

Build the application JAR with:

```shell
mvn clean package
```

## Build and share the Windows installer

On Windows, install JDK 24 and Maven 3.9 or later, open PowerShell in the project directory, and run:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\package-windows.ps1
```

The script runs the JUnit tests, bundles Java and JavaFX into a custom runtime, and uses `jpackage` to create the Windows installer:

```text
target\installer\Chess Engine-1.0.0.msi
```

The installer includes the runtime and offers Start Menu and desktop shortcuts. It installs per-user, allows choosing the installation directory, and uses the bundled application icon. Give the recipient the single `.msi` installer; they do not need Java, Maven, or IntelliJ.

WiX Toolset v3.14.1 is needed by `jpackage` to build Windows installers. The script downloads the official pinned WiX binary archive on first use and verifies its SHA-256 before extracting it into the local build-tools cache.

## Linux and macOS packages

Run `sh scripts/package-unix.sh` on Linux or macOS to create a runtime-bundled application image ZIP. `jpackage` produces native packages only for the operating system on which it runs; separate Windows, Linux, and macOS builds are required. The GitHub Actions workflow builds each platform separately.

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
- Installer signing and platform-specific application icons.
- Add annotated screenshots and expanded accessibility support.
