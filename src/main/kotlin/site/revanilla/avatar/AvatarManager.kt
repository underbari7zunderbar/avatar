@file:Suppress("DEPRECATION")

package site.revanilla.avatar

import io.github.monun.tap.fake.FakeEntity
import io.github.monun.tap.fake.FakeEntityServer
import io.github.monun.tap.mojangapi.MojangAPI
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.entity.Pose
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.inventory.meta.SkullMeta
import java.util.*


@Suppress("UNCHECKED_CAST")
object AvatarManager {
    val plugin = AvatarPlugin.instance
    val server = plugin.server
    val fakeServer = FakeEntityServer.create(plugin)
    val fakePlayers get() = fakeServer.entities.filter { it.bukkitEntity is Player } as List<FakeEntity<Player>>
    val linkedInventory = HashMap<UUID, Inventory>()
    val corpses = arrayListOf<AvatarData>()
    var taskId = 0

    var npc: FakeEntity<Player>? = null
    fun despawnAvatar() {
        npc?.run {
            remove()
            npc = null
        }
    }

    fun copyInventoryToAvatar(player: Player) {
        npc?.let { _ ->
            val corpseData = corpses.find { it.uniqueId == player.uniqueId }
            corpseData?.let {
                copyTo(it, player.inventory)
            }
        }
    }

    fun updateAvatarInventory(player: Player) {
        npc?.let { _ ->
            val corpseData = corpses.find { it.uniqueId == player.uniqueId }
            corpseData?.let {
                val avatarInventory = linkedInventory[it.uniqueId]
                val playerInventory = player.inventory

                if (avatarInventory != null) {
                    for (i in 0 until avatarInventory.size) {
                        val avatarItem = avatarInventory.getItem(i)
                        val playerItem = playerInventory.getItem(i)

                        if (avatarItem != playerItem) {
                            avatarInventory.setItem(i, playerItem)
                        }
                    }
                }
            }
        }
    }

    private fun copyTo(corpseData: AvatarData, inv: PlayerInventory) {
        inv.helmet = corpseData.inventory.getItem(39)
        inv.chestplate = corpseData.inventory.getItem(38)
        inv.leggings = corpseData.inventory.getItem(37)
        inv.boots = corpseData.inventory.getItem(36)
        inv.setItemInOffHand(corpseData.inventory.getItem(40))
        inv.setItemInMainHand(corpseData.inventory.getItem(0))

        for (i in 1 until 36) {
            inv.setItem(i, corpseData.inventory.getItem(i))
        }
    }

    fun createAvatarFromData(
        corpseData: AvatarData,
        isLoaded: Boolean = false,
        skinProfile: MojangAPI.SkinProfile? = null
    ) {
        val profile = skinProfile ?: MojangAPI.fetchSkinProfileAsync(corpseData.uniqueId).get()

        profile?.let {
            npc = fakeServer.spawnPlayer(corpseData.location, corpseData.name, profile.profileProperties().toSet())

            npc!!.updateMetadata {
                pose = Pose.SLEEPING
                linkedInventory[uniqueId] = corpseData.inventory

            }

                if (!isLoaded) corpses += AvatarData.from(npc!!, corpseData.uniqueId)
            }
        }

    private fun createAvatarInventory(player: Player): Inventory {
        val avatarInventory = server.createInventory(null, 54, text("Ѐ", NamedTextColor.DARK_GRAY))

        val barrier = Material.BARRIER
        val item = ItemStack(barrier)
        val m = item.itemMeta
        m.lore?.clear()
        avatarInventory.setItem(1, ItemStack(barrier))
        avatarInventory.setItem(2, ItemStack(barrier))
        avatarInventory.setItem(3, player.inventory.helmet)
        avatarInventory.setItem(4, player.inventory.chestplate)
        avatarInventory.setItem(5, player.inventory.leggings)
        avatarInventory.setItem(6, player.inventory.boots)
        avatarInventory.setItem(7, ItemStack(barrier))
        avatarInventory.setItem(8, player.inventory.itemInOffHand)

        val skull = ItemStack(Material.PLAYER_HEAD, 1)
        val meta = skull.itemMeta as SkullMeta
        meta.setOwner(player.name)
        meta.setDisplayName(player.name)
        meta.lore?.removeAt(0)
        skull.itemMeta = meta
        avatarInventory.setItem(0, skull)

        for (i in 9 until 18) {
            avatarInventory.setItem(i, ItemStack(barrier))
        }

        val storageContents = player.inventory.storageContents
        for (i in 18 until 54) {
            if (i - 18 < storageContents.size && storageContents[i - 18] != null) {
                avatarInventory.setItem(i, storageContents[i - 18])
            }
        }

        return avatarInventory
    }

        fun createCorpseNPC(player: Player, deathLocation: Location) = createAvatarFromData(
            AvatarData(deathLocation, player.uniqueId, createAvatarInventory(player), player.name)
        )

        fun openInventory(player: Player, body: FakeEntity<Player>) =
            player.openInventory(linkedInventory[body.bukkitEntity.uniqueId]!!)
    }