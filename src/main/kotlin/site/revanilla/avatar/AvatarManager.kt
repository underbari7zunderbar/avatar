package site.revanilla.avatar

import org.bukkit.entity.Player
import java.io.File
import java.util.*

class AvatarManager(val plugin: AvatarPlugin, val folder: File) {

  private val avatars = mutableMapOf<UUID, Avatar>()

  /*fun load() {
      if (!folder.exists()) folder.mkdirs()

      folder.listFiles()?.forEach { file ->
          if (file.isFile && file.extension == "yml") {
              val avatar = Avatar.load(file).apply {
                avatarManager = this@AvatarManager

              }
              avatars[avatar.uniqueid] = avatar
              }
          }

          avatars.forEach { (t, u) ->
              val offlinePlayer = Bukkit.getOfflinePlayer(u.uniqueid)
              if (offlinePlayer.isOnline) return@forEach
      }
  }*/

  fun avatarBy(player: Player): Avatar {
      val uniqueid = player.uniqueId
      return avatars.computeIfAbsent(uniqueid) {
          Avatar(uniqueid, player.name).apply {
              avatarManager = this@AvatarManager
          }
      }
  }

fun avatarBy(uuid: UUID): Avatar? {
      return avatars[uuid]
  }
}
