package commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;

public class ChooseCommand {

    public static void execute(MessageReceivedEvent event, String message) {
        // split the choices on all whitespace
        String [] items = message.trim().substring(message.indexOf("!choose")+6).trim().split("\\s+");
        // check that the user provided choices
        if(Arrays.stream(items).allMatch(String::isEmpty))
        {
            event.getChannel().sendTyping().queue();
            event.getChannel().sendMessage("You didn't give me any choices!").queue();
        }
        else
        {
            event.getChannel().sendTyping().queue();
            // if there is only one option, have a special reply
            if(items.length==1) {
                event.getChannel().sendMessage("You only gave me one option, `"+items[0]+"`").queue();
            }
            else // otherwise, pick a random response
            {
                event.getChannel().sendMessage("I choose `"+items[(int)(Math.random()*items.length)]+"`").queue();
            }
        }
    }

    public static class Help {
        static String name = "!choose";
        static String description = "make a decision";
        static String arguments = "<oregontrail.item> <oregontrail.item> ...";
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
