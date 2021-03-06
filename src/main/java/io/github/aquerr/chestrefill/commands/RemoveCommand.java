package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.entities.SelectionMode;
import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.PluginInfo;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

/**
 * Created by Aquerr on 2018-02-15.
 */
public class RemoveCommand extends AbstractCommand implements CommandExecutor
{
    public RemoveCommand(ChestRefill plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        if (source instanceof Player)
        {
            Player player = (Player)source;

            if (ChestRefill.PLAYER_CHEST_SELECTION_MODE.containsKey(player.getUniqueId()))
            {
                if (SelectionMode.REMOVE != ChestRefill.PLAYER_CHEST_SELECTION_MODE.get(player.getUniqueId()))
                {
                    ChestRefill.PLAYER_CHEST_SELECTION_MODE.replace(player.getUniqueId(), SelectionMode.REMOVE);
                    player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.YELLOW, "Turned on removal mode"));
                }
                else
                {
                    ChestRefill.PLAYER_CHEST_SELECTION_MODE.remove(player.getUniqueId());
                    player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.YELLOW, "Turned off removal mode"));
                }
            }
            else
            {
                ChestRefill.PLAYER_CHEST_SELECTION_MODE.put(player.getUniqueId(), SelectionMode.REMOVE);
                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.YELLOW, "Turned on removal mode"));
            }
        }

        return CommandResult.success();
    }
}
