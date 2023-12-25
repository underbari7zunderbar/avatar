package site.revanilla.avatar.events

import com.destroystokyo.paper.event.player.PlayerUseUnknownEntityEvent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import site.revanilla.avatar.AvatarManager
import site.revanilla.avatar.AvatarManager.avatarInventory
import site.revanilla.avatar.AvatarManager.copyTo
import site.revanilla.avatar.AvatarManager.createAvatar
import site.revanilla.avatar.AvatarManager.despawnAvatar
import site.revanilla.avatar.AvatarManager.fakePlayers
import site.revanilla.avatar.AvatarManager.fakeServer
import site.revanilla.avatar.AvatarManager.openInventory
import site.revanilla.avatar.AvatarManager.updateAvatarArmor


object AvatarEvent : Listener {

    var avatarLoaded = false

    @EventHandler
    fun PlayerJoinEvent.onJoin() {
        fakeServer.addPlayer(player)
        copyTo(player)
        for (onlinePlayer in Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.openInventory.topInventory == avatarInventory) {
                onlinePlayer.closeInventory()
            }
        }

        if (avatarLoaded) {
            despawnAvatar()
        }
        updateAvatarArmor()
        player.updateInventory()
        despawnAvatar()
        avatarLoaded = false
    }

    /*@EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val whoClicked = event.whoClicked

        if (whoClicked is Player) {
            val slot = event.rawSlot

            if (slot >= 0 && slot < whoClicked.inventory.size) {
                whoClicked.sendMessage("클릭한 슬롯 번호: $slot")
            }
        }
    }*/

    @EventHandler
    fun PlayerQuitEvent.onQuit() {
        fakeServer.removePlayer(player)
        createAvatar(player, player.location.clone().apply {
            pitch = 0f
            yaw = 0f
        })

        AvatarManager.npc!!.updateEquipment {
            helmet = player.inventory.helmet
            chestplate = player.inventory.chestplate
            leggings = player.inventory.leggings
            boots = player.inventory.boots
            setItemInMainHand(player.inventory.itemInMainHand)
            setItemInOffHand(player.inventory.itemInOffHand)
        }

        updateAvatarArmor()
        avatarLoaded = true
    }

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        updateAvatarArmor()
        val slot = event.rawSlot

        if (slot == 0) {
            event.isCancelled = true
            return
        }

        for (i in 3 until 9) {
            updateAvatarArmor()
        }
        val clickedInventory = event.clickedInventory
        val player = event.whoClicked as? Player

        if (clickedInventory == avatarInventory && player != null && !player.hasPermission("avt.avt")) {
            event.isCancelled = true
        }

        if (slot < 54) {
            val item = event.currentItem

            if (item?.type == Material.BARRIER) {
                event.isCancelled = true
                return
            }
        }
        updateAvatarArmor()
    }

    @EventHandler(ignoreCancelled = true)
    fun PlayerToggleSneakEvent.onToggleSneak() {
        if (isSneaking) {
            fakePlayers.find {
                it.bukkitEntity.location.distance(player.location) <= 1.5 ||
                        it.bukkitEntity.location.clone().subtract(1.0, 0.0, 0.0).distance(player.location) <= 1.5 ||
                        it.bukkitEntity.location.clone().subtract(2.0, 0.0, 0.0).distance(player.location) <= 1.5
            }?.let {
                openInventory(player, it)
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