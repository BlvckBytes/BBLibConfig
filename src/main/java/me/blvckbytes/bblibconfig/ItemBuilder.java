package me.blvckbytes.bblibconfig;

import com.cryptomorin.xseries.XMaterial;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import me.blvckbytes.bblibconfig.component.IComponentApplicator;
import me.blvckbytes.bblibconfig.component.TextComponent;
import me.blvckbytes.bblibconfig.sections.*;
import me.blvckbytes.bblibreflect.ICustomizableViewer;
import me.blvckbytes.bblibutil.Tuple;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 05/21/2022

  Build dynamic items by making use of the config value templating system.

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
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemBuilder implements IItemBuildable {

  private final ItemStack stack;
  private final ItemMeta meta;

  private boolean loreOverride;
  private boolean flagsOverride;
  private boolean enchantmentsOverride;
  private boolean customEffectsOverride;
  private boolean patternOverride;

  private @Nullable ConfigValue name;
  private @Nullable ConfigValue cAmount;
  private @Nullable ConfigValue cType;
  private @Nullable ConfigValue cColor;
  private @Nullable ConfigValue cTextures;
  private @Nullable ItemStackBaseEffectSection cBaseEffect;

  private final List<ConfigValue> lore;
  private final List<ItemStackEnchantmentSection> cEnchantments;
  private final List<ConfigValue> cFlags;
  private final List<ItemStackBannerPatternSection> cPatterns;
  private final List<ItemStackCustomEffectSection> cCustomEffects;

  private final IComponentApplicator componentApplicator;
  private final GradientGenerator gradientGenerator;

  ItemBuilder(
    ItemStack item,
    int amount,
    IComponentApplicator componentApplicator,
    GradientGenerator gradientGenerator
  ) {
    if (item == null)
      throw new IllegalStateException("Item cannot be null.");

    this.stack = item;
    this.meta = item.getItemMeta();

    if (this.meta == null)
      throw new IllegalStateException("Invalid item provided.");

    this.stack.setAmount(amount);
    this.componentApplicator = componentApplicator;
    this.gradientGenerator = gradientGenerator;

    this.lore = new ArrayList<>();
    this.cEnchantments = new ArrayList<>();
    this.cFlags = new ArrayList<>();
    this.cCustomEffects = new ArrayList<>();
    this.cPatterns = new ArrayList<>();
  }

  //=========================================================================//
  //                                 Builder                                 //
  //=========================================================================//

  /**
   * Change the type of this item
   * @param material New type
   */
  public ItemBuilder setType(Material material) {
    stack.setType(material);
    return this;
  }

  /**
   * Change the type of this item
   * @param material New type
   */
  public ItemBuilder setConfigType(ConfigValue material) {
    if (material == null)
      return this;

    cType = material;
    return this;
  }

  /**
   * Change the amount of this item
   * @param amount New amount
   */
  public ItemBuilder setAmount(int amount) {
    stack.setAmount(amount);
    return this;
  }

  /**
   * Change the amount of this item
   * @param amount New amount
   */
  public ItemBuilder setConfigAmount(ConfigValue amount) {
    if (amount == null)
      return this;

    cAmount = amount;
    return this;
  }

  /**
   * Add an enchantment with a specific level to this item
   * @param enchantment Enchantment to add
   * @param level Level to add the enchantment with
   */
  public ItemBuilder withEnchantment(Enchantment enchantment, int level) {
    this.meta.addEnchant(enchantment, level, true);
    return this;
  }

  /**
   * Add a enchantment section array to this item
   * @param enchantments Enchantment section
   */
  public ItemBuilder withConfigEnchantments(ItemStackEnchantmentSection[] enchantments) {
    enchantmentsOverride = false;
    cEnchantments.addAll(Arrays.asList(enchantments));
    return this;
  }

  /**
   * Add a enchantment section array to this item
   * @param enchantments Enchantment section
   */
  public ItemBuilder withOverridingConfigEnchantments(ItemStackEnchantmentSection[] enchantments) {
    enchantmentsOverride = true;
    cEnchantments.clear();
    cEnchantments.addAll(Arrays.asList(enchantments));
    return this;
  }

  public ItemBuilder withFlag(ItemFlag flag) {
    this.meta.addItemFlags(flag);
    return this;
  }

  public ItemBuilder withConfigFlags(@Nullable ConfigValue flag) {
    if (flag == null)
      return this;

    this.flagsOverride = false;
    this.cFlags.add(flag.copy());
    return this;
  }

  public ItemBuilder withOverridingConfigFlags(@Nullable ConfigValue flag) {
    if (flag == null)
      return this;

    this.flagsOverride = true;
    this.cFlags.clear();
    this.cFlags.add(flag.copy());
    return this;
  }

  public ItemBuilder withColor(Color color) {
    this.applyColor(color);
    return this;
  }

  public ItemBuilder withConfigColor(@Nullable ConfigValue color) {
    if (color == null)
      return this;

    this.cColor = color.copy();
    return this;
  }

  public ItemBuilder withBaseEffect(PotionData effect) {
    this.applyBaseEffect(effect);
    return this;
  }

  public ItemBuilder withConfigBaseEffect(@Nullable ItemStackBaseEffectSection effect) {
    if (effect == null)
      return this;

    this.cBaseEffect = effect;
    return this;
  }

  public ItemBuilder withCustomEffect(PotionEffect effect) {
    this.applyCustomEffect(effect);
    return this;
  }

  public ItemBuilder withConfigCustomEffects(ItemStackCustomEffectSection[] effects) {
    this.customEffectsOverride = false;
    this.cCustomEffects.addAll(Arrays.asList(effects));
    return this;
  }

  public ItemBuilder withOverridingConfigCustomEffects(ItemStackCustomEffectSection[] effects) {
    this.customEffectsOverride = true;
    this.cCustomEffects.clear();
    this.cCustomEffects.addAll(Arrays.asList(effects));
    return this;
  }

  /**
   * Set a display name
   * @param name Name to set
   */
  public ItemBuilder withName(@Nullable ConfigValue name) {
    this.name = name == null ? null : name.copy();
    return this;
  }

  /**
   * Add a lore to the existing lore
   * @param lore Lines to set
   */
  public ItemBuilder withLore(@Nullable ConfigValue lore) {
    if (lore == null)
      return this;

    this.lore.add(lore.copy());
    this.loreOverride = false;
    return this;
  }

  /**
   * Override all lore lines with the provided lore
   * @param lore Lines to set
   */
  public ItemBuilder withOverridingLore(@Nullable ConfigValue lore) {
    if (lore == null)
      return this;

    this.lore.clear();
    this.lore.add(lore.copy());
    this.loreOverride = true;
    return this;
  }

  public ItemBuilder withTextures(String textures) {
    this.applyTextures(textures);
    return this;
  }

  public ItemBuilder withConfigTextures(@Nullable ConfigValue textures) {
    if (textures == null)
      return this;

    this.cTextures = textures.copy();
    return this;
  }

  /**
   * Sets the head game profile of the item-meta
   * @param profile Game profile to set
   */
  public ItemBuilder withHeadProfile(GameProfile profile) {
    if (this.meta == null || !(this.meta instanceof SkullMeta))
      return this;

    // Try to find the setProfile method
    Method setProfileMethod = null;
    try {
      setProfileMethod = this.meta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
    } catch (Exception ignored) {}

    try {
      // if available, we use setProfile(GameProfile) so that it sets both the profile field and the
      // serialized profile field for us. If the serialized profile field isn't set
      // ItemStack#isSimilar() and ItemStack#equals() throw an error.

      // If setProfile is available, this is the preferred method, as it will also set
      // the serialized profile field without which bukkit will panic on ItemStack#isSimilar() or ItemStack#equals()
      if (setProfileMethod != null) {
        setProfileMethod.setAccessible(true);
        setProfileMethod.invoke(this.meta, profile);
        return this;
      }

      // Method unavailable, just set the GameProfile field
      Field profileField = meta.getClass().getDeclaredField("profile");
      profileField.setAccessible(true);
      profileField.set(meta, profile);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return this;
  }

  public ItemBuilder withPatterns(List<Pattern> patterns) {
    patterns.forEach(this::applyPattern);
    return this;
  }

  public ItemBuilder withConfigPatterns(ItemStackBannerPatternSection[] patterns) {
    this.patternOverride = false;
    this.cPatterns.addAll(Arrays.asList(patterns));
    return this;
  }

  public ItemBuilder withOverridingConfigPatterns(ItemStackBannerPatternSection[] patterns) {
    this.patternOverride = true;
    this.cPatterns.clear();
    this.cPatterns.addAll(Arrays.asList(patterns));
    return this;
  }

  //=========================================================================//
  //                              IItemBuildable                             //
  //=========================================================================//

  @Override
  public ItemStack build() {
    // Build without any customizations
    return build(null, null);
  }

  @Override
  public ItemStack build(@Nullable Map<String, String> variables, @Nullable ICustomizableViewer viewer) {
    ItemStack res = stack.clone();
    ItemMeta resMeta = res.getItemMeta();

    if (resMeta == null)
      throw new IllegalStateException("Invalid item provided.");

    //////////////////////////////////// Type ///////////////////////////////////

    if (cType != null) {
      XMaterial type = cType.copy().withVariables(variables).asScalar(XMaterial.class);

      if (type != null) {
        type.setType(res);
        resMeta = res.getItemMeta();

        if (resMeta == null)
          throw new IllegalStateException("Invalid item provided.");
      }
    }

    ////////////////////////////////// Amount ///////////////////////////////////

    if (cAmount != null) {
      Integer amount = cAmount.copy().withVariables(variables).asScalar(Integer.class);

      if (amount != null)
        res.setAmount(amount);
    }

    //////////////////////////////// Displayname ////////////////////////////////

    if (name != null) {
      componentApplicator.setDisplayName(
        name.copy().withVariables(variables).asComponent(gradientGenerator),
        viewer != null && viewer.cannotRenderHexColors(),
        res
      );
    }

    /////////////////////////////////// Lore ///////////////////////////////////

    if (loreOverride)
      resMeta.setLore(null);

    if (lore.size() > 0) {
      // Get old lore lines to extend, if applicable
      List<TextComponent> lines = (
        resMeta.getLore() == null ?
          new ArrayList<>() :
          resMeta.getLore().stream()
            .map(TextComponent::new)
            .collect(Collectors.toList())
      );

      // Extend by new lore lines
      for (ConfigValue line : lore)
        lines.addAll(line.copy().withVariables(variables).asComponentList(gradientGenerator));

      componentApplicator.setLore(
        lines,
        viewer != null && viewer.cannotRenderHexColors(),
        res
      );
    }

    /////////////////////////////////// Color //////////////////////////////////

    if (cColor != null)
      applyColor(cColor.copy().withVariables(variables).asScalar(Color.class));

    ////////////////////////////////// Textures /////////////////////////////////

    if (cTextures != null)
      applyTextures(cTextures.copy().withVariables(variables).asScalar());

    //////////////////////////////// Base Effect ////////////////////////////////

    if (cBaseEffect != null)
      applyBaseEffect(cBaseEffect.asData(variables));

    ////////////////////////////// Custom Effects ///////////////////////////////

    if (resMeta instanceof PotionMeta) {
      if (customEffectsOverride)
        ((PotionMeta) resMeta).clearCustomEffects();

      if (cCustomEffects.size() > 0)
        cCustomEffects.forEach(eff -> applyCustomEffect(eff.asEffect(variables)));
    }

    //////////////////////////////// Enchantments ///////////////////////////////

    if (enchantmentsOverride)
      resMeta.getEnchants().keySet().forEach(resMeta::removeEnchant);

    if (cEnchantments.size() > 0) {
      cEnchantments.forEach(ench -> {
        Tuple<Enchantment, Integer> enchantment = ench.asEnchantment(variables);

        // Invalid enchantment
        if (enchantment == null)
          return;

        // Apply enchantment
        meta.addEnchant(enchantment.getA(), enchantment.getB(), true);
      });
    }

    ///////////////////////////////// Item Flags ////////////////////////////////

    if (flagsOverride)
      resMeta.removeItemFlags(resMeta.getItemFlags().toArray(ItemFlag[]::new));

    if (cFlags.size() > 0) {
      cFlags.forEach(f -> {
        // Try to parse into an ItemFlag
        ItemFlag flag = f.asScalar(ItemFlag.class);

        if (flag != null)
          meta.addItemFlags(flag);
      });
    }

    /////////////////////////////// Banner Patterns /////////////////////////////

    if (resMeta instanceof BannerMeta) {
      if (patternOverride) {
        BannerMeta bm = (BannerMeta) resMeta;
        while (bm.getPatterns().size() > 0)
          bm.removePattern(0);
      }

      if (cPatterns.size() > 0)
        cPatterns.forEach(p -> applyPattern(p.asPattern(variables)));
    }

    return res;
  }

  @Override
  public ItemBuilder copy() {
    // Shallow list copies are enough, as config-values are always copied themselves
    return new ItemBuilder(
      new ItemStack(stack),
      meta.clone(),
      loreOverride,
      flagsOverride,
      enchantmentsOverride,
      customEffectsOverride,
      patternOverride,
      name,
      cAmount,
      cType,
      cColor,
      cTextures,
      cBaseEffect,
      new ArrayList<>(lore),
      new ArrayList<>(cEnchantments),
      new ArrayList<>(cFlags),
      new ArrayList<>(cPatterns),
      new ArrayList<>(cCustomEffects),
      componentApplicator, gradientGenerator
    );
  }

  @Override
  public ItemBuilder patch(ItemStackSection data) {
    ItemBuilder res = this.copy();

    if (data.getAmount() != null)
      res.setConfigAmount(data.getAmount());

    if (data.getType() != null)
      res.setConfigType(data.getType());

    if (data.getName() != null)
      res.withName(data.getName());

    if (data.getLore() != null) {
      if (data.isLoreOverride())
        res.withOverridingLore(data.getLore());
      else
        res.withLore(data.getLore());
    }

    if (data.getFlags() != null) {
      if (data.isFlagsOverride())
        res.withOverridingConfigFlags(data.getFlags());
      else
        res.withConfigFlags(data.getFlags());
    }

    if (data.getColor() != null)
      res.withConfigColor(data.getColor());

    if (data.getEnchantments().length > 0) {
      if (data.isEnchantmentsOverride())
        res.withOverridingConfigEnchantments(data.getEnchantments());
      else
        res.withConfigEnchantments(data.getEnchantments());
    }

    if (data.getTextures() != null)
      res.withConfigTextures(data.getTextures());

    if (data.getBaseEffect() != null)
      res.withConfigBaseEffect(data.getBaseEffect());

    if (data.getCustomEffects().length > 0) {
      if (data.isCustomEffectsOverride())
        res.withOverridingConfigCustomEffects(data.getCustomEffects());
      else
        res.withConfigCustomEffects(data.getCustomEffects());
    }

    if (data.getBannerPatterns().length > 0) {
      if (data.isBannerPatternsOverride())
        res.withOverridingConfigPatterns(data.getBannerPatterns());
      else
        res.withConfigPatterns(data.getBannerPatterns());
    }

    return res;
  }

  //=========================================================================//
  //                                Utilities                                //
  //=========================================================================//

  /**
   * Applies a color value to the item-meta, based on it's type
   * @param color Color value
   */
  private void applyColor(@Nullable Color color) {
    if (this.meta instanceof LeatherArmorMeta)
      ((LeatherArmorMeta) this.meta).setColor(color);

    else if (this.meta instanceof PotionMeta)
      ((PotionMeta) this.meta).setColor(color);

    else if (this.meta instanceof MapMeta)
      ((MapMeta) this.meta).setColor(color);
  }

  /**
   * Applies a potion base effect
   * @param effect Base effect
   */
  private void applyBaseEffect(PotionData effect) {
    if (this.meta instanceof PotionMeta)
      ((PotionMeta) this.meta).setBasePotionData(effect);
  }

  /**
   * Applies a custom potion effect
   * @param effect Custom effect
   */
  private void applyCustomEffect(@Nullable PotionEffect effect) {
    if (effect == null)
      return;

    if (this.meta instanceof PotionMeta)
      ((PotionMeta) this.meta).addCustomEffect(effect, true);
  }

  /**
   * Applies a base64 texture value to a skull
   * @param textures Texture value
   */
  private void applyTextures(String textures) {
    GameProfile prof = new GameProfile(UUID.randomUUID(), null);
    prof.getProperties().put("textures", new Property("textures", textures));
    withHeadProfile(prof);
  }

  /**
   * Applies a banner pattern to a banner
   * @param pattern Banner pattern
   */
  private void applyPattern(@Nullable Pattern pattern) {
    if (pattern == null)
      return;

    if (meta instanceof BannerMeta)
      ((BannerMeta) meta).addPattern(pattern);
  }
}
