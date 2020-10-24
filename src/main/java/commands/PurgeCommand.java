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
            event.getChannel().sendMessage("Invalid format for purge. Ex: `!purge 100`").queue();
            return;
        }
        if (i < 1) {
            event.getChannel().sendMessage("Must purge atleast 1 message").queue();
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
            MessageHistory history = new MessageHistory(channel);

            int messagesDeleted = 0;
            while (true) {
                List<Message> msgs;
                if (amount - messagesDeleted > 100) {
                    msgs = history.retrievePast(100).complete();
                }
                else {
                    msgs = history.retrievePast(amount).complete();
                }
                channel.deleteMessages(msgs).complete();
                messagesDeleted += msgs.size();
                System.out.println("msgSize:" + msgs.size());
                System.out.println("messagesDeleted:" + messagesDeleted);
                if (messagesDeleted >= amount) {
                    break;
                }
            }
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
