package me.blvckbytes.bblibconfig.sections;

import lombok.Getter;
import me.blvckbytes.bblibconfig.AConfigSection;
import me.blvckbytes.bblibconfig.ConfigValue;
import org.jetbrains.annotations.Nullable;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 07/20/2022

  Represents all credentials and endpoint information which
  a database connection requires.
*/
@Getter
public class DatabaseSection extends AConfigSection {

  private ConfigValue username;
  private ConfigValue password;
  private ConfigValue host;
  private ConfigValue port;
  private ConfigValue database;

  @Override
  public @Nullable Object defaultFor(Class<?> type, String field) {
    if (type == ConfigValue.class)
      return ConfigValue.makeEmpty();

    return super.defaultFor(type, field);
  }
}
