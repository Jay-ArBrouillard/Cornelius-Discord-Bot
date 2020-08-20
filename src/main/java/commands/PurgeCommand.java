package commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.RateLimitedException;

import java.awt.*;
import java.util.List;

public class PurgeCommand {

    public static void execute(MessageReceivedEvent event) {
        String [] args = event.getMessage().getContentRaw().split(" ");
        int i = 0;
        try {
            i = Integer.parseInt(args[1]);
        } catch (Exception nfe) {
            return;
        }
        if (i < 1 || i > 100) {
            event.getChannel().sendMessage("You can only purge between 1 and 100 messages").queue();
            return;
        }

        TextChannel channel = event.getTextChannel();

        boolean isAdmin = event.getMember().getPermissions(channel).contains(Permission.ADMINISTRATOR);

        if (isAdmin) {
            clear(channel, i);
        }
    }

    public static void clear(TextChannel channel, int amount)
    {
        System.out.println("Deleting messages in channel: " + channel);

        //OffsetDateTime twoWeeksAgo = OffsetDateTime.now().minus(2, ChronoUnit.WEEKS);

        new Thread(() ->
        {
                List<Message> messages = channel.getHistory().retrievePast(amount).complete();
                //messages.removeIf(m -> m.getCreationTime().isBefore(twoWeeksAgo));
                messages.remove(messages);
                if (messages.isEmpty())
                {
                    return;
                }
//                messages.forEach(m -> System.out.println("Deleting: " + m));
                channel.deleteMessages(messages).complete();
        }).run();

    }

    public static class Help {
        static String name = "!purge";
        static String description = "deletes up to 100 messages in the given text channel";
        static String arguments = "<number>";
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
