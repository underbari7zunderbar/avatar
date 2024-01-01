package site.revanilla.avatar.events

import com.destroystokyo.paper.event.player.PlayerUseUnknownEntityEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
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
import site.revanilla.avatar.AvatarManager.npc
import site.revanilla.avatar.AvatarManager.openInventory

object AvatarEvent : Listener {

    @EventHandler
    fun PlayerJoinEvent.onJoin() {
        despawnAvatar()
        fakeServer.addPlayer(player)
        copyTo(player)
        val avatarInventory = linkedInventories[player.uniqueId] ?: return
        avatarInventory.close()
    }

    @EventHandler
    fun PlayerQuitEvent.onQuit() {
        fakeServer.removePlayer(player)
        createAvatar(player, player.location.clone().apply {
            pitch = 0f
            yaw = 0f
        })

        npc!!.updateEquipment {
            helmet = player.inventory.helmet
            chestplate = player.inventory.chestplate
            leggings = player.inventory.leggings
            boots = player.inventory.boots
            setItemInMainHand(player.inventory.itemInMainHand)
            setItemInOffHand(player.inventory.itemInOffHand)
        }
    }

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val slot = event.rawSlot

        if (slot == 0 && event.view.title() == Component.text("ѐЀ", NamedTextColor.WHITE)) {
            event.isCancelled = true
            return
        }

        val player = event.whoClicked as? Player

        if (event.view.title() == Component.text("ѐЀ", NamedTextColor.WHITE) && player != null && !player.hasPermission("avt.avt")) {
            event.isCancelled = true
        }

        if (slot < 54) {
            val item = event.currentItem

            if (item?.type == Material.BARRIER && event.view.title() == Component.text("ѐЀ", NamedTextColor.WHITE)) {
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