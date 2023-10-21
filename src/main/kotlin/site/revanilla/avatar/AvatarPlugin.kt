package site.revanilla.avatar

import io.github.monun.tap.fake.FakeEntity
import io.github.monun.tap.fake.FakeEntityServer
import net.minecraft.world.entity.Pose
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin


class AvatarPlugin : JavaPlugin(), Listener {

  lateinit var fes: FakeEntityServer
  private var isSpawned: Boolean = false
  private var avatarEntity: FakeEntity<Player>? = null
  override fun onEnable() {
    fes = FakeEntityServer.create(this).apply {
      Bukkit.getOnlinePlayers().forEach { addPlayer(it) }
    }

    server.scheduler.runTaskTimer(this, fes::update, 0L, 1L)

    server.pluginManager.registerEvents(this, this)
  }

  override fun onDisable() {
    fes.clear()
  }

  @EventHandler
  fun onPlayerJoin(event: PlayerJoinEvent) {
    val player = event.player

    fes.addPlayer(player)
    despawnAvatar()
    }
  fun despawnAvatar() {
    avatarEntity?.remove()
    avatarEntity = null
  }

  @EventHandler
  fun onPlayerQuit(event: PlayerQuitEvent) {
    val player = event.player


    fes.removePlayer(player)

    val fakeEntity: FakeEntity<Player> = fes.spawnPlayer(player.location, player.name, player.playerProfile.properties)
    fes.spawnPlayer(player.location, player.name, player.playerProfile.properties).apply {
      updateMetadata {
        (this as CraftPlayer).handle.pose = Pose.SLEEPING
        health = player.health
      }
    }

    fakeEntity.updateEquipment {
      helmet = player.inventory.helmet
      chestplate = player.inventory.chestplate
      leggings = player.inventory.leggings
      boots = player.inventory.boots
      setItemInMainHand(player.inventory.itemInMainHand)
      setItemInOffHand(player.inventory.itemInOffHand)
      isSpawned = true
    }

    /*val loc = player.location
    loc.world.spawn(loc, Slime::class.java).apply {
      isInvisible = true
      setAI(false)
      size = 1
      isInvulnerable = true
      isSilent = true
      isPersistent = true
      customName(text("avatar"))
      isCustomNameVisible = false*/

      fakeEntity.updateMetadata {
        (this as CraftPlayer).handle.pose = Pose.SLEEPING
      }
    }
  }
//}
