package me.blvckbytes.bblibconfig;

import com.mojang.authlib.GameProfile;
import me.blvckbytes.bblibconfig.component.IComponentApplicator;
import me.blvckbytes.bblibdi.AutoConstruct;
import me.blvckbytes.bblibdi.AutoInject;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 08/13/2022

  An ItemBuilder factory which just relays standard dependencies.

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
public class ItemBuilderFactory implements IItemBuilderFactory {

  private final IComponentApplicator componentApplicator;
  private final GradientGenerator gradientGenerator;

  public ItemBuilderFactory(
    @AutoInject IComponentApplicator componentApplicator,
    @AutoInject GradientGenerator gradientGenerator
  ) {
    this.componentApplicator = componentApplicator;
    this.gradientGenerator = gradientGenerator;
  }

  @Override
  public ItemBuilder create(Material mat, int amount) {
    return new ItemBuilder(new ItemStack(mat), amount, componentApplicator, gradientGenerator);
  }

  @Override
  public ItemBuilder create(GameProfile profile) {
    return new ItemBuilder(new ItemStack(Material.PLAYER_HEAD), 1, componentApplicator, gradientGenerator)
      .withHeadProfile(profile);
  }

  @Override
  public ItemBuilder create(ItemStack from, int amount) {
    return new ItemBuilder(from, amount, componentApplicator, gradientGenerator);
  }
}
