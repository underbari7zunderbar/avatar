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

    fun copyToP(player: Player) {
        val inv = player.inventory
        inv.setItem(0, ItemStack(Material.WOODEN_SWORD))
        inv.setItem(1, ItemStack(Material.WOODEN_SWORD))
        inv.setItem(2, ItemStack(Material.WOODEN_SWORD))
        inv.setItem(3, ItemStack(Material.WOODEN_SWORD))
        inv.setItem(4, ItemStack(Material.WOODEN_SWORD))
        inv.setItem(5, ItemStack(Material.WOODEN_SWORD))
        inv.setItem(6, ItemStack(Material.WOODEN_SWORD))
        inv.setItem(7, ItemStack(Material.WOODEN_SWORD))
        inv.setItem(8, ItemStack(Material.WOODEN_SWORD))
    }

    fun copyHotBar(player: Player) {
        val i = player.inventory
        i.setItem(0, avatarInventory.getItem(18))
        i.setItem(1, avatarInventory.getItem(19))
        i.setItem(2, avatarInventory.getItem(20))
        i.setItem(3, avatarInventory.getItem(21))
        i.setItem(4, avatarInventory.getItem(22))
        i.setItem(5, avatarInventory.getItem(23))
        i.setItem(6, avatarInventory.getItem(24))
        i.setItem(7, avatarInventory.getItem(25))
        i.setItem(8, avatarInventory.getItem(26))
        println("copyHotBar")
    }

    fun copyArmor(player: Player){
        val plinv = player.inventory
        val helmet = avatarInventory.getItem(3)
        val chestplate = avatarInventory.getItem(4)
        val leggings = avatarInventory.getItem(5)
        val boots = avatarInventory.getItem(6)
        val offhand = avatarInventory.getItem(8)
        if (helmet != null) {
            plinv.helmet = helmet
        }
        if (chestplate != null) {
            plinv.chestplate = chestplate
        }
        if (leggings != null) {
            plinv.leggings = leggings
        }
        if (boots != null) {
            plinv.boots = boots
        }
        if (offhand != null) {
            plinv.setItemInOffHand(offhand)
        }
        println("copyArmor")
    }
    fun copyTo(player: Player) {
        val playerInventory = player.inventory
        for (i in 53 downTo 27) {
            val avatarItem = avatarInventory.getItem(i)
            if (avatarItem != null) {
                playerInventory.setItem(i - 18, avatarItem)
            }
        }
        avatarLoaded = true
        println("copyTo")
    }



        fun createAvatar(player: Player, deathLocation: Location) = createAvatarFromData(
            AvatarData(deathLocation, player.uniqueId, createAvatarInventory(player), player.name)
        )

        fun openInventory(player: Player, body: FakeEntity<Player>) =
            player.openInventory(linkedInventory[body.bukkitEntity.uniqueId]!!)
    }