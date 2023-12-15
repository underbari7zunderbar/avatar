package site.revanilla.avatar.events

import com.destroystokyo.paper.event.player.PlayerUseUnknownEntityEvent
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
import site.revanilla.avatar.AvatarManager.copyFrom
import site.revanilla.avatar.AvatarManager.createCorpseNPC
import site.revanilla.avatar.AvatarManager.despawnAvatar
import site.revanilla.avatar.AvatarManager.fakePlayers
import site.revanilla.avatar.AvatarManager.fakeServer
import site.revanilla.avatar.AvatarManager.openInventory
import site.revanilla.avatar.AvatarManager.updateAvatarArmor
import site.revanilla.avatar.AvatarManager.updateInv


object AvatarEvent : Listener {

    var avatarLoaded = false
    @EventHandler
    fun PlayerJoinEvent.onJoin() {
        fakeServer.addPlayer(player)
        updateInv(player)
        copyFrom(player)
        if (avatarLoaded) {
            despawnAvatar()
        }
        //despawnAvatar()
        //avatarLoaded = false
    }

    @EventHandler
    fun PlayerQuitEvent.onQuit() {
        fakeServer.removePlayer(player)
        createCorpseNPC(player, player.location.clone().apply {
            pitch = 0f
            yaw = 0f
            while (block.type.isAir) {
                y -= 0.005
            }
        })

        AvatarManager.npc!!.updateEquipment {
            helmet = player.inventory.helmet
            chestplate = player.inventory.chestplate
            leggings = player.inventory.leggings
            boots = player.inventory.boots
            setItemInMainHand(player.inventory.itemInMainHand)
            setItemInOffHand(player.inventory.itemInOffHand)
        }

        player.inventory.clear()
        updateAvatarArmor()
        avatarLoaded = true
    }

    /*@EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val clickedInventory = event.clickedInventory
        val action = event.action
        val clickedSlot = event.slot
        val currentItem = event.currentItem

        if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY || action == InventoryAction.PICKUP_ONE || action == InventoryAction.PICKUP_HALF || action == InventoryAction.PICKUP_ALL) {
            if (clickedInventory != null && clickedSlot != InventoryView.OUTSIDE) {
                // Check if an item is removed from the inventory
                if (currentItem == null) {
                    // Handle item removal here
                    println("Item removed from inventory")
                }
            }
        }
    }*/

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val slot = event.rawSlot

        if (slot == 0) {
            event.isCancelled = true
            return
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