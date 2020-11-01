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
            if (s.contains("success")) {
                numSuccesses++;
            }
            if (s.contains("failure")) {
                break;
            }
        }

        if (numSuccesses != 3) {
            throw new RuntimeException("Error initializing Personality and/or opening book files for " + this.getClass().getSimpleName());
        }
        sendCommand("setoption name Verbose value false");
        sendCommand("setoption name Taunting value true"); //NOTE turned Taunting on here
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

        //Assuming Taunting is on
        //BestMoveString - ex: "e5e7##tauntString"
        String[] lines = readLines("bestmove");
        System.out.println(lines.length);
        System.out.println(lines[0] + ", " + lines[1]);
        if (lines.length == 1) {
            if (lines[0] != null) return lines[0].split("\\s+")[1].trim();
        }
        else { //Taunt - bestmove
            StringBuilder sb = null;
            if (lines[1] != null) { //Best move
                sb.append(lines[1].split("\\s+")[1].trim());
            }

            System.out.println(sb.toString());

            if (lines[0] != null && lines[0].startsWith("info string")) {
                sb.append("##");
                sb.append(lines[0].substring(lines[0].indexOf("info string")));
            }

            System.out.println(sb.toString());

            return sb.toString();
        }
        return null;
    }

    String[] readLines(String expected) throws IOException {
        String[] lines = new String[2]; //index 0 is previous line, index1 is current line
        String line;
        while ((line = input.readLine()) != null) {
            lines[1] = line;
            if (line.startsWith(expected)) {
                return lines;
            }
            lines[0] = lines[1];
        }

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
