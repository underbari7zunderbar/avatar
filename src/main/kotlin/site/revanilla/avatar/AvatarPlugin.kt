package site.revanilla.avatar

import org.bukkit.configuration.serialization.ConfigurationSerialization.registerClass
import org.bukkit.event.HandlerList
import org.bukkit.plugin.java.JavaPlugin
import site.revanilla.avatar.AvatarManager.corpses
import site.revanilla.avatar.AvatarManager.fakeServer
import site.revanilla.avatar.AvatarManager.taskId
import site.revanilla.avatar.events.AvatarEvent

//import site.revanilla.avatar.events.AvatarEvent.cancelAvatarUpdater

@Suppress("UNCHECKED_CAST")
class AvatarPlugin : JavaPlugin() {
  companion object {
    lateinit var instance: AvatarPlugin
      private set
  }

  override fun onEnable() {
    instance = this

    registerClass(AvatarData::class.java)
    corpses.addAll(config.getList("avatars", listOf<AvatarData>()) as List<AvatarData>)
    server.scheduler.runTaskTimer(this, fakeServer::update, 0L, 1L)

    AvatarManager.server.onlinePlayers.forEach { fakeServer.addPlayer(it) }
    AvatarManager.server.pluginManager.registerEvents(AvatarEvent, AvatarManager.plugin)
    AvatarManager.server.scheduler.runTaskTimer(AvatarManager.plugin, Runnable { fakeServer.update() }, 0L, 0L).also { taskId = it.taskId }

    corpses.forEach { AvatarManager.createAvatarFromData(it, true) }
  }

  override fun onDisable() {
    fakeServer.clear()
    fakeServer.entities.forEach { it.remove() }
    server.onlinePlayers.forEach { fakeServer.removePlayer(it) }
    server.scheduler.cancelTask(taskId)
    HandlerList.unregisterAll(AvatarEvent)

    config.set("corpses", corpses.toList())
    AvatarManager.plugin.saveConfig()
  }
}