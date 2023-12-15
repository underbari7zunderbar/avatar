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

    var npc: FakeEntity<Player>? = null
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
    val avatarInventory = server.createInventory(null, 54, text("Ѐ", NamedTextColor.DARK_GRAY))

    private fun createAvatarInventory(player: Player): Inventory {

        val barrier = Material.BARRIER
        val item = ItemStack(barrier)
        val m = item.itemMeta
        m.setDisplayName("")
        m.lore?.clear()
        item.itemMeta = m
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

    fun copyFrom(player: Player) {
        val inv = player.inventory

        avatarInventory.setItem(3, inv.helmet?.clone())
        avatarInventory.setItem(4, inv.chestplate?.clone())
        avatarInventory.setItem(5, inv.leggings?.clone())
        avatarInventory.setItem(6, inv.boots?.clone())
        avatarInventory.setItem(18, inv.itemInMainHand.clone())
        avatarInventory.setItem(8, inv.itemInOffHand.clone())
        avatarInventory.storageContents = inv.storageContents.clone()
    }
    fun updateInv(player: Player) {
        val playerInventory = player.inventory

        playerInventory.helmet = avatarInventory.getItem(3)?.clone()
        playerInventory.chestplate = avatarInventory.getItem(4)
        playerInventory.leggings = avatarInventory.getItem(5)?.clone()
        playerInventory.boots = avatarInventory.getItem(6)?.clone()
        playerInventory.setItemInMainHand(avatarInventory.getItem(18)?.clone())
        playerInventory.setItemInOffHand(avatarInventory.getItem(8)?.clone())

        val avatarStorageContents = avatarInventory.contents.copyOfRange(18, avatarInventory.size)
        playerInventory.storageContents = avatarStorageContents

    }

        fun createAvatar(player: Player, deathLocation: Location) = createAvatarFromData(
            AvatarData(deathLocation, player.uniqueId, createAvatarInventory(player), player.name)
        )

        fun openInventory(player: Player, body: FakeEntity<Player>) =
            player.openInventory(linkedInventory[body.bukkitEntity.uniqueId]!!)
    }