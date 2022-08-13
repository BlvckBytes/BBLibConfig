package me.blvckbytes.bblibconfig;

import me.blvckbytes.bblibconfig.sections.ItemStackSection;
import me.blvckbytes.bblibreflect.ICustomizableViewer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 08/12/2022

  Represents an ItemStack which can be built for a customizable viewer.

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
public interface IItemBuildable {

  /**
   * Build the item for a specific viewer
   * @param variables Variables to apply
   * @param viewer Viewer viewing this customizable
   * @return Item built for the viewer
   */
  default ItemStack build(@Nullable Map<String, String> variables, @Nullable ICustomizableViewer viewer) {
    throw new UnsupportedOperationException("This buildable does not support templating");
  }

  /**
   * Gets the static representation of this item
   */
  ItemStack build();

  /**
   * Create a new mutable carbon copy
   */
  IItemBuildable copy();

  /**
   * Patch a clone of this item with the provided properties of an
   * item section and return that copy
   * @param data Values to apply
   * @return Patched copy
   */
  IItemBuildable patch(ItemStackSection data);

}
