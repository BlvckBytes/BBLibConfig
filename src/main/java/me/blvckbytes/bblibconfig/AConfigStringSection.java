package me.blvckbytes.bblibconfig;

import org.jetbrains.annotations.Nullable;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 07/24/2022

  The base class of a configuration section value which holds
  only string values which should have a human readable error notificaton fallback.
*/
public abstract class AConfigStringSection extends AConfigSection {

  /**
   * Called when a field wasn't found within the config and a default could be set
   * @param type Target field's type
   * @param field Target field name
   * @return Value to use as a default
   */
  @Override
  public @Nullable Object defaultFor(Class<?> type, String field) {
    if (type == ConfigValue.class)
      return ConfigValue.immediate("&cundefined");

    if (type == String.class)
      return "§cundefined";

    return null;
  }
}
