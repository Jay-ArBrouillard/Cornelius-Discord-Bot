import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import events.*;
import kong.unirest.*;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import net.dv8tion.jda.api.entities.TextChannel;

public class Main {

    public static int[] delay = {0}; //ms
    public static int[] timeLeft = {0}; //ms
    public static boolean watchingMovie = false;
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
    public static final String[] movieTitle = new String[1];
    public static String moviesApiKey;

    public static void main(String[] args) throws LoginException, InterruptedException {
        // config.txt contains two lines
        // the first is the bot token
        String token = System.getenv("TOKEN");
        // the second is the bot's owner's id
        String ownerId = System.getenv("OWNER_ID");
        moviesApiKey = System.getenv("MOVIES_API_KEY");

        EventWaiter waiter = new EventWaiter();

        // define a command client
        CommandClientBuilder client = new CommandClientBuilder();

        // The default is "Type !!help" (or whatever prefix you set)
        client.useDefaultGame();

        // sets the owner of the bot
        client.setOwnerId(ownerId);

        // sets emojis used throughout the bot on successes, warnings, and failures
        client.setEmojis("\uD83D\uDE03", "\uD83D\uDE2E", "\uD83D\uDE26");

        // sets the bot prefix
        client.setPrefix("!");

        // adds commands
        client.addCommands(new ChooseBot(), new HelloBot(waiter), new MemeBot(), new UserInfoBot());


        int rand = getRandomNumber(0, activities.length-1);
        movieTitle[0] = activities[rand];
        // start getting a bot account set up
        JDA jda = JDABuilder.createDefault(token)
                  .addEventListeners(new PollBot(), new WitBot(), new NumberGeneratorBot(), new WumpusBot(), new CopyPastaBot(), waiter, client.build())
                  .setStatus(OnlineStatus.ONLINE)
                  .setActivity(Activity.watching(movieTitle[0]))
                  .setAutoReconnect(true)
                  .build()
                  .awaitReady();

        startTimer(jda);
    }

    public static void startTimer(JDA jda) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                if (timeLeft[0] > 0) {
                    timeLeft[0] -= 1; //subtract a minute in ms
                    jda.getPresence().setPresence(Activity.watching(" (" + timeLeft[0] + "m) " + movieTitle[0]), true);
                    return;
                }
                Unirest.get("https://api.themoviedb.org/3/discover/movie?api_key="+moviesApiKey+ "&language=en-US&include_adult=true&include_video=false&page="+getRandomNumber(1,500)).asJsonAsync(new Callback<JsonNode>() {
                    // The API call was successful
                    @Override
                    public void completed(HttpResponse<JsonNode> hr) {
                        JSONObject o = hr.getBody().getObject();
                        if (o == null || o.isEmpty()) return;
                        JSONArray results = o.getJSONArray("results");
                        if (results == null || results.isEmpty()) return;
                        int random = getRandomNumber(0, results.length()-1);
                        JSONObject movie = results.getJSONObject(random);
                        List<TextChannel> channels = jda.getTextChannelsByName("general", true); //Only return 1 text channel for each
                        for(TextChannel ch : channels)
                        {
                            getMovieData(jda, ch, movie);
                        }
                    }

                    // The API call failed
                    @Override
                    public void failed(UnirestException ue) {
                        System.out.println("failed");
                    }

                    // The API call was cancelled (this should never happen)
                    @Override
                    public void cancelled() {
                        System.out.println("cancelled");
                    }
                });
            }
        }, delay[0], 1000*60);
    }

    public static void getMovieData(JDA jda, TextChannel ch, JSONObject discoverApi) {
        Unirest.get("https://api.themoviedb.org/3/movie/" + discoverApi.getString("id") + "?api_key="+moviesApiKey).asJsonAsync(new Callback<JsonNode>() {
            @Override
            public void completed(HttpResponse<JsonNode> hr) {
                JSONObject movieApi = hr.getBody().getObject();
                if (movieApi == null || movieApi.isEmpty()) return;
                timeLeft[0] = movieApi.getInt("runtime");

                String genreString = "";
                JSONArray genresArr = movieApi.getJSONArray("genres");
                if (genresArr != null || !genresArr.isEmpty()) {
                    for (int i = 0; i < genresArr.length(); i++) {
                        JSONObject gen = genresArr.getJSONObject(i);
                        genreString += gen.getString("name") + ", ";
                    }
                }
                else {
                    genreString = "None";
                }

                String countriesString = "";
                JSONArray prodCountryArr = movieApi.getJSONArray("production_countries");
                if (prodCountryArr != null || !prodCountryArr.isEmpty()) {
                    for (int i = 0; i < prodCountryArr.length(); i++) {
                        JSONObject gen = prodCountryArr.getJSONObject(i);
                        countriesString += gen.getString("name") + ", ";
                    }
                }
                else {
                    countriesString = "None";
                }

                String companiesString = "";
                JSONArray prodCompaniesArr = movieApi.getJSONArray("production_companies");
                if (prodCompaniesArr != null || !prodCompaniesArr.isEmpty()) {
                    for (int i = 0; i < prodCompaniesArr.length(); i++) {
                        JSONObject gen = prodCompaniesArr.getJSONObject(i);
                        companiesString += gen.getString("name") + ", ";
                    }
                }
                else {
                    companiesString = "None";
                }

                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("Cornelius finished watching `" + movieTitle[0] + "`");
                eb.setColor(Color.getHSBColor(getRandomNumber(0, 360), getRandomNumber(0,100), getRandomNumber(0, 100)));
                eb.addField("He is now watching", discoverApi.getString("title"), false);
                eb.addField("Genre(s): ", genreString, true);
                eb.addField("Popularity: ", discoverApi.getString("popularity"), true);
                eb.addField("Vote Count: ", discoverApi.getString("vote_count"), true);
                eb.addField("Rating: ", discoverApi.getString("vote_average"), true);
                eb.addField("Release: ", discoverApi.getString("release_date"), true);
                eb.addField("Budget: ", "$" + movieApi.getString("budget"), true);
                eb.addField("Revenue: ", "$" + movieApi.getString("revenue"), true);
                eb.addField("Production Country(s): ", countriesString, true);
                eb.addField("Production Companies(s): ", companiesString, true);
                if (movieApi.getString("tagline") != null) eb.setDescription(movieApi.getString("tagline"));
                eb.setFooter(discoverApi.getString("overview"));
                movieTitle[0] = discoverApi.getString("title");
                ch.sendMessage(eb.build()).queue();
                jda.getPresence().setPresence(Activity.watching(" (" + timeLeft[0] + "m) " + movieTitle[0]), true);
            }

            // The API call failed
            @Override
            public void failed(UnirestException ue) {
                System.out.println("failed");
            }

            // The API call was cancelled (this should never happen)
            @Override
            public void cancelled() {
                System.out.println("cancelled");
            }
        });
    }

    public static int getRandomNumber() {
        return getRandomNumber(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public static int getRandomNumber(long min, long max) {
        return (int) (min + Math.random() * (max - min + 1));
    }
}
