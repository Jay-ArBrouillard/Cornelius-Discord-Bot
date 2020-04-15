import Utils.CommandHelper;
import Utils.MovieWatcher;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import javax.security.auth.login.LoginException;

public class Main {

    public static String[] activities = new String[] {
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

    public static void main(String[] args) throws LoginException, InterruptedException {
        // config.txt contains two lines
        // the first is the bot token
        String token = System.getenv("TOKEN");
        // the second is the bot's owner's id
        String ownerId = System.getenv("OWNER_ID");
        String moviesApiKey = System.getenv("MOVIES_API_KEY");
        String witServerAccessToken = System.getenv("WIT_AI_TOKEN");

        int rand = (int)(Math.random()*activities.length);
        String movieTitle = activities[rand];
        // start getting a bot account set up
        JDA jda = JDABuilder.createDefault(token)
                  .addEventListeners(new CommandHelper(ownerId))
                  .setStatus(OnlineStatus.ONLINE)
                  .setActivity(Activity.watching(movieTitle))
                  .setAutoReconnect(true)
                  .build()
                  .awaitReady();
        MovieWatcher movieWatcher = new MovieWatcher(moviesApiKey, jda);
        jda.addEventListener(movieWatcher);
    }
}
