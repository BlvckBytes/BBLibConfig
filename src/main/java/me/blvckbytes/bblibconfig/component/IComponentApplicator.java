package me.blvckbytes.bblibconfig.component;

import me.blvckbytes.bblibreflect.ICustomizableViewer;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 08/13/2022

  The public API which the component applicator offers to other consumers
  in order to apply the IComponent to as many destinations as possible.

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
public interface IComponentApplicator {

  /**
   * Sets a component as the display name of an item
   * @param displayName Displayname to set
   * @param approximateColors Whether to approximate HEX colors as ChatColors
   * @param item Item to apply on
   */
  void setDisplayName(IComponent displayName, boolean approximateColors, ItemStack item);

  /**
   * Sets a list of components as the lore of an item
   * @param lines Lore to set
   * @param approximateColors Whether to approximate HEX colors as ChatColors
   * @param item Item to apply on
   */
  void setLore(List<? extends IComponent> lines, boolean approximateColors, ItemStack item);

  /**
   * Send out a chat message
   * @param message Message to send
   * @param viewer Viewer of the message
   */
  void sendChat(IComponent message, ICustomizableViewer viewer);

  /**
   * Set the action bar text
   * @param text Text to set
   * @param viewer Viewer of the text
   */
  void sendActionBar(IComponent text, ICustomizableViewer viewer);

  /**
   * Send out a title message
   * @param title Title to display
   * @param subtitle Subtitle to display
   * @param fadeIn Fade in duration in ticks
   * @param duration Stay duration in ticks
   * @param fadeOut Fade out duration in ticks
   * @param viewer Viewer of the title
   */
  void sendTitle(
    IComponent title, IComponent subtitle,
    int fadeIn, int duration, int fadeOut,
    ICustomizableViewer viewer
  );

}
