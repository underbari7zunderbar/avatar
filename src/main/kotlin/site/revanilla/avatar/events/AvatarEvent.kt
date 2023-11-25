package site.revanilla.avatar.events

import com.destroystokyo.paper.event.player.PlayerUseUnknownEntityEvent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.scheduler.BukkitRunnable
import site.revanilla.avatar.AvatarManager
import site.revanilla.avatar.AvatarManager.copyInventoryToAvatar
import site.revanilla.avatar.AvatarManager.createCorpseNPC
import site.revanilla.avatar.AvatarManager.despawnAvatar
import site.revanilla.avatar.AvatarManager.fakePlayers
import site.revanilla.avatar.AvatarManager.fakeServer
import site.revanilla.avatar.AvatarManager.openInventory
import site.revanilla.avatar.AvatarManager.updateAvatarInventory
import site.revanilla.avatar.AvatarPlugin.Companion.instance


object AvatarEvent : Listener {
    @EventHandler
    fun PlayerJoinEvent.onJoin() {
        copyInventoryToAvatar(player)
        fakeServer.addPlayer(player)
        despawnAvatar()
    }

    private var avatarTask: BukkitRunnable? = null

    fun startAvatarUpdater() {
        avatarTask = object : BukkitRunnable() {
            override fun run() {
                updateAllAvatarsInventories()
            }
        }

        avatarTask?.runTaskTimer(instance, 0L, 20L)
    }

    fun cancelAvatarUpdater() {
        avatarTask?.cancel()
    }

    fun updateAllAvatarsInventories() {
        for (offlinePlayer in Bukkit.getOnlinePlayers()) {
            (offlinePlayer as Player)
            updateAvatarInventory(offlinePlayer)
        }
    }
    /*private fun updateAvatars() {
        AvatarManager.updateAvatarInventory(player)
    }*/
    @EventHandler
    fun PlayerQuitEvent.onQuit() {
        fakeServer.removePlayer(player)
        createCorpseNPC(player, player.location.clone().apply {
            pitch = 0f
            yaw = 0f
            while (block.type.isAir) { y -= 0.005 }
        })

        /*AvatarManager.npc!!.updateEquipment {
            helmet = player.inventory.helmet
            chestplate = player.inventory.chestplate
            leggings = player.inventory.leggings
            boots = player.inventory.boots
            setItemInMainHand(player.inventory.itemInMainHand)
            setItemInOffHand(player.inventory.itemInOffHand)
        }*/

        player.inventory.clear()
    }

    @EventHandler
    fun onInventoryDrag(event: InventoryDragEvent) {
        val player = event.whoClicked as? Player ?: return
        AvatarManager.updateAvatarInventory(player)
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        AvatarManager.updateAvatarInventory(player)
    }

    /*@EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val clickedInventory = event.clickedInventory
        val player = event.whoClicked as? Player ?: return

        if (clickedInventory != null) {
            val clickedSlot = event.slot
            player.sendMessage("Clicked slot: $clickedSlot")
        }
    }*/

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val clickedInventory = event.clickedInventory
        val player = event.whoClicked

        if (clickedInventory != null && player is Player) {
            AvatarManager.updateAvatarInventory(player)
        }
    }
    @EventHandler
    fun PlayerUseUnknownEntityEvent.onUseUnknownEntity() {
        fakePlayers.find { it.bukkitEntity.entityId == entityId }?.let {
            openInventory(player, it)
        }
    }
}