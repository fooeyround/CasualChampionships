package net.casualuhc.uhc.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.casualuhc.arcade.utils.CommandSourceUtils.success
import net.casualuhc.arcade.utils.PlayerUtils
import net.casualuhc.arcade.utils.TeamUtils.asPlayerTeam
import net.casualuhc.uhc.UHCMod
import net.casualuhc.uhc.extensions.TeamFlag.Ignored
import net.casualuhc.uhc.extensions.TeamFlag.Ready
import net.casualuhc.uhc.extensions.TeamFlagsExtension.Companion.flags
import net.casualuhc.uhc.managers.TeamManager
import net.casualuhc.uhc.util.Texts
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component

object ReadyCommand: Command {
    private val NOT_NOW = SimpleCommandExceptionType(Texts.LOBBY_READY_NOT_NOW)
    private val NO_TEAM = SimpleCommandExceptionType(Texts.LOBBY_READY_NO_TEAM)
    private val ALREADY_READY = SimpleCommandExceptionType(Texts.LOBBY_READY_ALREADY)

    override fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("ready").then(
                Commands.literal("yes").executes {
                    this.ready(it, true)
                }
            ).then(
                Commands.literal("no").executes {
                    this.ready(it, false)
                }
            ).then(
                Commands.literal("reset").requires {
                    it.hasPermission(4)
                }.executes(this::unreadyAll)
            ).then(
                Commands.literal("awaiting").requires {
                    it.hasPermission(4)
                }.executes(this::awaiting)
            )
        )
    }

    private fun ready(context: CommandContext<CommandSourceStack>, ready: Boolean): Int {
        if (!UHCMod.minigame.isReadyPhase()) {
            throw NOT_NOW.create()
        }
        val team = context.source.playerOrException.team
        if (team === null || team.flags.has(Ignored)) {
            throw NO_TEAM.create()
        }
        if (team.flags.has(Ready)) {
            throw ALREADY_READY.create()
        }
        team.flags.set(Ready, ready)
        val generator = (if (ready) Texts.LOBBY_IS_READY else Texts.LOBBY_NOT_READY)
        val message = generator.generate(team.asPlayerTeam().formattedDisplayName)
        PlayerUtils.broadcast(message)
        TeamManager.checkAllTeamsReady()
        return 1
    }

    private fun unreadyAll(context: CommandContext<CommandSourceStack>): Int {
        context.source.server.scoreboard.playerTeams.forEach { team ->
            team.flags.set(Ready, false)
        }
        return 1
    }

    private fun awaiting(context: CommandContext<CommandSourceStack>): Int {
        if (!UHCMod.minigame.isReadyPhase()) {
            throw NOT_NOW.create()
        }
        context.source.success(Component.literal("The following teams are not ready:"), true)
        for (team in TeamManager.getUnreadyTeams()) {
            context.source.success(team.displayName, true)
        }
        return 1
    }
}