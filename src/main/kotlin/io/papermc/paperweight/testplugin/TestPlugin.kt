package io.papermc.paperweight.testplugin

import io.github.monun.tap.fake.FakeEntityServer
import net.minecraft.world.entity.Pose
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer
import org.bukkit.entity.Slime
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.*


class TestPlugin : JavaPlugin(), Listener {

  private lateinit var fes: FakeEntityServer
    override fun onEnable() {
      fes = FakeEntityServer.create(this).apply {
      Bukkit.getOnlinePlayers().forEach { addPlayer(it) }
    }

      server.scheduler.runTaskTimer(this, fes::update, 0L, 1L)

      server.pluginManager.registerEvents(this, this)
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        fes.addPlayer(event.player)
    }


    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player

        fes.removePlayer(player)

        fes.spawnPlayer(player.location, player.name, player.playerProfile.properties).apply {
            updateMetadata {
                (this as CraftPlayer).handle.pose = Pose.SLEEPING
            }
        }
    }

  /*class EventListener(private val plugin: TestPlugin) : Listener {
      @EventHandler
      fun onPlayerJoin(event: PlayerJoinEvent) {
          val player = event.player
          plugin.fes.addPlayer(player)

          plugin.avatarManager.avatarby(player).apply {
              copyTo(player.inventory)
              despawnAvatar()
          }
      }

  @EventHandler
  fun onPlayerQuit(event: PlayerQuitEvent) {
    val player = event.player

    fes.removePlayer(player)

    fes.spawnPlayer(player.location, player.name, player.playerProfile.properties).apply {
      updateMetadata {
        (this as CraftPlayer).handle.pose = Pose.SLEEPING
      }
    }


    val loc = player.location
    loc.world.spawn(loc, Slime::class.java).apply {
      isInvisible = true
      setAI(false)
      size = 1
      isInvulnerable = true
      isSilent = true
      isPersistent = true
    }
  }

  @EventHandler
  fun onPlayerInteractEntity(event: PlayerInteractEntityEvent) {
      val clicked = event.rightClicked
      if (clicked is Slime) {
          val most = clicked.persistentData[AvatarKeychain.UUID_MOST] ?: return
          val least = clicked.persistentData[AvatarKeychain.UUID_LEAST] ?: return
          val uuid = UUID(most, least)
          val avatar = plugin.avatarManager.avatarBy(uuid) ?: return

          event.player.openInventory(avatar.inventory)
      }

  }  }*/
}
