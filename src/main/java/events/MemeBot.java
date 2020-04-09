package events;

import com.kdotj.simplegiphy.SimpleGiphy;
import com.kdotj.simplegiphy.data.Giphy;
import com.kdotj.simplegiphy.data.GiphyListResponse;
import com.kdotj.simplegiphy.data.GiphyResponse;
import com.kdotj.simplegiphy.data.RandomGiphyResponse;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class MemeBot extends ListenerAdapter {

    public static final String [] RATINGS = new String[] {"G", "PG", "PG-13", "R"};
    public static final String HEROKUAPP_URL = "https://meme-api.herokuapp.com/gimme/dankmemes/1";
    public static final String IMGFLIP_URL = "https://api.imgflip.com/get_memes";

    public void onGuildMessageReceived (GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        String message = event.getMessage().getContentRaw();
        if (!message.contains("!meme")) return;

        StringBuilder result = new StringBuilder();
        String line;

        String url = HEROKUAPP_URL;//Math.random() < 0.8 ? HEROKUAPP_URL : IMGFLIP_URL;

        // add user agent
        URLConnection urlConnection = null;
        try {
            urlConnection = new URL(url).openConnection();

            urlConnection.addRequestProperty("User-Agent", "Mozilla");
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(10000);

            try (InputStream is = new URL(url).openStream();
                 BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

                while ((line = br.readLine()) != null) {
                    result.append(line);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONObject obj = new JSONObject(result.toString());
        ArrayList<Meme> memeUrls = new ArrayList<Meme>();
        try {
            JSONArray array = obj.getJSONObject("data").getJSONArray("memes");
            for(int i = 0 ; i < array.length() ; i++){
                JSONObject curr = array.getJSONObject(i);
                memeUrls.add(new Meme(curr.getString("name"), curr.getString("url")));
            }
        } catch (JSONException je) {
            JSONArray array = new JSONObject(result.toString()).getJSONArray("memes");
            for(int i = 0 ; i < array.length() ; i++){
                JSONObject curr = array.getJSONObject(i);
                Meme m = new Meme();
                m.name = curr.getString("title");
                m.url = curr.getString("url");
                m.postLink = curr.getString("postLink");
                m.subreddit = "dankmemes";
                memeUrls.add(m);
            }
        }

        String query = null;
        System.out.println(message);
        if (message.contains("!gif")) {
            query = message.substring(message.indexOf("!gif") + 4).trim();
            SimpleGiphy.setApiKey("");
            GiphyListResponse response = null;
            if (query == null || query.isEmpty()) {
                RandomGiphyResponse single = SimpleGiphy.getInstance().random("", "50");
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle(single.getRandomGiphy().getCaption());
                eb.setImage(single.getRandomGiphy().getUrl());
                eb.setDescription(single.getRandomGiphy().getUsername());
                eb.setColor(Color.getHSBColor(getRandomNumber(0, 360), getRandomNumber(0,100), getRandomNumber(0, 100)));
                event.getChannel().sendMessage(eb.build()).queue();
            } else {
                response = SimpleGiphy.getInstance().search(query, "50", "0", RATINGS[0]);
                if (response == null) {
                    event.getChannel().sendMessage("Reached 42 gif quota for the day :(").queue();
                    return;
                }
                ArrayList<Giphy> list = (ArrayList<Giphy>) response.getData();

                EmbedBuilder eb = new EmbedBuilder();
                int rand = getRandomNumber(0, list.size()-1);
                Giphy selectedGif = list.get(rand);
                String url2 = selectedGif.getImages().getOriginal().getUrl();
                eb.setTitle(selectedGif.getCaption(), url2);
                eb.setDescription(selectedGif.getBitlyGifUrl());
                eb.setFooter(selectedGif.getSlug());
                eb.setImage(url2);
                eb.setColor(Color.getHSBColor(getRandomNumber(0, 360), getRandomNumber(0,100), getRandomNumber(0, 100)));
                event.getChannel().sendMessage(eb.build()).queue();
            }

        } else {
            EmbedBuilder eb = new EmbedBuilder();
            int rand = getRandomNumber(0, memeUrls.size()-1);
            Meme meme = memeUrls.get(rand);
            eb.setTitle(meme.getName());
            eb.setImage(meme.getUrl());
            eb.setColor(Color.getHSBColor(getRandomNumber(0, 360), getRandomNumber(0,100), getRandomNumber(0, 100)));
            event.getChannel().sendMessage(eb.build()).queue();
        }


    }

    public int getRandomNumber() {
        return getRandomNumber(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public int getRandomNumber(long min, long max) {
        return (int) (min + Math.random() * (max - min + 1));
    }

    class Meme {
        String name;
        String url;
        String postLink;
        String subreddit;

        Meme() {

        }

        Meme (String name, String url) {
            this.name = name;
            this.url = url;
        }

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }

        public String getPostLink() {
            return postLink;
        }

        public String getSubreddit() {
            return subreddit;
        }
    }
}
