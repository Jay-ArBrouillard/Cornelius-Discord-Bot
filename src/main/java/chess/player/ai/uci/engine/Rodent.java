package chess.player.ai.uci.engine;

import chess.player.ai.uci.engine.enums.Option;
import chess.player.ai.uci.engine.enums.Query;
import chess.player.ai.uci.engine.enums.Variant;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
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
        Option personality = null;
        for (Option o : options) {
//            System.out.println("Option:"+o.toString());
            if (Option.Personality_File.getOptionString().contains(o.getOptionString())) {
                personality = o;
                break;
            }
        }

        if (personality == null) {
            throw new RuntimeException("PersonalityFile is not found in options list:"+Arrays.asList(options));
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
            throw new RuntimeException("Error initializing Personality and/or opening book files for " + this.getClass().getSimpleName() + " - " + personality.getValue());
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
        if (lines.length == 1) {
            if (lines[0] != null) return lines[0].split("\\s+")[1].trim();
        }
        else { //Taunt - bestmove
            StringBuilder sb = new StringBuilder();
            //First append best move
            if (lines[1] != null) { //Best move
                sb.append(lines[1].split("\\s+")[1].trim());
            }
            //Now append taunt
            if (lines[0] != null && lines[0].startsWith("info string")) {
                sb.append("##");
                lines[0] = lines[0].substring(12);
                lines[0] = lines[0].replaceAll("\uFFFD", "'");
                sb.append(lines[0]);
            }
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

    public static LinkedList<LinkedList> getOptionList() {
        LinkedList<LinkedList> options = new LinkedList<>();
        options.add(new LinkedList(Arrays.asList("PawnValueMg", 0, 300))); //Option, minimum value, maximum value
        options.add(new LinkedList(Arrays.asList("KnightValueMg", 0, 500)));
        options.add(new LinkedList(Arrays.asList("BishopValueMg", 0, 500)));
        options.add(new LinkedList(Arrays.asList("RookValueMg", 0, 700)));
        options.add(new LinkedList(Arrays.asList("QueenValueMg", 800, 1200)));
        options.add(new LinkedList(Arrays.asList("PawnValueEg", 0, 400)));
        options.add(new LinkedList(Arrays.asList("KnightValueEg", 0, 600)));
        options.add(new LinkedList(Arrays.asList("BishopValueEg", 0, 600)));
        options.add(new LinkedList(Arrays.asList("RookValueEg", 0, 800)));
        options.add(new LinkedList(Arrays.asList("QueenValueEg", 800, 1350)));
        options.add(new LinkedList(Arrays.asList("KeepPawn", -10, 20)));
        options.add(new LinkedList(Arrays.asList("KeepKnight", -10, 50)));
        options.add(new LinkedList(Arrays.asList("KeepBishop", -10, 50)));
        options.add(new LinkedList(Arrays.asList("KeepRook", -10, 50)));
        options.add(new LinkedList(Arrays.asList("KeepQueen", -10, 100)));
        options.add(new LinkedList(Arrays.asList("BishopPairMg", 0, 200)));
        options.add(new LinkedList(Arrays.asList("BishopPairEg", 0, 200)));
        options.add(new LinkedList(Arrays.asList("KnightPair", -100, 50)));
        options.add(new LinkedList(Arrays.asList("RookPair", -100, 100)));
        options.add(new LinkedList(Arrays.asList("KnightLikesClosed", -50, 50)));
        options.add(new LinkedList(Arrays.asList("RookLikesOpen", -50, 50)));
        options.add(new LinkedList(Arrays.asList("ExchangeImbalance", -100, 100)));
        options.add(new LinkedList(Arrays.asList("MinorVsQueen", -100, 100)));
        options.add(new LinkedList(Arrays.asList("Material", 0, 100))); //Percentage
        options.add(new LinkedList(Arrays.asList("OwnAttack", 0, 500)));
        options.add(new LinkedList(Arrays.asList("OppAttack", 0, 500)));
        options.add(new LinkedList(Arrays.asList("OwnMobility", 0, 500)));
        options.add(new LinkedList(Arrays.asList("OppMobility", 0, 500)));
        options.add(new LinkedList(Arrays.asList("FlatMobility", 0, 500)));
        options.add(new LinkedList(Arrays.asList("KingTropism", 0, 400)));
        options.add(new LinkedList(Arrays.asList("PrimaryPstWeight", 0, 100)));
        options.add(new LinkedList(Arrays.asList("SecondaryPstWeight", 0, 100)));
        options.add(new LinkedList(Arrays.asList("PiecePressure", -300, 300)));
        options.add(new LinkedList(Arrays.asList("PassedPawns", 0, 300)));
        options.add(new LinkedList(Arrays.asList("PawnStructure", 0, 500)));
        options.add(new LinkedList(Arrays.asList("Lines", 0, 300)));
        options.add(new LinkedList(Arrays.asList("Outposts", 0, 500)));
        options.add(new LinkedList(Arrays.asList("Space", -500, 500)));
        options.add(new LinkedList(Arrays.asList("PawnShield", -500, 500)));
        options.add(new LinkedList(Arrays.asList("PawnStorm", -500, 500)));
        options.add(new LinkedList(Arrays.asList("DoubledPawnMg", -100, 0)));
        options.add(new LinkedList(Arrays.asList("DoubledPawnEg", -100, 0)));
        options.add(new LinkedList(Arrays.asList("IsolatedPawnMg", -100, 0)));
        options.add(new LinkedList(Arrays.asList("IsolatedPawnEg", -100, 0)));
        options.add(new LinkedList(Arrays.asList("IsolatedOnOpenMg", -100, 0)));
        options.add(new LinkedList(Arrays.asList("BackwardPawnMg", -100, 0)));
        options.add(new LinkedList(Arrays.asList("BackwardPawnEg", -100, 0)));
        options.add(new LinkedList(Arrays.asList("BackwardOnOpenMg", -100, 0)));
        options.add(new LinkedList(Arrays.asList("FianchBase", -100, 100)));
        options.add(new LinkedList(Arrays.asList("FianchKing", -100, 100)));
        options.add(new LinkedList(Arrays.asList("ReturningB", -100, 100)));
        options.add(new LinkedList(Arrays.asList("PawnMass", -200, 200)));
        options.add(new LinkedList(Arrays.asList("PawnChains", -200, 200)));
        options.add(new LinkedList(Arrays.asList("PrimaryPstStyle", 0, 4)));
        options.add(new LinkedList(Arrays.asList("SecondaryPstStyle", 0, 4)));
        options.add(new LinkedList(Arrays.asList("blockedcpawn", -100, 100)));
        options.add(new LinkedList(Arrays.asList("NpsLimit", 0, 1000000)));
        options.add(new LinkedList(Arrays.asList("EvalBlur", 0, 1000)));
        options.add(new LinkedList(Arrays.asList("Contempt", -100, 100)));
        options.add(new LinkedList(Arrays.asList("SlowMover", 0, 200)));
        options.add(new LinkedList(Arrays.asList("Selectivity", 0, 200)));
//        options.add(new LinkedList(Arrays.asList("SearchSkill", 0, 10)));
//        options.add(new LinkedList(Arrays.asList("RiskyDepth", 0, 200)));
        options.add(new LinkedList(Arrays.asList("BookFilter", 0, 100)));
        options.add(new LinkedList(Arrays.asList("GuideBookFile", "/app/bin/books/guide/active.bin", "/app/bin/books/guide/closed.bin", "/app/bin/books/guide/cs.bin",
                "/app/bin/books/guide/exchange.bin", "/app/bin/books/guide/flank.bin", "/app/bin/books/guide/grandpa.bin", "/app/bin/books/guide/guide.bin",
                "/app/bin/books/guide/low.bin", "/app/bin/books/guide/simple.bin", "/app/bin/books/guide/solid.bin", "/app/bin/books/guide/tricky.bin")));
        options.add(new LinkedList(Arrays.asList("MainBookFile", "/app/bin/books/elo-2650.bin", "/app/bin/books/guide.bin", "/app/bin/books/micro.bin",
                "/app/bin/books/mini.bin", "/app/bin/books/ph-exoticbook.bin", "/app/bin/books/ph-gambitbook.bin", "/app/bin/books/pwned-book.bin",
                "/app/bin/books/rodent.bin", "/app/bin/books/small.bin")));

        return options;
    }
}
