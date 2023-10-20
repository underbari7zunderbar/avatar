/*package io.papermc.paperweight.testplugin

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
import java.io.File
import java.util.UUID
class Avatar {
    val foler: File,
    val uniqueid: UUID,
    val name: String
) : Inventoryholder {

  val file
    get() = File(folder, "$uniqueid.yml")

  private val avatarInventory = Bukkit.createInventory(this, 9 * 5, Component.text(name)).apply {
      for (i in 9..17) {
        setItem(i, lockedSlotItemStack)
      }
      setItem(2, lockedSlotItemStack)
      setItem(3, lockedSlotItemStack)
      setItem(6, lockedSlotItemStack)
  }

  private var avatarEntity: FakeEntity<Player>? = null
  private var slime: Slime? = null

  override fun getInventory(): Inventory {
      return avatarInventory
  }

  fun copyFrom(inv: PlayerInventory) {
      val armorContents = inv.armorContents
      val offHand = inv.itemInOffHand
      val contents = inv.contents

      armorSlots.forEachIndexed { from, to ->
          avatarInventory.setItem(to, armorContents[from])
      }
      avatarInventory.setItem(offHandSlot, offHand)
      inventorySlots.forEachIndexed { from, to ->
          avatarInventory.setItem(to, contents[from])
      }
      inv.contents.forEachIndexed { index, itemStack ->
        avatarInventory.setItem(index, itemStack)
      }
      inv.armorcontents.forEachIndexed { index, itemStack ->
        avatarInventory.setItem(index + 38, itemStack)
      }
      avatarInventory.setItem(42, inv.itemInOffHand)
      updateAvatarArmor
      }
  }
  fun copyTo(inv: PlayerInventory) {
      armorSlots.forEachIndexed { form, to ->
        inv.setItem(from, avatarInventory, getItem(to))
      }
      inv.setItemInOffHand(avatarInventory.getItem(offHandSlot))
      inventorySlots.forEachIndexed { from, to ->
          inv.setItem(from, avatarInventory.getItem(to)
      }
  }

  fun spawnAvatar(
      location: Location
      properties: Set<ProfileProperty> = Bukkit.getOfflinePlayer(uniqueid).playerProfile.properties
  ) {
      this.avatarEntity = avatarManager.plugin.fakeServer.spawnPlayer(location, name, properties).apply {
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
          persistentData[AvatarKeychain.UUID_MOST] = uniqueid.mostSignificantBits
          persistentData[AvatarKeychain.UUID_LEAST] = uniqueid.leastSignificantBits
      }
  }

  fun despawnAvatar() {
      avatarEntity?.remove()
      avatarEntity = null
  }

  fun updateAvatarArmor() {
      avatarEntity?.apply {
          updateEquipment {}
      }
  }
  )
    fun copyFrom(inv: PlayerInventory) {
      inv.content.forEachIndexed { index, itemStack ->
        avatarInventory.setItem(index, itemStack)
      }
      inv.armorContents.forEachIndexed { index, itemStack ->
        avatarInventory.setItem(index + 38, itemStack)
      }
      avatarInventory.setItem(42, inv.itemInOffHand)
      }

  fun spawnAvatar(location: Location, properties: Set<ProfileProperty> = Bukkit.getOfflinePlayer(uniqueid).playerProfile.properties) {
      this.avatarEntity = avatarManager.plugin.fakeServer.spawnPlayer(

        fun despawnAvatar() {
            avatarEntity?.remove()
            avatarEntity = null
        }
      )
    }}
}
*/
