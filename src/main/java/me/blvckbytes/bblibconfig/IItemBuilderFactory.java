package me.blvckbytes.bblibconfig;

import com.mojang.authlib.GameProfile;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 08/13/2022

  A factory which internally creates the ItemBuilder and injects
  additional dependencies.

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
public interface IItemBuilderFactory {

  /**
   * Create a new builder for a specific material
   * @param mat Material to target
   * @param amount Amount of items
   */
  ItemBuilder create(Material mat, int amount);

  /**
   * Create a new builder for a player-head
   * @param profile Profile to apply to the head
   */
  ItemBuilder create(GameProfile profile);

  /**
   * Create a new builder based on an existing item stack
   * @param from Existing item stack to mimic
   * @param amount Amount of items
   */
  ItemBuilder create(ItemStack from, int amount);
}
