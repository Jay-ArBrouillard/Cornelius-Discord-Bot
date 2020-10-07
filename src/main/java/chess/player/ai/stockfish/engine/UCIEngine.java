package chess.player.ai.stockfish.engine;

import chess.player.ai.stockfish.engine.enums.Option;
import chess.player.ai.stockfish.engine.enums.Variant;
import chess.player.ai.stockfish.exception.StockfishEngineException;
import chess.player.ai.stockfish.exception.StockfishInitException;

import java.io.*;
import java.util.*;

abstract class UCIEngine {
    final BufferedReader input;
    final BufferedWriter output;
    final Process process;

    UCIEngine(String path, Variant variant, Option... options) throws StockfishInitException {
        try {
            process = Runtime.getRuntime().exec(getPath(variant, path));
            input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            output = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

            for (Option option : options)
                passOption(option);
        } catch (IOException e) {
            throw new StockfishInitException("Unable to start and bind Stockfish process: ", e);
        }
    }

    void waitForReady() {
        sendCommand("isready");
        readResponse("readyok");
    }

    void sendCommand(String command) {
        try {
            output.write(command + "\n");
            output.flush();
        } catch (IOException e) {
            throw new StockfishEngineException(e);
        }
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

    private void passOption(Option option) {
        if (option.getValue() != null) {
            sendCommand(option.toString());
        }
    }

    private String getPath(Variant variant, String override) {
        StringBuilder path = new StringBuilder(override == null ? "bin/stockfish_20090216_x64_avx2.exe" : override + "stockfish_20090216_x64");
/*
        if (System.getProperty("os.name").toLowerCase().contains("win"))
            switch (variant) {
                case DEFAULT:
                    path.append(".exe");
                    break;
                case BMI2:
                    path.append("_bmi2.exe");
                    break;
                case POPCNT:
                    path.append("_popcnt.exe");
                    break;
                default:
                    throw new StockfishEngineException("Illegal variant provided.");
            }
        else
            switch (variant) {
                case DEFAULT:
                    break;
                case BMI2:
                    path.append("_bmi2.exe"); //Quick patch fix
                    break;
                case MODERN:
                    path.append("_modern");
                    break;
                default:
                    throw new StockfishEngineException("Illegal variant provided.");
    }*/

        return path.toString();
    }
}
