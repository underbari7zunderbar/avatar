package site.revanilla.avatar

import org.bukkit.configuration.serialization.ConfigurationSerialization.registerClass
import org.bukkit.event.HandlerList
import org.bukkit.plugin.java.JavaPlugin
import site.revanilla.avatar.AvatarManager.avatars
import site.revanilla.avatar.AvatarManager.fakeServer
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

    /*val configFile = File(AvatarManager.plugin.dataFolder, "config.yml")
    configFile.delete()
    saveDefaultConfig()
    reloadConfig()*/

    server.scheduler.runTaskTimer(this, fakeServer::update, 0L, 1L)

    AvatarManager.server.onlinePlayers.forEach { fakeServer.addPlayer(it) }
    AvatarManager.server.pluginManager.registerEvents(AvatarEvent, AvatarManager.plugin)

    avatars.forEach { AvatarManager.createAvatarFromData(it, true) }
    for (it in avatars) {
      AvatarEvent.avatarLoaded = true
    }
  }

  override fun onDisable() {
    fakeServer.clear()
    fakeServer.entities.forEach { it.remove() }
    server.onlinePlayers.forEach { fakeServer.removePlayer(it) }
    HandlerList.unregisterAll(AvatarEvent)

    config.set("avatars", avatars.toList())
    AvatarManager.plugin.saveConfig()
  }
}