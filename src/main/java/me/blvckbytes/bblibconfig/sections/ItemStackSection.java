package me.blvckbytes.bblibconfig.sections;

import com.cryptomorin.xseries.XMaterial;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import lombok.Getter;
import me.blvckbytes.bblibconfig.AConfigSection;
import me.blvckbytes.bblibconfig.ConfigValue;
import me.blvckbytes.bblibconfig.IItemBuilderFactory;
import me.blvckbytes.bblibconfig.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.function.Function;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 06/28/2022

  Represents the properties of a fully describeable item stack.

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
@Getter
public class ItemStackSection extends AConfigSection {

  private @Nullable ConfigValue amount;
  private @Nullable ConfigValue type;
  private @Nullable ConfigValue name;
  private @Nullable ConfigValue lore;
  private boolean loreOverride;
  private @Nullable ConfigValue flags;
  private boolean flagsOverride;
  private @Nullable ConfigValue color;
  private ItemStackEnchantmentSection[] enchantments;
  private boolean enchantmentsOverride;
  private @Nullable ConfigValue textures;
  private @Nullable ItemStackBaseEffectSection baseEffect;
  private ItemStackCustomEffectSection[] customEffects;
  private boolean customEffectsOverride;
  private ItemStackBannerPatternSection[] bannerPatterns;
  private boolean bannerPatternsOverride;

  public ItemStackSection() {
    this.enchantments = new ItemStackEnchantmentSection[0];
    this.customEffects = new ItemStackCustomEffectSection[0];
    this.bannerPatterns = new ItemStackBannerPatternSection[0];
  }

  /**
   * Create an item stack builder from the parameters of this section
   */
  public ItemBuilder asItem(IItemBuilderFactory builderFactory) {
    return builderFactory.create(new ItemStack(Material.BARRIER), 1)
      .setConfigType(type)
      .setConfigAmount(amount)
      .withConfigPatterns(bannerPatterns)
      .withName(name)
      .withLore(lore)
      .withConfigFlags(flags)
      .withConfigEnchantments(enchantments)
      .withConfigColor(color)
      .withConfigTextures(textures)
      .withConfigBaseEffect(baseEffect)
      .withConfigCustomEffects(customEffects);
  }

  /**
   * Compares all available values of this section against the
   * provided item and checks if they match
   * @param item Target item
   */
  public boolean describesItem(@Nullable ItemStack item) {
    if (item == null)
      return false;

    XMaterial m = getType() == null ? null : getType().asScalar(XMaterial.class);

    if (m != null && !m.isSimilar(item))
      return false;

    if (amount != null) {
      Integer am = amount.asScalar(Integer.class);
      if (am != null && am != item.getAmount())
        return false;
    }

    // Compare displayname
    if (!checkMeta(item, name, meta -> name.asScalar().equals(meta.getDisplayName())))
      return false;

    // Compare lore lines for equality (and order)
    if (!checkMeta(item, lore, meta -> lore.asList().equals(meta.getLore())))
      return false;

    // Compare flag entries for equality (ignoring order)
    if (!checkMeta(item, flags, meta -> flags.asSet(ItemFlag.class).equals(meta.getItemFlags())))
      return false;

    // Compare either potion color or leather color
    if (!checkMeta(item, color, meta -> {
      if (meta instanceof PotionMeta)
        return color.equals(((PotionMeta) meta).getColor());

      if (meta instanceof LeatherArmorMeta)
        return color.equals(((LeatherArmorMeta) meta).getColor());

      // Not colorable
      return false;
    }))
      return false;

    // Check for the presence of all enchantments at the right levels (ignoring order)
    if (!checkMeta(item, enchantments, meta -> {
      for (ItemStackEnchantmentSection ench : enchantments) {
        Enchantment e = ench.getEnchantment() == null ? null : ench.getEnchantment().asScalar(Enchantment.class);

        // Cannot compare
        if (e == null)
          continue;

        Integer level = ench.getLevel() == null ? null : ench.getLevel().asScalar(Integer.class);

        if (!(
          // Contains this enchantment at any levej
          meta.hasEnchant(e) &&
          // Contains at a matching level, if required
          (level == null || meta.getEnchantLevel(e) == level)
        ))
          return false;
      }
      // All enchantments matched
      return true;
    }))
      return false;

    // Compare for head textures
    if (!checkMeta(item, textures, meta -> {
      // Not a skull
      if (!(meta instanceof SkullMeta))
        return false;

      SkullMeta sm = (SkullMeta) meta;
      OfflinePlayer owner = sm.getOwningPlayer();

      // Has no head owner
      if (owner == null)
        return false;

      try {
        Field profileField = meta.getClass().getDeclaredField("profile");
        profileField.setAccessible(true);
        GameProfile profile = (GameProfile) profileField.get(sm);
        PropertyMap pm = profile.getProperties();
        Collection<Property> targets = pm.get("textures");

        // Does not contain any textures
        if (targets.size() == 0)
          return false;

        String texturesValue = textures.asScalar();
        return targets.stream().anyMatch(prop -> prop.getValue().equals(texturesValue));
      } catch (Exception ignored) {}

      return false;
    }))
      return false;

    // Compare the base potion effect
    if (!checkMeta(item, baseEffect, meta -> {
      // Not a potion
      if (!(meta instanceof PotionMeta))
        return false;
      return baseEffect.describesData(((PotionMeta) meta).getBasePotionData());
    }))
      return false;

    // Check for the presence of all custom effects (ignoring order)
    if (!checkMeta(item, customEffects, meta -> {
      // Nothing to compare
      if (customEffects.length == 0)
        return true;

      if (!(meta instanceof PotionMeta))
        return false;

      for (ItemStackCustomEffectSection eff : customEffects) {
        // Current custom effect is not represented within the custom effects of the potion
        if (((PotionMeta) meta).getCustomEffects().stream().anyMatch(eff::describesEffect))
          return false;
      }

      // All effects present
      return true;
    }))
      return false;

    // TODO: Compare banner patterns

    // All checks passed
    return true;
  }

  /**
   * Check a local parameter against the item's metadata. Whenever the
   * local value is null, this function returns true, and if it's present
   * but there's no metadata or the metadata property mismatches, it returns false
   * @param item Item to check the meta of
   * @param value Local parameter to use
   * @param checker Checker function
   */
  private boolean checkMeta(ItemStack item, @Nullable Object value, Function<ItemMeta, Boolean> checker) {
    // Value not present, basically a wildcard
    if (value == null)
      return true;

    // Fails if there is either no meta to compare against at all or if the checker failed
    return item.getItemMeta() != null && checker.apply(item.getItemMeta());
  }
}
