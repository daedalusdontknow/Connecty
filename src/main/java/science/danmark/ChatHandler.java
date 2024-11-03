package science.danmark;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChatHandler extends ListenerAdapter  {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        sendGlobalMessage(event);
    }

    public static Set<String> bannedWords = new HashSet<>();

    public static void getBannedWords() {
        //get banned words from database
        try {
            java.sql.ResultSet resultSet = SqlManager.getStatement("SELECT * FROM bannedWords");

            while (resultSet.next()) {
                bannedWords.add(resultSet.getString("Word"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean chatFilter(String text) {
        for (String word : bannedWords) if (text.contains(word)) return true;
        return false;
    }

    public static void sendGlobalMessage(MessageReceivedEvent event) {
        //if the message is from a bot, ignore it
        if (event.getAuthor().isBot()) return;
        //check if the message was sent in a guild, if so delete it
        if (event.isFromGuild()) {
            try {
                java.sql.ResultSet rs = SqlManager.getStatement("SELECT * FROM servers WHERE GuildID = '" + event.getGuild().getId() + "' AND ChannelID = '" + event.getChannel().getId() + "'");
                if (!rs.next()) return;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        //check if the userID is setup for the global chat or banned
        //            statement.execute("CREATE TABLE IF NOT EXISTS `users` (UserID VARCHAR(255), RoleID VARCHAR(255), State INT DEFAULT 1)");
        try {
            java.sql.ResultSet rs = SqlManager.getStatement("SELECT * FROM users WHERE UserID = '" + event.getAuthor().getId() + "'");
            if (!rs.next()) {
                EmbedBuilder message = new EmbedBuilder();
                message.setTitle("Privacy Policy");
                message.setDescription("By using this bot you agree to the privacy policy");
                message.addField("What do we collect?", "We collect your UserID, the messages you send and the state of the user.", true);
                message.addField("What do we do with the data?", "We store it in a database and use it to display the user's name and messages in the global chat.", true);
                message.addField("How can I delete my data?", "You can delete your data by contacting a Team member.", true);
                message.addField("When can´t i get my data deleted?", "If you were banned, you cannot appeal to get your data deleted, due to investigations that might be ongoing.", true);

                message.setFooter("By clicking the button below you agree to the privacy policy.");
                message.setColor(Color.red);

                List<net.dv8tion.jda.api.interactions.components.buttons.Button> Buttons = new ArrayList<>();
                Buttons.add(Button.primary("ds_privacy", "I agree"));

                event.getAuthor().openPrivateChannel().queue((channel) -> {
                    channel.sendMessageEmbeds(message.build()).setActionRow(Buttons).queue();
                });

                event.getMessage().getChannel().sendMessage("Please check your DMs!" + event.getAuthor().getAsMention()).complete().delete().queueAfter(5, java.util.concurrent.TimeUnit.SECONDS);
                event.getMessage().delete().queue();
                return;
            }
            if (rs.getInt("State") == 0) {
                event.getAuthor().openPrivateChannel().queue(channel -> channel.sendMessage("You are banned!").queue());
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //check if the message contains a banned word
        if (chatFilter(event.getMessage().getContentRaw())) {
            event.getMessage().delete().queue();
            return;
        }

        //send the message to all servers
        String userTag = event.getAuthor().getName();
        String userIcon = event.getAuthor().getAvatarUrl();
        String message = event.getMessage().getContentRaw();
        //check if the message contains an file,
        if (event.getMessage().getAttachments().size() > 0) {
            //send the user a message that files are not allowed
            event.getAuthor().openPrivateChannel().queue(channel -> channel.sendMessage("Files are not allowed!").queue());
        }
        if (event.isFromGuild()) {
            event.getMessage().delete().queue();
        }

        for (int i= 0; i < ServerManager.servers.size(); i++) {
            String guildID = ServerManager.servers.get(i)[0];
            String channelID = ServerManager.servers.get(i)[1];

            //check if the server still exists and if the channel still exists, else delete the server from the database
            if (event.getJDA().getTextChannelById(channelID) == null) continue;

            TextChannel channel = event.getJDA().getTextChannelById(channelID);
            channel.createWebhook("🌍 - " + userTag).queue((webhook) -> {
                webhook.sendMessage(message)
                        .setAvatarUrl(userIcon)
                        .queue(sentMessage -> {
                            String messageId = sentMessage.getId(); // Get the message ID here
                            SqlManager.executeStatement("INSERT INTO messages (UserID, MessageID, GuildID, ChannelID, Message) VALUES ('" + event.getAuthor().getId() + "', '" + messageId + "', '" + guildID + "', '" + channelID + "', '" + message + "')");
                            webhook.delete().queue();
                        });
            });
        }

        //next send the message to all users userChannels
        for (int i = 0; i < ServerManager.userChannels.size(); i++) {
            String userID = ServerManager.userChannels.get(i)[0];

            // Check if the UserID is the same as the author of the message
            if (userID.equals(event.getAuthor().getId())) continue;

            // Retrieve user by ID (will attempt to fetch if not cached)
            event.getJDA().retrieveUserById(userID).queue((user) -> {
                if (user != null) {
                    // Open private channel and send the message
                    user.openPrivateChannel().queue((channel) -> {
                        EmbedBuilder embed = new EmbedBuilder();
                        embed.setTitle("Global Chat");
                        embed.setDescription(message);
                        embed.setColor(Color.darkGray); // Set a color to make it visually distinct
                        embed.setThumbnail(userIcon); // Set the user's icon as the thumbnail
                        embed.setFooter("" + userTag); // Add a footer with the user's name and icon

                        channel.sendMessageEmbeds(embed.build()).queue();
                    });
                }
            }, (error) -> {
                System.err.println("Failed to retrieve user: " + userID);
            });
        }

    }
}