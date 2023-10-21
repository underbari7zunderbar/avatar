package site.revanilla.avatar

import com.destroystokyo.paper.profile.ProfileProperty
import io.github.monun.tap.fake.FakeEntity
import net.minecraft.world.entity.Pose
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.entity.Slime
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.PlayerInventory
import net.kyori.adventure.text.Component
import java.util.UUID
import io.github.monun.tap.data.persistentData
import org.bukkit.Material
import java.io.File

class Avatar(
  val folder: UUID,
  val uniqueid: String
) : InventoryHolder {

  lateinit var avatarManager: AvatarManager
    internal set

  val file
      get() = File(avatarManager.folder, "$uniqueid.yml")

  private val avatarInventory = Bukkit.createInventory(this, 9 * 5, Component.text("인벤토리")).apply {
    //for (i in 9..17) {
      //setItem(i, Material.BARRIER)
    }
    //setItem(2, Material.BARRIER)
    //setItem(3, Material.BARRIER)
    //avatarInventory.setItem(6, Material.BARRIER)
  //}

  private var avatarEntity: FakeEntity<Player>? = null
  private var slime: Slime? = null

  override fun getInventory(): Inventory {
    return avatarInventory
  }

  fun copyFrom(inv: PlayerInventory) {
    val armorContents = inv.armorContents
    val offHand = inv.itemInOffHand
    val contents = inv.contents

    avatarInventory.contents.forEachIndexed { index, itemStack ->
      avatarInventory.setItem(index, itemStack)
    }
    armorContents.forEachIndexed { index, itemStack ->
      avatarInventory.setItem(index + 38, itemStack)
    }
    avatarInventory.setItem(42, inv.itemInOffHand)
    updateAvatarArmor()
  }

  /*fun copyTo(inv: PlayerInventory) {
    val armorContents = inv.armorContents
    armorContents.forEachIndexed { from, to ->
      avatarInventory.setItem(from, avatarInventory.getItem(to))
    }
    inv.setItemInOffHand(avatarInventory.getItem(45))
    inv.forEachIndexed { from, to ->
      avatarInventory.setItem(from, avatarInventory.getItem(to))
    }
  }*/

  fun spawnAvatar(
    location: Location,
    name: String,
    properties: Set<ProfileProperty> = Bukkit.getOfflinePlayer(uniqueid).playerProfile.properties
  ){
    this.avatarEntity = avatarManager.plugin.fes.spawnPlayer(location, name, properties).apply {
      updateMetadata {
        (this as CraftPlayer).handle.pose = Pose.SLEEPING
      }
    }
    updateAvatarArmor()

    location.world.spawn(location, Slime::class.java).apply {
      size = 1
      isPersistent = true
      isSilent = true
      // TODO: 체력구현
      //persistentData[AvatarKeychain.UUID_MOST] = uniqueid.mostSignificantBits
      //persistentData[AvatarKeychain.UUID_LEAST] = uniqueid.leastSignificantBits
    }
  }

  fun despawnAvatar() {
    avatarEntity?.remove()
    avatarEntity = null
  }

  fun updateAvatarArmor() {
    avatarEntity?.apply {
      updateEquipment {

      }
    }
  }
}
