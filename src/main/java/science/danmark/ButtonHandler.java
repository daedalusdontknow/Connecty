package science.danmark;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.ResultSet;

public class ButtonHandler extends ListenerAdapter {
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        switch (event.getButton().getId()) {
            case "ds_set_global_chat" -> setGlobalChat(event);
            case "ds_remove_global_chat" -> removeGlobalChat(event);

            case "ds_privacy" -> privacy(event);
        }
    }

    private void privacy(ButtonInteractionEvent event) {
        //add the user to the database
        ResultSet rs = SqlManager.getStatement("SELECT * FROM users WHERE UserID = '" + event.getUser().getId() + "'");
        try {
            if (rs.next()) {
                event.reply("You are already registered!").setEphemeral(true).queue();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        SqlManager.executeStatement("INSERT INTO users (UserID, RoleID, State) VALUES ('" + event.getUser().getId() + "', '0', 1)");
        event.reply("You are now registered!").setEphemeral(true).queue();
    }

    private void removeGlobalChat(ButtonInteractionEvent event) {
        if (event.getGuild() == null) {
            //check if the user is already setup
            ResultSet rs = SqlManager.getStatement("SELECT * FROM usersChannels WHERE UserID = '" + event.getUser().getId() + "'");
            try {
                if (!rs.next()) {
                    event.reply("You are not setup!").setEphemeral(true).queue();
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //remove the user from the database
            SqlManager.executeStatement("DELETE FROM usersChannels WHERE UserID = '" + event.getUser().getId() + "'");
            event.reply("You have been removed from the global chat!").setEphemeral(true).queue();
        } else {
            //check if the server is already setup
            ResultSet rs = SqlManager.getStatement("SELECT * FROM servers WHERE GuildID = '" + event.getGuild().getId() + "'");
            try {
                if (!rs.next()) {
                    event.reply("This server is not setup!").setEphemeral(true).queue();
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //remove the server from the database
            ServerManager.deleteServer(event.getGuild().getId());
            event.reply("This server has been removed from the global chat!").setEphemeral(true).queue();
        }
    }

    private void setGlobalChat(ButtonInteractionEvent event) {
        //check if its a private channel or a guild channel
        if (event.getGuild() == null) {
            //check if the user is already setup
            //            statement.execute("CREATE TABLE IF NOT EXISTS `usersChannels` (UserID VARCHAR(255), ChannelID VARCHAR(255))");
            ResultSet rs = SqlManager.getStatement("SELECT * FROM usersChannels WHERE UserID = '" + event.getUser().getId() + "'");
            try {
                if (rs.next()) {
                    event.reply("You are already setup!").setEphemeral(true).queue();
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //add the user to the database
            SqlManager.executeStatement("INSERT INTO usersChannels (UserID) VALUES ('" + event.getUser().getId() + "')");
            event.reply("You have been added to the global chat!").setEphemeral(true).queue();
        } else {
            //check if the server is already setup
            ResultSet rs = SqlManager.getStatement("SELECT * FROM servers WHERE GuildID = '" + event.getGuild().getId() + "'");
            try {
                if (rs.next()) {
                    event.reply("This server is already setup!").setEphemeral(true).queue();
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //add the server to the database
            ServerManager.addServer(event.getGuild().getId(), event.getChannel().getId());
            event.reply("This server has been added to the global chat!").setEphemeral(true).queue();
        }
    }
}
