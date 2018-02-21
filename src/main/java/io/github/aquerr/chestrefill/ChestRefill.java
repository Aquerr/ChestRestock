package io.github.aquerr.chestrefill;

import io.github.aquerr.chestrefill.commands.*;
import io.github.aquerr.chestrefill.listeners.ChestBreakListener;
import io.github.aquerr.chestrefill.listeners.RightClickListener;
import io.github.aquerr.chestrefill.managers.ChestManager;
import io.github.aquerr.chestrefill.version.VersionChecker;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameLoadCompleteEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.*;


/**
 * Created by Aquerr on 2018-02-09.
 */

@Plugin(id = PluginInfo.Id, name = PluginInfo.Name, version = PluginInfo.Version, description = PluginInfo.Description, authors = PluginInfo.Authors, url = PluginInfo.Url)
public class ChestRefill
{
    private VersionChecker versionChecker;

    public static Map<List<String>, CommandSpec> Subcommands = new HashMap<>();

    public static Map<UUID, ChestMode> PlayersChestMode = new HashMap<>();
    public static Map<UUID, Integer> ChestTimeChangePlayer = new HashMap<>();

    private static ChestRefill chestRefill;
    public static ChestRefill getChestRefill() {return chestRefill;}

    @Inject
    private Logger _logger;
    public Logger getLogger() {return _logger;}

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path _configDir;
    public Path getConfigDir() {return _configDir;}

    @Listener
    public void onGameInitialization(GameInitializationEvent event)
    {
        chestRefill = this;
        versionChecker = new VersionChecker();

        ChestManager.setupChestManager(_configDir);

        Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.YELLOW, "Chest Refill is loading... :D"));
        Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.YELLOW, "Initializing commands..."));

        initCommands();

        Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.YELLOW, "Initializing listeners..."));

        initListeners();

        Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.YELLOW, "Chest Refill is ready!"));

        //TODO: Check if there is a new version available...



        if (VersionChecker.isLatest(PluginInfo.Version))
        {

        }

    }

    @Listener
    public void onGameLoad(GameLoadCompleteEvent event)
    {
        //Start refilling chests that were created on the server before
        ChestManager.restoreRefilling();
    }

    private void initCommands()
    {

        //Help Command
        Subcommands.put(Arrays.asList("help"), CommandSpec.builder()
            .description(Text.of("Displays all available commands"))
            .permission(PluginPermissions.HELP_COMMAND)
            .executor(new HelpCommand())
            .build());

        //Create Command
        Subcommands.put(Arrays.asList("c", "create"), CommandSpec.builder()
            .description(Text.of("Toggles chest creation mode"))
            .permission(PluginPermissions.CREATE_COMMAND)
            .executor(new CreateCommand())
            .build());

        //Remove Command
        Subcommands.put(Arrays.asList("r", "remove"), CommandSpec.builder()
            .description(Text.of("Toggles chest removal mode"))
            .permission(PluginPermissions.REMOVE_COMMAND)
            .executor(new RemoveCommand())
            .build());

        //Update Command
        Subcommands.put(Arrays.asList("u", "update"), CommandSpec.builder()
            .description(Text.of("Toggles chest update mode"))
            .permission(PluginPermissions.UPDATE_COMMAND)
            .executor(new UpdateCommand())
            .build());

        //Time Command
        Subcommands.put(Arrays.asList("t", "time"), CommandSpec.builder()
            .description(Text.of("Change chest's refill time"))
            .permission(PluginPermissions.TIME_COMMAND)
            .arguments(GenericArguments.optional(GenericArguments.integer(Text.of("time"))))
            .executor(new TimeCommand())
            .build());

        //List Command
        Subcommands.put(Arrays.asList("l","list"), CommandSpec.builder()
                .description(Text.of("Show all refilling chests"))
                .permission(PluginPermissions.LIST_COMMAND)
                .executor(new ListCommand())
                .build());

        //Build all commands
        CommandSpec mainCommand = CommandSpec.builder()
                .description(Text.of("Displays all available commands"))
                .executor(new HelpCommand())
                .children(Subcommands)
                .build();

        //Register commands
        Sponge.getCommandManager().register(this, mainCommand, "chestrefill", "cr");

    }

    private void initListeners()
    {
        Sponge.getEventManager().registerListeners(this, new RightClickListener());
        Sponge.getEventManager().registerListeners(this, new ChestBreakListener());
    }
}
