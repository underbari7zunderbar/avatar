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
import site.revanilla.avatar.events.AvatarEvent.avatarLoaded
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

    var npc: FakeEntity<Player>? = fakePlayers.firstOrNull()

    fun despawnAvatar() {
        npc?.run {
            remove()
            npc = null
        }
    }

    fun updateAvatarArmor() {
        npc?.apply {
            updateEquipment {
                setHelmet(avatarInventory.getItem(3)?.clone(), true)
                setChestplate(avatarInventory.getItem(4)?.clone(), true)
                setLeggings(avatarInventory.getItem(5)?.clone(), true)
                setBoots(avatarInventory.getItem(6)?.clone(), true)
                setItemInMainHand(avatarInventory.getItem(18)?.clone(), true)
                setItemInOffHand(avatarInventory.getItem(8)?.clone(), true)
            }
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

    val avatarInventory = server.createInventory(null, 54, text("ѐЀ", NamedTextColor.WHITE))
    private fun createAvatarInventory(player: Player): Inventory {

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
        for (i in 18 ..53) {
            if (i - 18 < storageContents.size && storageContents[i - 18] != null) {
                avatarInventory.setItem(i, storageContents[i - 18])
            }
        }

        return avatarInventory
    }

    fun copyFrom(player: Player) {
        val inv = player.inventory

        avatarInventory.setItem(3, inv.helmet)
        avatarInventory.setItem(4, inv.chestplate)
        avatarInventory.setItem(5, inv.leggings)
        avatarInventory.setItem(6, inv.boots)
        avatarInventory.setItem(18, inv.itemInMainHand)
        avatarInventory.setItem(8, inv.itemInOffHand)
        avatarInventory.storageContents = inv.storageContents
    }

    fun copyTo(player: Player) {
        val playerInventory = player.inventory
        for (i in 27 .. 53) {
                val avatarItem = avatarInventory.getItem(i)
                if (avatarItem != null) {
                    for (j in 9..35) {
                        playerInventory.setItem(j, avatarItem)
                }
            }
        }
        for (i in 18 .. 26) {
            val avatarItem = avatarInventory.getItem(i)
            if (avatarItem != null) {
                for (j in 36 .. 44) {
                    playerInventory.setItem(j, avatarItem)
                }
            }
        }
        playerInventory.helmet = avatarInventory.getItem(3)
        playerInventory.chestplate = avatarInventory.getItem(4)
        playerInventory.leggings = avatarInventory.getItem(5)
        playerInventory.boots = avatarInventory.getItem(6)
        playerInventory.setItemInMainHand(avatarInventory.getItem(18))
        playerInventory.setItemInOffHand(avatarInventory.getItem(8))
        avatarLoaded = true
    }

        fun createAvatar(player: Player, deathLocation: Location) = createAvatarFromData(
            AvatarData(deathLocation, player.uniqueId, createAvatarInventory(player), player.name)
        )

        fun openInventory(player: Player, body: FakeEntity<Player>) =
            player.openInventory(linkedInventory[body.bukkitEntity.uniqueId]!!)
    }