/*package site.revanilla.avatar

import com.destroystokyo.paper.profile.ProfileProperty
import io.github.monun.tap.data.persistentData
import io.github.monun.tap.fake.FakeEntity
import net.kyori.adventure.text.Component
import net.minecraft.world.entity.Pose
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.entity.Slime
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import java.util.*

class Avatar(val folder: UUID, private val uniqueid: String) : InventoryHolder {

  lateinit var avatarManager: AvatarManager
    //internal set

  //val file
    //get() = File(folder, "$uniqueid.yml")

  private val avatarInventory = Bukkit.createInventory(this, 9 * 5, Component.text("인벤토리")).apply {
    for (i in 9..17) {
      setItem(i, ItemStack(Material.BARRIER))
    }
    setItem(2, ItemStack(Material.BARRIER))
    setItem(3, ItemStack(Material.BARRIER))
    setItem(6, ItemStack(Material.BARRIER))
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

    avatarInventory.contents.forEachIndexed { index, itemStack ->
      avatarInventory.setItem(index, itemStack)
    }
    armorContents.forEachIndexed { index, itemStack ->
      avatarInventory.setItem(index + 38, itemStack)
    }
    avatarInventory.setItem(42, offHand)
    updateAvatarArmor()
  }

  fun copyTo(inv: PlayerInventory) {
    val armorContents = inv.armorContents
    armorContents.forEachIndexed { from, to ->
      inv.setItem(from, avatarInventory.getItem(1))
    }
    //inv.setItemInOffHand(avatarInventory.getItem(45))
    inv.forEachIndexed { from, to ->
      inv.setItem(from, avatarInventory.getItem(1))
    }
  }

  fun spawnAvatar(location: Location, name: String, properties: Set<ProfileProperty> = Bukkit.getOfflinePlayer(uniqueid).playerProfile.properties) {
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
      persistentData[AvatarKeychain.UUID_MOST] = UUID.fromString(uniqueid).mostSignificantBits
      persistentData[AvatarKeychain.UUID_LEAST] = UUID.fromString(uniqueid).leastSignificantBits
    }
  }

  private fun updateAvatarArmor() {
    avatarEntity?.apply {
      updateEquipment {
        setHelmet(avatarInventory.getItem(5)?.clone(), true)
        setChestplate(avatarInventory.getItem(6)?.clone(), true)
        setLeggings(avatarInventory.getItem(7)?.clone(), true)
        setBoots(avatarInventory.getItem(8)?.clone(), true)
        setItemInMainHand(avatarInventory.getItem(29 + 18)?.clone(), true)
        setItemInOffHand(avatarInventory.getItem(45)?.clone(), true)
      }
    }
  }

  fun refreshSessions() {
      session = System.currentTimeMillis()
      avatarEntity?.updateSession(session)

      changed = true
  }

  fun updateState(player: Player) {
      name = player.name
      location = player.location
      handSlot = player.inventory.heldItemSlot
      maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: 20.0
      health = player.health

      changed = true
  }

  fun onClick(event: InventoryClickEvent) {
    val slot = event.rawSlot

    if (slot == 0) {
      event.isCancelled = true
      return
    }

    if (slot < 54) {
      val item = event.currentItem

      if (item?.type == Material.BARRIER) {
        event.isCancelled = true
        return
      }
    }
  }

  fun onDrag(event: InventoryDragEvent) {
    val cursor = event.oldCursor
    val slots = event.rawSlots

    if (slots.count() == 1) {
      val slot = slots.first()
      armorSlots.fin { it.slot == slot }?.run {
        if (!test(cursor.type)) {
          event.isCancelled = true
        }
      }
    } else if (slots.any { it < 7 }) {
      event.isCancelled = true
    }
  }
}*/
