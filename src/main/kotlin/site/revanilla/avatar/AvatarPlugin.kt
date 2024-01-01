package site.revanilla.avatar

import org.bukkit.configuration.serialization.ConfigurationSerialization.registerClass
import org.bukkit.event.HandlerList
import org.bukkit.plugin.java.JavaPlugin
import site.revanilla.avatar.AvatarManager.avatars
import site.revanilla.avatar.AvatarManager.copyTo
import site.revanilla.avatar.AvatarManager.fakeServer
import site.revanilla.avatar.AvatarManager.linkedInventories
import site.revanilla.avatar.AvatarManager.plugin
import site.revanilla.avatar.AvatarManager.taskId
import site.revanilla.avatar.events.AvatarEvent

@Suppress("UNCHECKED_CAST")
class AvatarPlugin : JavaPlugin() {
  companion object {
    lateinit var instance: AvatarPlugin
      private set
  }

  override fun onEnable() {
    instance = this

    registerClass(AvatarData::class.java)
    avatars.addAll(config.getList("avatars", listOf<AvatarData>()) as List<AvatarData>)

    server.scheduler.runTaskTimer(plugin, Runnable { fakeServer.update() }, 0L, 0L).also { taskId = it.taskId }

    server.onlinePlayers.forEach { fakeServer.addPlayer(it) }
    server.pluginManager.registerEvents(AvatarEvent, plugin)

    avatars.forEach { AvatarManager.createAvatarFromData(it, true) }
    for (it in server.onlinePlayers) {
      val avatarInventory = linkedInventories[it.uniqueId] ?: return
      avatarInventory.close()
    }
    //config.set("avatars", null)
    //plugin.saveConfig()
  }

  override fun onDisable() {
    server.onlinePlayers.forEach { copyTo(it) }
    taskId = 0
    server.scheduler.cancelTask(taskId)
    fakeServer.clear()
    fakeServer.entities.forEach { it.remove() }
    server.onlinePlayers.forEach { fakeServer.removePlayer(it) }
    HandlerList.unregisterAll(AvatarEvent)

    //config.set("avatars", avatars.toList())
    //plugin.saveConfig()
  }
}