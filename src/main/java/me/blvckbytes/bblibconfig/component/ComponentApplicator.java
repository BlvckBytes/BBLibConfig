package me.blvckbytes.bblibconfig.component;

import me.blvckbytes.bblibdi.AutoConstruct;
import me.blvckbytes.bblibdi.AutoInject;
import me.blvckbytes.bblibreflect.MCReflect;
import me.blvckbytes.bblibreflect.ReflClass;
import me.blvckbytes.bblibutil.logger.ILogger;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 08/12/2022

  Reflection based implementation of the component applicator.

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as published
  by the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
@AutoConstruct
public class ComponentApplicator implements IComponentApplicator {

  // TODO: Just set the plain value on < 1.16

  private final Class<?> CRAFT_META_ITEM_C, I_CHAT_BASE_COMPONENT_C;
  private final MCReflect reflect;
  private final ILogger logger;

  public ComponentApplicator(
    @AutoInject MCReflect reflect,
    @AutoInject ILogger logger
  ) throws Exception {
    this.reflect = reflect;
    this.logger = logger;

    CRAFT_META_ITEM_C = reflect.getClassBKT("inventory.CraftMetaItem");
    I_CHAT_BASE_COMPONENT_C = reflect.getReflClass(ReflClass.I_CHAT_BASE_COMPONENT);
  }

  @Override
  public void setDisplayName(IComponent displayName, ItemStack item) {
    ItemMeta meta = item.getItemMeta();

    // Meta unavailable
    if (meta == null)
      return;

    try {
      boolean isJson;
      Field f;

      // Figure out whether there's still a IChatBaseComponent, or already a JSON String
      try {
        f = reflect.findFieldByType(CRAFT_META_ITEM_C, String.class, 0);
        isJson = true;
      } catch (Exception e) {
        f = reflect.findFieldByType(CRAFT_META_ITEM_C, I_CHAT_BASE_COMPONENT_C, 0);
        isJson = false;
      }

      // Overwrite the displayname string or base component
      f.set(
        meta,
        isJson ?
          displayName.toJson().toString() :
          reflect.chatComponentFromJson(displayName.toJson())
      );

      item.setItemMeta(meta);
    } catch (Exception e) {
      logger.logError(e);
    }
  }

  @Override
  public void setLore(List<? extends IComponent> lines, ItemStack item) {
    ItemMeta meta = item.getItemMeta();

    // Meta unavailable
    if (meta == null)
      return;

    try {
      // Jsonify all lore lines
      List<String> lore = lines.stream()
        .map(c -> c.toJson().toString())
        .collect(Collectors.toList());

      // Overwrite the list ref
      reflect.findGenericFieldByType(CRAFT_META_ITEM_C, List.class, String.class, 0).set(meta, lore);

      item.setItemMeta(meta);
    } catch (Exception e) {
      logger.logError(e);
    }
  }
}
