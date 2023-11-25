package site.revanilla.avatar

import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import site.revanilla.avatar.AvatarManager.fakeServer
import site.revanilla.avatar.events.AvatarEvent
import site.revanilla.avatar.events.AvatarEvent.cancelAvatarUpdater
import site.revanilla.avatar.events.AvatarEvent.startAvatarUpdater

class AvatarPlugin : JavaPlugin(), Listener {
  companion object {
    lateinit var instance: AvatarPlugin
      private set
  }

  override fun onEnable() {
    instance = this
    server.scheduler.runTaskTimer(this, fakeServer::update, 0L, 1L)

    server.pluginManager.registerEvents(this, this)
    AvatarManager.server.onlinePlayers.forEach { fakeServer.addPlayer(it) }
    AvatarManager.server.pluginManager.registerEvents(AvatarEvent, AvatarManager.plugin)
    AvatarManager.server.scheduler.runTaskTimer(AvatarManager.plugin, Runnable { fakeServer.update() }, 0L, 0L).also { AvatarManager.taskId = it.taskId }

    AvatarManager.corpses.forEach { AvatarManager.createAvatarFromData(it, true) }
    startAvatarUpdater()
  }

  override fun onDisable() {
    fakeServer.clear()
    AvatarManager.taskId = 0
    fakeServer.entities.forEach { it.remove() }
    AvatarManager.server.onlinePlayers.forEach { fakeServer.removePlayer(it) }
    AvatarManager.server.scheduler.cancelTask(AvatarManager.taskId)
    HandlerList.unregisterAll(AvatarEvent)

    AvatarManager.corpses.clear()

    AvatarManager.plugin.config.set("corpses", null)
    AvatarManager.plugin.saveConfig()
    cancelAvatarUpdater()
  }
}