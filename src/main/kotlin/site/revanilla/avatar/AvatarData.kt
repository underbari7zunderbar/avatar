package site.revanilla.avatar

import io.github.monun.tap.fake.FakeEntity
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import site.revanilla.avatar.AvatarManager.server
import java.util.*

@Suppress("UNUSED", "UNCHECKED_CAST")
data class AvatarData(
        val location: Location,
        val uniqueId: UUID,
        val inventory: Inventory,
        val name: String,
): ConfigurationSerializable {
    companion object {
        fun from(fakeEntity: FakeEntity<Player>, uuid: UUID): AvatarData {
            val location = fakeEntity.location
            return AvatarData(location, uuid, AvatarManager.linkedInventory[fakeEntity.bukkitEntity.uniqueId]!!, fakeEntity.bukkitEntity.name)
        }

        @JvmStatic
        fun deserialize(args: Map<String, Any>): AvatarData {
            val location = args["location"] as Location
            val uuid = UUID.fromString(args["uniqueId"] as String)
            val name = args["name"] as String

            val inventoryContents = server.createInventory(null, 54, text("ѐЀ", NamedTextColor.WHITE)).apply {
                contents = (args["inventory"] as List<ItemStack>).toTypedArray()
            }

            return AvatarData(location, uuid, inventoryContents, name)
        }
    }
    override fun serialize(): MutableMap<String, Any> {
        val out = mutableMapOf<String, Any>()
        out["location"] = location
        out["uniqueId"] = uniqueId.toString()
        out["inventory"] = inventory.contents.toList()
        out["name"] = name
        return out
    }
}