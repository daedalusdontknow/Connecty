package science.danmark;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;
import java.util.List;

public class CommandHandler extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "settings" -> settings(event);
            case "announce" -> announce(event);
            case "ping" -> ping(event);
            case "leaderboard" -> leaderboard(event);
            case "report" -> report(event);
            case "admin" -> admin(event);
        }
    }

    private void admin(SlashCommandInteractionEvent event) {
    }

    private void report(SlashCommandInteractionEvent event) {
        if (!event.isFromGuild()) {
            event.reply("You need to contact the support to report a message!").setEphemeral(true).queue();
            return;
        }

        String messageID = event.getOption("messageid").getAsString();
        String reason = event.getOption("reason").getAsString();

        event.reply("The message has been reported!").setEphemeral(true).queue();

        SqlManager.executeStatement("INSERT INTO reports (messageID, reason) VALUES ('" + messageID + "', '" + reason + "')");
    }

    private void leaderboard(SlashCommandInteractionEvent event) {
    }

    private void ping(SlashCommandInteractionEvent event) {
        long time = System.currentTimeMillis();
        event.reply("Waiting for response").setEphemeral(true)
                .flatMap(v ->
                        event.getHook().editOriginalFormat("ResponseTime: %d ms", System.currentTimeMillis() - time) // then edit original
                ).queue();
    }

    private void announce(SlashCommandInteractionEvent event) {
    }

    private void settings(SlashCommandInteractionEvent event) {
        //check if the user has admin rights, is the guild owner or if its a private chat the command is executed in, if not, return a message

        if (event.isFromGuild()) {
            if (!event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                event.reply("You need to be an admin of the server to use this command!").setEphemeral(true).queue();
                return;
            }
        }

        EmbedBuilder setup = new EmbedBuilder();
        //emoji id 1302583809664880732, name Connecty
        setup.setTitle("<:Connecty:1302583809664880732> Settings <:Connecty:1302583809664880732>");
        setup.setDescription("Globalchat Settings for your server/DM");
        setup.setFooter("Choose an Action you want to perform");

        List<Button> buttons = new ArrayList<>();
        buttons.add(Button.primary("ds_set_global_chat", "Set Global Chat").withEmoji(Emoji.fromUnicode("📥")));
        buttons.add(Button.primary("ds_remove_global_chat", "Remove Global Chat").withEmoji(Emoji.fromUnicode("📤")));

        event.replyEmbeds(setup.build()).addActionRow(buttons).setEphemeral(true).queue();
    }
}
