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
        npc?.let { avatar ->
            val corpseData = corpses.find { it.uniqueId == player.uniqueId }
            corpseData?.let {
                copyTo(it, player.inventory)
            }
        }
    }

    fun updateAvatarInventory(player: Player) {
        npc?.let { avatar ->
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

        private fun createAvatarInventory(player: Player) =
            server.createInventory(null, 45, text(player.name, NamedTextColor.DARK_GRAY)).apply {
                contents = Array(45) { if (it < 41) player.inventory.contents[it] else ItemStack(Material.AIR) }
            }

        fun createCorpseNPC(player: Player, deathLocation: Location) = createAvatarFromData(
            AvatarData(deathLocation, player.uniqueId, createAvatarInventory(player), player.name)
        )

        fun openInventory(player: Player, body: FakeEntity<Player>) =
            player.openInventory(linkedInventory[body.bukkitEntity.uniqueId]!!)
    }