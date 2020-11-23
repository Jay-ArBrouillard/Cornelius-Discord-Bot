package utils;

import commands.*;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

public class CommandHelper extends ListenerAdapter {

    private String botId;

    public CommandHelper(String pBotId) {
        botId = pBotId;
    }

    @Override
    public void onMessageReceived (@Nonnull MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw();
        MessageChannel mc = event.getChannel();
        if (event.getMessage().getAuthor().isBot()) return;
        if (message.startsWith("!help")) {
            StringBuilder help = new StringBuilder("**Cornelius** commands:\n");
            help.append("`").append(ChooseCommand.Help.getName()).append(" ")
                            .append(ChooseCommand.Help.getArguments()).append("`")
                            .append(" - ").append(ChooseCommand.Help.getDescription()).append("\n");
            help.append("`").append(CopyPastaCommand.Help.getName()).append(" ")
                            .append(CopyPastaCommand.Help.getArguments()).append("`")
                            .append(" - ").append(CopyPastaCommand.Help.getDescription()).append("\n");
            help.append("`").append(MemeCommand.Help.getName()).append(" ")
                            .append(MemeCommand.Help.getArguments()).append("`")
                            .append(" - ").append(MemeCommand.Help.getDescription()).append("\n");
            help.append("`").append(NumberGeneratorCommand.Help.getName()).append(" ")
                            .append(NumberGeneratorCommand.Help.getArguments()).append("`")
                            .append(" - ").append(NumberGeneratorCommand.Help.getDescription()).append("\n");
            help.append("`").append(PollCommand.Help.getName()).append(" ")
                            .append(PollCommand.Help.getArguments()).append("`")
                            .append(" - ").append(PollCommand.Help.getDescription()).append("\n");
            help.append("`").append(ServerInfoCommand.Help.getName()).append(" ")
                            .append(ServerInfoCommand.Help.getArguments()).append("`")
                            .append(" - ").append(ServerInfoCommand.Help.getDescription()).append("\n");
            help.append("`").append(UserInfoCommand.Help.getName()).append(" ")
                            .append(UserInfoCommand.Help.getArguments()).append("`")
                            .append(" - ").append(UserInfoCommand.Help.getDescription()).append("\n");
            help.append("`").append(WumpusCommand.Help.getName()).append(" ")
                            .append(WumpusCommand.Help.getArguments()).append("`")
                            .append(" - ").append(WumpusCommand.Help.getDescription()).append("\n");
            help.append("`").append(Covid19Command.Help.getName()).append(" ")
                            .append(Covid19Command.Help.getArguments()).append("`")
                            .append(" - ").append(Covid19Command.Help.getDescription()).append("\n");
            help.append("`").append(Holiday.Help.getName()).append(" ")
                            .append(Holiday.Help.getArguments()).append("`")
                            .append(" - ").append(Holiday.Help.getDescription()).append("\n");

            mc.sendTyping().queue();
            mc.sendMessage(help.toString()).queue();
        }
        else if (message.startsWith("!choose")) {
            ChooseCommand.execute(event, message);
        }
        else if (message.startsWith("!420")) {
            CopyPastaCommand.execute(event, message);
        }
        else if (message.startsWith("!meme")) {
            MemeCommand.execute(event);
        }
        else if (message.startsWith("!generate")) {
            NumberGeneratorCommand.execute(event, message);
        }
        else if (message.startsWith("!poll")) {
            PollCommand.execute(event, message);
        }
        else if (message.startsWith("!server")) {
            ServerInfoCommand.execute(event);
        }
        else if (message.startsWith("!user")) {
            UserInfoCommand.execute(event, message);
        }
        else if (WumpusCommand.isRunning() || message.startsWith("!wumpus")) {
            WumpusCommand.execute(event, message);
        }
        else if (message.startsWith("!purge")) {
            PurgeCommand.execute(event);
        }
        else if (message.startsWith("!covid")) {
            Covid19Command.execute(event, message);
        }
        else if ((ChessCommand.isRunning() || message.startsWith("!chess")) && mc.getName().equals("chess")) {
            ChessCommand.execute(event, message);
        }
        else if (message.startsWith("!oregontrail") || OregonTrailCommand.gameInProgress()) {
            OregonTrailCommand.execute(event, message);
        }
    }
}
