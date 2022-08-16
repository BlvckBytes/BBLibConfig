package me.blvckbytes.bblibconfig.component;

import com.google.gson.JsonElement;
import me.blvckbytes.bblibdi.AutoConstruct;
import me.blvckbytes.bblibdi.AutoInject;
import me.blvckbytes.bblibreflect.IReflectionHelper;
import me.blvckbytes.bblibreflect.RClass;
import me.blvckbytes.bblibreflect.handle.Assignability;
import me.blvckbytes.bblibreflect.handle.ClassHandle;
import me.blvckbytes.bblibreflect.handle.FieldHandle;
import me.blvckbytes.bblibreflect.handle.MethodHandle;
import me.blvckbytes.bblibutil.component.IComponent;
import me.blvckbytes.bblibutil.logger.ILogger;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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

  private final FieldHandle F_CRAFT_META_ITEM__NAME_BASE_COMPONENT, F_CRAFT_META_ITEM__NAME_STRING,
    F_CRAFT_META_ITEM__LORE_STRING_LIST;

  private final MethodHandle M_CHAT_SERIALIZER__FROM_JSON;

  private final ILogger logger;

  public ComponentApplicator(
    @AutoInject IReflectionHelper reflection,
    @AutoInject ILogger logger
  ) throws Exception {
    this.logger = logger;

    ClassHandle C_CHAT_SERIALIZER  = reflection.getClass(RClass.CHAT_SERIALIZER);
    ClassHandle C_CRAFT_META_ITEM  = reflection.getClass(RClass.CRAFT_META_ITEM);
    ClassHandle C_BASE_COMPONENT   = reflection.getClass(RClass.I_CHAT_BASE_COMPONENT);

    M_CHAT_SERIALIZER__FROM_JSON = C_CHAT_SERIALIZER.locateMethod().withParameters(JsonElement.class).withReturnType(C_BASE_COMPONENT, false, Assignability.TYPE_TO_TARGET).withStatic(true).required();

    F_CRAFT_META_ITEM__NAME_BASE_COMPONENT = C_CRAFT_META_ITEM.locateField().withType(C_BASE_COMPONENT).optional();
    F_CRAFT_META_ITEM__NAME_STRING         = C_CRAFT_META_ITEM.locateField().withType(String.class).optional();

    if (F_CRAFT_META_ITEM__NAME_BASE_COMPONENT == null && F_CRAFT_META_ITEM__NAME_STRING == null)
      throw new IllegalStateException("Couldn't find neither base component nor string lore field");

    F_CRAFT_META_ITEM__LORE_STRING_LIST = C_CRAFT_META_ITEM.locateField().withType(List.class).withGeneric(String.class).required();
  }

  @Override
  public void setDisplayName(IComponent displayName, boolean approximateColors, ItemStack item) {
    ItemMeta meta = item.getItemMeta();

    // Meta unavailable
    if (meta == null)
      return;

    try {
      if (F_CRAFT_META_ITEM__NAME_STRING != null)
        F_CRAFT_META_ITEM__NAME_STRING.set(meta, displayName.toJson(approximateColors).toString());

      if (F_CRAFT_META_ITEM__NAME_BASE_COMPONENT != null)
        F_CRAFT_META_ITEM__NAME_BASE_COMPONENT.set(meta, M_CHAT_SERIALIZER__FROM_JSON.invoke(null, displayName.toJson(approximateColors)));

      item.setItemMeta(meta);
    } catch (Exception e) {
      logger.logError(e);
    }
  }

  @Override
  public void setLore(List<? extends IComponent> lines, boolean approximateColors, ItemStack item) {
    ItemMeta meta = item.getItemMeta();

    // Meta unavailable
    if (meta == null)
      return;

    try {
      // Jsonify all lore lines
      List<String> lore = lines.stream()
        .map(c -> c.toJson(approximateColors).toString())
        .collect(Collectors.toList());

      // Overwrite the list ref
      F_CRAFT_META_ITEM__LORE_STRING_LIST.set(meta, lore);

      item.setItemMeta(meta);
    } catch (Exception e) {
      logger.logError(e);
    }
  }
}
