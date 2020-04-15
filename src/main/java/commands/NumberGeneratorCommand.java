package commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import java.util.regex.Pattern;

public class NumberGeneratorCommand {

    public static void execute(MessageReceivedEvent event, String message) {
        Pattern p1 = Pattern.compile("!generate");
        Pattern p2 = Pattern.compile("!generate \\d* \\d*");
        String[] responses = new String[] {
                "Here's your number ",
                "Here you go ",
                "Am I just a calculator to you? ",
                "Easy peasy. ",
                "You coulda just used Random.org "
        };
        message = message.trim();
        if (p1.matcher(message).matches()) {
            int rand = getRandomNumber();
            event.getChannel().sendTyping().queue();
            event.getChannel().sendMessage(responses[getRandomNumber(0, responses.length-1)] + rand).queue();
        } else if (p2.matcher(message).matches()) {
            String [] nums = message.split("\\s+");
            int first = Integer.parseInt(nums[1]);
            int second = Integer.parseInt(nums[2]);

            event.getChannel().sendTyping().queue();

            if (first > second) {
                event.getChannel().sendMessage("You dumbass, that doesn't make any sense").queue();
            } else {
                int rand = getRandomNumber(first, second);
                event.getChannel().sendMessage(responses[getRandomNumber(0, responses.length-1)] + rand).queue();
            }
        }

    }

    public static int getRandomNumber() {
        return getRandomNumber(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public static int getRandomNumber(long min, long max) {
        return (int) (min + Math.random() * (max - min + 1));
    }

    public static class Help {
        static String name = "!generate";
        static String description = "creates a random number";
        static String arguments = "<min> <max> (optional)";
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
