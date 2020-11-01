package chess.player.ai.uci.engine;

import chess.player.ai.uci.engine.enums.Option;
import chess.player.ai.uci.engine.enums.Query;
import chess.player.ai.uci.engine.enums.Variant;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Rodent extends UCIEngine {
    public Rodent(Variant variant, String filePath, Option... options) throws IOException {
        super(variant, filePath, options);
        //Ensure personality and opening books are read successfully
        /*
            setoption name Verbose value true
            setoption name Personality value Cloe
            info string reading personality 'cloe.txt' (success)
            info string reading book file '/app/bin/books/guide/cat.bin' (success)
            info string reading book file '/app/bin/books/rodent.bin' (success)
         */
        Option personality = Arrays.stream(options).findFirst().filter(o -> Option.Personality_File.getOptionString().equals(o.getOptionString())).orElse(null);
        if (personality == null) {
            throw new RuntimeException("Error initializing " + this.getClass().getSimpleName() + " Personality File is not set");
        }

        waitForReady();
        sendCommand("setoption name Verbose value true");
        waitForReady();
        sendCommand("setoption name PersonalityFile value " + personality.getValue());
        List<String> responses = readResponse("info string reading", 3);
        int numSuccesses = 0;
        for (String s : responses) {
            if (s.endsWith("success")) {
                System.out.println("SUCCESS LOADING:" + s);
                numSuccesses++;
            }
            if (s.endsWith("failure")) {
                System.out.println("FAILURE LOADING:" + s);
                break;
            }
        }

        if (numSuccesses != 3) {
            throw new RuntimeException("Error initializing Personality and/or opening book files for " + this.getClass().getSimpleName());
        }
        sendCommand("setoption name Verbose value false");
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
