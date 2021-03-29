package com.elchologamer.userlogin.commands;

import com.elchologamer.userlogin.UserLogin;
import com.elchologamer.userlogin.api.event.AuthenticationEvent;
import com.elchologamer.userlogin.api.types.AuthType;
import com.elchologamer.userlogin.util.database.Database;
import com.elchologamer.userlogin.util.extensions.QuickMap;
import com.elchologamer.userlogin.util.extensions.ULPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.UUID;

public class RegisterCommand extends AuthCommand {

    public RegisterCommand() {
        super("register", AuthType.REGISTER, 2);
    }

    private static final UserLogin plugin = UserLogin.getPlugin();

    @Override
    protected boolean authenticate(ULPlayer ulPlayer, String[] args) {
        Database db = getPlugin().getDB();
        FileConfiguration config = plugin.getConfig();

        // Check if player is not already registered
        UUID uuid = ulPlayer.getPlayer().getUniqueId();
        if (db.isRegistered(uuid)) {
            ulPlayer.sendPathMessage("messages.already_registered");
            return false;
        }

        boolean registerEnabled = config.getBoolean("register.enabled");

        if (!registerEnabled) {
            ulPlayer.sendPathMessage("messages.register_disabled");
            return false;
        }

        String password = args[0];

        int minChars = config.getInt("password.minCharacters", 0);
        int maxChars = config.getInt("password.maxChars", 128);

        // Check password length
        if (password.length() < minChars) {
            ulPlayer.sendPathMessage(
                    "messages.short_password",
                    new QuickMap<>("chars", minChars)
            );
            return false;
        }

        if (password.length() > maxChars) {
            ulPlayer.sendPathMessage(
                    "messages.long_password",
                    new QuickMap<>("chars", maxChars)
            );
            return false;
        }

        // Check password regex
        String regex = config.getString("password.regex", "").trim();
        if (!regex.equals("") && !password.matches(regex)) {
            ulPlayer.sendPathMessage(
                    "messages.regex_mismatch",
                    new QuickMap<>("regex", regex)
            );
            return false;
        }

        // Check that both passwords match
        if (!password.equals(args[1])) {
            ulPlayer.sendPathMessage("messages.different_passwords");
            return false;
        }

        try {
            db.createPassword(uuid, password);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            ulPlayer.sendPathMessage("messages.register_failed");
            return false;
        }
    }
}
