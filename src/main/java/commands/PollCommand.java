package commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;

public class PollCommand extends ListenerAdapter {

    public static void execute(MessageReceivedEvent event, String message) {
        message = message.substring(message.indexOf("!poll") + 5);
        message = "**" + message + "**";
        if (!message.contains("?")) message += "?";

        //Delete original message
        event.getMessage().delete().queue();

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Poll created by " + event.getAuthor().getName());
        eb.setColor(Color.getHSBColor(getRandomNumber(0, 360), getRandomNumber(0,100), getRandomNumber(0, 100)));
        eb.setDescription(message);
        eb.setFooter("React to vote");

        event.getChannel().sendMessage(eb.build()).queue( (msg) -> {
            msg.addReaction("U+1F44D").queue();
            msg.addReaction("U+1F44E").queue();
        });
    }

    public static int getRandomNumber(long min, long max) {
        return (int) (min + Math.random() * (max - min + 1));
    }

    public static class Help {
        static String name = "!poll";
        static String description = "creates a poll";
        static String arguments = "<Poll Question>";
        static boolean guildOnly = false;

        public static String getName() {
            return name;
        }

        public static String getDescription() {
            return description;
        }

        public static String getArguments() {
            return arguments;
        }

        public boolean isGuildOnly() {
            return guildOnly;
        }
    }
}
