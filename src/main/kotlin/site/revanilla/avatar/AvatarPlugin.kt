package site.revanilla.avatar

import io.github.monun.tap.data.persistentData
import io.github.monun.tap.fake.FakeEntity
import io.github.monun.tap.fake.FakeEntityServer
import net.minecraft.world.entity.Pose
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.entity.Slime
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.*

class AvatarPlugin : JavaPlugin(), Listener {

  private lateinit var avatarManager: AvatarManager
  lateinit var fes: FakeEntityServer
  private var avatarEntity: FakeEntity<Player>? = null

  override fun onEnable() {
    val folder = File(dataFolder, "avatars")
    if (!folder.exists()) {
      folder.mkdirs()
    }

    avatarManager = AvatarManager(this, folder)
    fes = FakeEntityServer.create(this).apply {
      Bukkit.getOnlinePlayers().forEach { addPlayer(it) }
    }

    server.scheduler.runTaskTimer(this, fes::update, 0L, 1L)

    server.pluginManager.registerEvents(this, this)
  }
  override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
    val player = sender as Player
    val loc = player.location
    loc.world.spawn(loc, Slime::class.java).apply {
      size = 1
      equipment.apply {
          helmet = ItemStack(Material.NETHERITE_HELMET)
          chestplate = ItemStack(Material.NETHERITE_CHESTPLATE)
          leggings = ItemStack(Material.NETHERITE_LEGGINGS)
          boots = ItemStack(Material.NETHERITE_BOOTS)
      }
    }
    return true
  }

  override fun onDisable() {
    fes.clear()
  }
  private fun despawnAvatar() {
    avatarEntity?.run {
      remove()
      avatarEntity = null
    }
  }

    @EventHandler
  fun onPlayerJoin(event: PlayerJoinEvent) {
    val player = event.player
    fes.addPlayer(player)
      avatarManager.avatarBy(player).apply {
        copyTo(player.inventory)
      despawnAvatar()
    }
    despawnAvatar()
  }

  @EventHandler
  fun onPlayerQuit(event: PlayerQuitEvent) {
    val player = event.player

    fes.removePlayer(player)

    avatarEntity = fes.spawnPlayer(player.location, player.name, player.playerProfile.properties).apply {
      updateMetadata {
        (this as CraftPlayer).handle.pose = Pose.SLEEPING
      }
    }

    avatarEntity!!.updateEquipment {
      helmet = player.inventory.helmet
      chestplate = player.inventory.chestplate
      leggings = player.inventory.leggings
      boots = player.inventory.boots
      setItemInMainHand(player.inventory.itemInMainHand)
      setItemInOffHand(player.inventory.itemInOffHand)
    }

    val loc = player.location
    loc.world.spawn(loc, Slime::class.java).apply {
      isInvisible = true
      setAI(false)
      size = 1
      isInvulnerable = true
      isSilent = true
      isPersistent = true
      @Suppress("DEPRECATION")
      customName = "아바타"
      isCustomNameVisible = false
    }
  }
  @EventHandler
  fun onPlayerInteractEntity(event: PlayerInteractEntityEvent) {
    val clicked = event.rightClicked
    if (event.hand == EquipmentSlot.HAND && clicked is Slime) {
      val most = clicked.persistentData[AvatarKeychain.UUID_MOST] ?: return
      val least = clicked.persistentData[AvatarKeychain.UUID_LEAST] ?: return

      val uuid = UUID(most, least)
      val avatar = avatarManager.avatarBy(uuid) ?: return

      //event.player.openInventory(avatar.inventory)
      Bukkit.getConsoleSender().sendMessage(uuid)
    }
  }
}
