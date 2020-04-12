package events;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.lang3.StringUtils;

public class CopyPastaBot extends ListenerAdapter {

    public void onMessageReceived (MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String msg = event.getMessage().getContentRaw().trim();
        String temp = "grabs brim of brightly colored :red_circle::blue_circle:hat :tophat::ok_hand_tone1:YEAH yeah :stuck_out_tongue_closed_eyes::stuck_out_tongue_closed_eyes: what up :arrow_up: what up :arrow_up: youtube :video_camera::iphone:youtuuUUUuuuUuuUUUbbee :video_camera::iphone::video_camera::iphone::video_camera::iphone::video_camera: whaddup :arrow_up: Joli Oli :man_tone2:cummin' :tongue::sweat_drops: at ya :weary: f:four:r CuustomGrooow :herb::maple_leaf: FourTwennnnttttyyyyyy :dash::four::two::zero: :wind_blowing_face: THISEIGHTEEN:one::eight:ANDOVER :baby_tone3::person_gesturing_no_tone2:CHANNELDESIGNEDF:four:R CANNIBISPATIENTS :maple_leaf::hospital: AND ADULTS :man_tone4::woman_tone3: Alright :ok_hand_tone1:man:person_walking_tone2: in this one :one:THE LUNG BUSTER :x::sushi::x: :three::zero: INCHES :eggplant:OF DOOM :skull_crossbones::crossed_swords:This video :video_camera: is going up :arrow_up: into the crazy :office::cyclone: hit:punch_tone1: library :european_post_office::book:and this video :video_camera: is all f:four:r fun :joy::ok_hand_tone3::100: If at any point:pen_fountain: in time :clock12::clock3::clock6::clock9: you :point_up_tone1:enjoy :ok_hand_tone3::100::relieved: this video :video_camera: Make sure you :point_up_tone1:give this video :video_camera: the thumbs :thumbsup_tone1::thumbsup_tone1: up :arrow_up::arrow_up:! And if you :point_up_tone1:haven't :x::person_gesturing_no_tone2: subscribed :heavy_check_mark: to CustomGrow:herb::maple_leaf::four::two::zero: already, subscribe :heavy_check_mark: f:four:r more.";
        String [] input = msg.split(" ");
        if (input.length == 2 && input[0].equals("!420") && StringUtils.isNumeric(input[1])) {
            for (int i = 0; i < Integer.parseInt(input[1]); i++) {
                event.getChannel().sendMessage(temp).queue();
            }
        }
        else if (msg.equals("!420")) {
            event.getChannel().sendMessage(temp).queue();
        }

        switch (msg) {
            case "!bitch":
            case "!asshole":
            case "!bellend":
            case "!ass":
            case "!cock":
            case "!shit":
            case "!fuck":
            case "!motherfucker":
                event.getChannel().sendMessage("Don't say swearz!").queue();
                break;
        }

    }
}
