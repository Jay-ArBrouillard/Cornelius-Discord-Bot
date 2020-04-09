import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import events.*;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import com.jagrosh.jdautilities.command.CommandClientBuilder;

public class Main {

    public static void main(String[] args) throws LoginException, IOException {
        // config.txt contains two lines
        // the first is the bot token
        String token = System.getenv("TOKEN");
        // the second is the bot's owner's id
        String ownerId = System.getenv("OWNER_ID");

        String[] activities = new String[] {
                "Shrek",
                "Space Jam",
                "How to Simp III",
                "The Room",
                "Trolls 2",
                "Sex Lives of the Potato Men",
                "The Last Airbender",
                "Hobgoblin",
                "National Treasure",
                "Ghost Rider"
        };

        // start getting a bot account set up
        JDA jda = JDABuilder.createDefault(token)
                  .addEventListeners(new NumberGeneratorBot(), new WumpusBot())
                  .setStatus(OnlineStatus.ONLINE)
                  .setActivity(Activity.watching(activities[getRandomNumber(0, activities.length-1)]))
                  .setAutoReconnect(true)
                  .build();

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 2);
        today.set(Calendar.MINUTE, 35);
        today.set(Calendar.SECOND, 0);

        // every night at 2am you run your task
        TimerTask changeActivityTask = new TimerTask() {
            @Override
            public void run() {
                jda.getPresence().setPresence(Activity.watching(activities[getRandomNumber(0, activities.length-1)]), true);
            }
        };

        Timer timer = new Timer();
        timer.schedule(changeActivityTask, today.getTime(), TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS)); // period: 1 day
    }

    public static int getRandomNumber() {
        return getRandomNumber(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public static int getRandomNumber(long min, long max) {
        return (int) (min + Math.random() * (max - min + 1));
    }


}
