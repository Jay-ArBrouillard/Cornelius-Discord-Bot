package Utils;

import kong.unirest.*;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MovieWatcher extends ListenerAdapter {

    public int delay = 0; //ms
    public int timeLeft = 0; //ms
    public String movieTitle;
    private String moviesApiKey;
    private Timer timer = new Timer();
    private JDA jda;
    private boolean isBreak = false;


    public MovieWatcher(String moviesApiKey, JDA jda) {
        this.moviesApiKey = moviesApiKey;
        this.jda = jda;
        startTimer(jda);
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (event.getMessage().getAuthor().isBot()) return;
        String message = event.getMessage().getContentRaw();
        if (message.contains("!break")) {
            String [] args = message.split(" ");
            int i = 0;
            try {
                 i = Integer.parseInt(args[1]);
            } catch (Exception nfe) {
                return;
            }
            if (i < 0) return;
            timeLeft = i;
            isBreak = true;
            timer.cancel();
            timer = new Timer();
            startTimer(jda);
            event.getChannel().sendTyping().queue();
            event.getChannel().sendMessage("Ok! I will take a " + timeLeft + "m break").queue();
            jda.getPresence().setPresence(Activity.playing("(" + timeLeft + "m) breaking..."), true);
        }
        else if (message.contains("!watch")) {
            timeLeft = 0;
            event.getChannel().sendTyping().queue();
            event.getChannel().sendMessage("Ok! I'll find something else to watch, gimme a minute...").queue();
            isBreak = false;
        }
    }

    public void startTimer(JDA jda) {
        timer.schedule(new TimerTask() {
            public void run() {
                if (timeLeft > 0) {
                    timeLeft -= 1; //subtract a minute
                    if (isBreak) {
                        jda.getPresence().setPresence(Activity.playing("(" + timeLeft + "m) breaking..."), true);
                    }
                    else {
                        jda.getPresence().setPresence(Activity.watching(" (" + timeLeft + "m) " + movieTitle), true);
                    }
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
                        delay = getRandomNumber(getRandomNumber(0,10),getRandomNumber(10,60));
                        jda.getPresence().setPresence(Activity.playing("!help or @Cornelius"), true);
                    }

                    // The API call was cancelled (this should never happen)
                    @Override
                    public void cancelled() {
                        delay = getRandomNumber(getRandomNumber(0,10),getRandomNumber(10,60));
                        jda.getPresence().setPresence(Activity.playing("!help or @Cornelius"), true);
                    }
                });
            }
        }, delay, 1000*60);
    }

    public void getMovieData(JDA jda, TextChannel ch, JSONObject discoverApi) {
        Unirest.get("https://api.themoviedb.org/3/movie/" + discoverApi.getString("id") + "?api_key="+moviesApiKey).asJsonAsync(new Callback<JsonNode>() {
            @Override
            public void completed(HttpResponse<JsonNode> hr) {
                JSONObject movieApi = hr.getBody().getObject();
                if (movieApi == null || movieApi.isEmpty()) return;
                timeLeft = movieApi.getInt("runtime");

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
                eb.setTitle("Cornelius finished watching `" + movieTitle + "`");
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
                movieTitle = discoverApi.getString("title");
                ch.sendMessage(eb.build()).queue();
                jda.getPresence().setPresence(Activity.watching(" (" + timeLeft + "m) " + movieTitle), true);
            }

            // The API call failed
            @Override
            public void failed(UnirestException ue) {
                delay = getRandomNumber(getRandomNumber(0,10),getRandomNumber(10,60));
                jda.getPresence().setPresence(Activity.playing("!help or @Cornelius"), true);
            }

            // The API call was cancelled (this should never happen)
            @Override
            public void cancelled() {
                delay = getRandomNumber(getRandomNumber(0,10),getRandomNumber(10,60));
                jda.getPresence().setPresence(Activity.playing("!help or @Cornelius"), true);
            }
        });
    }

    public int getRandomNumber() {
        return getRandomNumber(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public int getRandomNumber(long min, long max) {
        return (int) (min + Math.random() * (max - min + 1));
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }
}
