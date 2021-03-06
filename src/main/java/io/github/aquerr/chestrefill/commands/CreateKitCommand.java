package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.PluginInfo;
import io.github.aquerr.chestrefill.entities.SelectionMode;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class CreateKitCommand extends AbstractCommand implements CommandExecutor
{
    public CreateKitCommand(ChestRefill plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        Optional<String> optionalName = context.getOne(Text.of("kit name"));

        if(!optionalName.isPresent())
        {
            source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, "You must specify a kit name!"));
            return CommandResult.empty();
        }

        if(super.getPlugin().getContainerManager().getKits().keySet().stream().anyMatch(x->x.equals(optionalName.get())))
        {
            source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, "Kit with given name already exists!"));
            return CommandResult.empty();
        }

        if(!(source instanceof Player))
        {
            source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, "Only in-game players can use this command!"));
            return CommandResult.empty();
        }

        Player player = (Player)source;
        if (ChestRefill.PLAYER_CHEST_SELECTION_MODE.containsKey(player.getUniqueId()))
        {
            if (SelectionMode.CREATE_KIT != ChestRefill.PLAYER_CHEST_SELECTION_MODE.get(player.getUniqueId()))
            {
                optionalName.ifPresent(s -> ChestRefill.PLAYER_KIT_NAME.put(player.getUniqueId(), s));
                ChestRefill.PLAYER_CHEST_SELECTION_MODE.replace(player.getUniqueId(), SelectionMode.CREATE_KIT);
                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.YELLOW, "Turned on kit creation mode"));
            }
            else
            {
                ChestRefill.PLAYER_KIT_NAME.remove(player.getUniqueId());
                ChestRefill.PLAYER_CHEST_SELECTION_MODE.remove(player.getUniqueId());
                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.YELLOW, "Turned off kit creation mode"));
            }
        }
        else
        {
            optionalName.ifPresent(s -> ChestRefill.PLAYER_KIT_NAME.put(player.getUniqueId(), s));
            ChestRefill.PLAYER_CHEST_SELECTION_MODE.put(player.getUniqueId(), SelectionMode.CREATE_KIT);
            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.YELLOW, "Turned on kit creation mode"));
        }

        return CommandResult.success();
    }
}
