package events;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.List;

public class PollBot extends ListenerAdapter {
    public PollBot() {

    }

    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        String message = event.getMessage().getContentRaw();
        if (!message.contains("!poll")) return;
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

    public int getRandomNumber(long min, long max) {
        return (int) (min + Math.random() * (max - min + 1));
    }
}
