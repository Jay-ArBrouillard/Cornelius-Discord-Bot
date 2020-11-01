package chess.player.ai.uci.engine;

import chess.player.ai.uci.engine.enums.Option;
import chess.player.ai.uci.engine.enums.Query;
import chess.player.ai.uci.engine.enums.Variant;

import java.io.IOException;

/**
 * When running Sugar it will create experience.bin and pawngame.bin for persistent learning
 */
public class Sugar extends UCIEngine {
    private final String BOOK_FILE_1 = "/app/bin/books/elo-2650.bin";
    private final String BOOK_FILE_2 = "/app/bin/books/pwned-book.bin";

    public Sugar(Variant variant, String filePath, Option... options) throws IOException {
        super(variant, filePath, options);

        waitForReady();
        sendCommand("setoption name Less Pruning Mode value 3");
        sendCommand("setoption name Use MCTS Score value true");
        sendCommand("setoption name BookFile value " + BOOK_FILE_1);
        String line1 = readLine("info string");
        if (line1.contains("Could not open book"))
            throw new RuntimeException("Error opening book " + BOOK_FILE_1 + " for " + this.getClass().getSimpleName());
        waitForReady();
        sendCommand("setoption name BookFile2 value " + BOOK_FILE_2);
        String line2 = readLine("info string");
        if (line2.contains("Could not open book"))
            throw new RuntimeException("Error opening book " + BOOK_FILE_2 + " for " + this.getClass().getSimpleName());
    }

    public String getBestMove(Query query) throws IOException {
        waitForReady();
        sendCommand("position fen " + query.getFen());

        StringBuilder command = new StringBuilder("go ");

        if (query.getDepth() >= 0)
            command.append("depth ").append(query.getDepth()).append(" ");

        if (query.getMovetime() >= 0)
            command.append("movetime ").append(query.getMovetime());

        waitForReady();
        sendCommand(command.toString());

        String result = readLine("bestmove");
        if (result != null) return result.split("\\s+")[1].trim();
        return null;
    }

    public void close() throws IOException {
        super.close();
    }

    private String getFen() throws IOException {
        waitForReady();
        sendCommand("d");

        return readLine("Fen: ").substring(5);
    }
}
