package site.revanilla.avatar.events

import com.destroystokyo.paper.event.player.PlayerUseUnknownEntityEvent
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.TextColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import site.revanilla.avatar.AvatarManager.copyTo
import site.revanilla.avatar.AvatarManager.createAvatar
import site.revanilla.avatar.AvatarManager.despawnAvatar
import site.revanilla.avatar.AvatarManager.fakePlayers
import site.revanilla.avatar.AvatarManager.fakeServer
import site.revanilla.avatar.AvatarManager.linkedInventories
import site.revanilla.avatar.AvatarManager.openInventory

object AvatarEvent : Listener {

    private var isRide = false
    @EventHandler
    fun PlayerJoinEvent.onJoin() {
        despawnAvatar(player.uniqueId)
        fakeServer.addPlayer(player)
        copyTo(player)
        val avatarInventory = linkedInventories[player.uniqueId] ?: return
        avatarInventory.close()
        println(isRide)
        if (isRide) {
            val py = player.location.y
            player.location.y = py + 1
        }
    }
    @EventHandler
    fun PlayerQuitEvent.onQuit() {
        despawnAvatar(player.uniqueId)
        if (player.gameMode != GameMode.SPECTATOR && !player.hasPermission("avt.avt")) {
            fakeServer.removePlayer(player)
            createAvatar(player, player.location.clone().apply {
                pitch = 0f
                yaw = 0f
                if (player.isInsideVehicle) {
                    y += 1
                    isRide = true
                }
            })
        }
    }

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val slot = event.rawSlot

        if (slot == 0 && event.view.title() == text("ѐЀ").color(TextColor.color(198,198,198))) {
            event.isCancelled = true
            return
        }

        val player = event.whoClicked as? Player

        if (event.view.title() == text("ѐЀ").color(TextColor.color(198,198,198)) && player != null && !player.hasPermission("avt.avt")) {
            event.isCancelled = true
        }

        if (slot < 54) {
            val item = event.currentItem

            if (item?.type == Material.LIGHT_GRAY_STAINED_GLASS_PANE && event.view.title() == text("ѐЀ").color(
                    TextColor.color(198,198,198))) {
                event.isCancelled = true
                return
            }
        }
    }

    @EventHandler
    fun PlayerUseUnknownEntityEvent.onUseUnknownEntity() {
        fakePlayers.find { it.bukkitEntity.entityId == entityId }?.let {
            openInventory(player, it)
        }
    }
}