@file:Suppress("DEPRECATION")

package site.revanilla.avatar

import io.github.monun.tap.fake.FakeEntity
import io.github.monun.tap.fake.FakeEntityServer
import io.github.monun.tap.mojangapi.MojangAPI
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.entity.Pose
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.util.*


@Suppress("UNCHECKED_CAST")
object AvatarManager {
    val plugin = AvatarPlugin.instance
    val server = plugin.server
    val fakeServer = FakeEntityServer.create(plugin)
    val fakePlayers get() = fakeServer.entities.filter { it.bukkitEntity is Player } as List<FakeEntity<Player>>
    val linkedInventory = HashMap<UUID, Inventory>()
    val avatars = arrayListOf<AvatarData>()
    var taskId = 0
    val linkedInventories = HashMap<UUID, Inventory>()
    var npc: FakeEntity<Player>? = fakePlayers.firstOrNull()

    fun despawnAvatar() {
        npc?.run {
            remove()
            npc = null
        }
    }

    fun createAvatarFromData(
        avatarData: AvatarData,
        isLoaded: Boolean = false,
        skinProfile: MojangAPI.SkinProfile? = null
    ) {
        val profile = skinProfile ?: MojangAPI.fetchSkinProfileAsync(avatarData.uniqueId).get()

        profile?.let {
            npc = fakeServer.spawnPlayer(avatarData.location, avatarData.name, profile.profileProperties().toSet())

            npc!!.updateMetadata {
                pose = Pose.SLEEPING
                linkedInventory[uniqueId] = avatarData.inventory

            }

            if (!isLoaded) avatars += AvatarData.from(npc!!, avatarData.uniqueId)
        }
    }


    private fun createAvatarInventory(player: Player): Inventory {
        val avatarInventory = server.createInventory(null, 54, text("ѐЀ", NamedTextColor.WHITE))

        val barrierItem = ItemStack(Material.BARRIER)
        val barrierMeta = barrierItem.itemMeta
        barrierMeta.displayName(
            text()
                .content("${player.name}의 가방")
                .decoration(TextDecoration.ITALIC, false)
                .color(NamedTextColor.GRAY)
                .build()
        )
        barrierMeta.lore = null
        barrierMeta.setCustomModelData(1)
        barrierItem.itemMeta = barrierMeta

        avatarInventory.setItem(1, barrierItem)
        avatarInventory.setItem(2, barrierItem)
        avatarInventory.setItem(3, player.inventory.helmet)
        avatarInventory.setItem(4, player.inventory.chestplate)
        avatarInventory.setItem(5, player.inventory.leggings)
        avatarInventory.setItem(6, player.inventory.boots)
        avatarInventory.setItem(7, barrierItem)
        avatarInventory.setItem(8, player.inventory.itemInOffHand)

        val skull = ItemStack(Material.PLAYER_HEAD, 1)
        val skullMeta = skull.itemMeta as SkullMeta
        skullMeta.owningPlayer = player
        skullMeta.displayName(
            text()
                .content(player.name)
                .decoration(TextDecoration.ITALIC, false)
                .color(NamedTextColor.WHITE)
                .build()
        )
        skullMeta.lore = null
        skull.itemMeta = skullMeta
        avatarInventory.setItem(0, skull)
        for (i in 9..17) {
            avatarInventory.setItem(i, barrierItem)
        }

        val storageContents = player.inventory.storageContents
        for (i in 18..53) {
            if (i - 18 < storageContents.size && storageContents[i - 18] != null) {
                avatarInventory.setItem(i, storageContents[i - 18])
            }
        }

        linkedInventories[player.uniqueId] = avatarInventory
        return avatarInventory
    }

    fun copyTo(player: Player) {
        val avatarInventory = linkedInventories[player.uniqueId] ?: return
        val playerInventory = player.inventory

        for (i in 0 until 9) {
            val avatarItem = avatarInventory.getItem(i + 18)
            if (avatarItem != null) {
                playerInventory.setItem(i, avatarItem)
            }
        }

        val armorSlots = listOf(3, 4, 5, 6, 8)
        for (slot in armorSlots) {
            val avatarItem = avatarInventory.getItem(slot)
            if (avatarItem != null) {
                when (slot) {
                    3 -> playerInventory.helmet = avatarItem
                    4 -> playerInventory.chestplate = avatarItem
                    5 -> playerInventory.leggings = avatarItem
                    6 -> playerInventory.boots = avatarItem
                    8 -> playerInventory.setItemInOffHand(avatarItem)
                }
            }
        }

        for (i in 27 until 54) {
            val avatarItem = avatarInventory.getItem(i)
            if (avatarItem != null) {
                playerInventory.setItem(i - 18, avatarItem)
            }
        }
        for (i in 0 until 9) {
            val avatarItem = avatarInventory.getItem(i + 18)
            if (avatarItem == null) {
                playerInventory.setItem(i, null)
            }
        }

        for (slot in armorSlots) {
            val avatarItem = avatarInventory.getItem(slot)
            if (avatarItem == null) {
                when (slot) {
                    3 -> playerInventory.helmet = null
                    4 -> playerInventory.chestplate = null
                    5 -> playerInventory.leggings = null
                    6 -> playerInventory.boots = null
                    8 -> playerInventory.setItemInOffHand(null)
                }
            }
        }

        for (i in 27 until 54) {
            val avatarItem = avatarInventory.getItem(i)
            if (avatarItem == null) {
                playerInventory.setItem(i - 18, null)
            }
        }
    }

    fun createAvatar(player: Player, deathLocation: Location) {
        val avatarInventory = createAvatarInventory(player)
        val avatarData = AvatarData(deathLocation, player.uniqueId, avatarInventory, player.name)

        avatars += avatarData

        linkedInventories[player.uniqueId] = avatarInventory

        createAvatarFromData(avatarData)
    }

    fun openInventory(player: Player, body: FakeEntity<Player>) =
        player.openInventory(linkedInventory[body.bukkitEntity.uniqueId]!!)
}