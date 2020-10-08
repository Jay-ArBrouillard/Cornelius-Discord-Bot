package chess.player.ai.stockfish.engine;

import chess.player.ai.stockfish.engine.enums.Option;
import chess.player.ai.stockfish.engine.enums.Variant;
import chess.player.ai.stockfish.exception.StockfishEngineException;
import chess.player.ai.stockfish.exception.StockfishInitException;

import java.io.*;
import java.util.*;

abstract class UCIEngine {
    private OutputStream os;
    private PrintStream ps;

    private BufferedReader input;
    private BufferedWriter writer;
    private Process process;

    UCIEngine(String path, Variant variant, Option... options) throws StockfishInitException {
        try {
            String[] cmd = {"bin/stockfish_20090216_x64_bmi2.exe"};

            process = new ProcessBuilder().command(cmd).start();
            for (Option option : options)
                passOption(option);

            StringBuilder output = new StringBuilder();
            input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            writer.write("position fen rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1" + "\n");
            writer.write("go movetime 1000");
//            sendCommand("position fen rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
            writer.flush();

            String line;
            while ((line = input.readLine()) != null) {
                output.append(line + "\n");
            }

            int exitVal = process.waitFor();
            if (exitVal == 0) {
                System.out.println("success");
                System.out.println(output.toString());
            }
            else {
                System.out.println("error");
            }



//            input = new BufferedReader(new InputStreamReader(process.getInputStream()));
//            output = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

//            for (Option option : options)
//                passOption(option);
        } catch (IOException | InterruptedException e) {
            throw new StockfishInitException("Unable to start and bind Stockfish process: ", e);
        }
    }

    void passOption(Option option) {
        sendCommand(option.toString());
    }

    void waitForReady() {
        sendCommand("isready");
        readResponse("readyok");
    }

    void sendCommand(String command) {
//        try {
//            output.write(command + "\n");
//            output.flush();
//        } catch (IOException e) {
//            throw new StockfishEngineException(e);
//        }
    }

    String readLine(String expected) {
        try {
            String line;

            while ((line = input.readLine()) != null) {
                if (line.startsWith(expected))
                    return line;
            }

            return null;
        } catch (IOException e) {
            throw new StockfishEngineException(e);
        }
    }

    List<String> readResponse(String expected) {
        try {
            List<String> lines = new ArrayList<>();
            String line;

            while ((line = input.readLine()) != null) {
                lines.add(line);

                if (line.startsWith(expected))
                    break;
            }

            return lines;
        } catch (IOException e) {
            throw new StockfishEngineException(e);
        }
    }
}
