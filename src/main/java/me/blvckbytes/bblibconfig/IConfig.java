package me.blvckbytes.bblibconfig;

import java.util.Optional;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 04/23/2022

  Public interfaces which the Configuration provides to other consumers.
*/
public interface IConfig {

  /**
   * Get a value by it's config key from any available config file
   * @param path Path of the target config file (no leading slash, no .yml)
   * @param key Key to identify the value
   */
  Optional<ConfigValue> get(String path, String key);

  /**
   * Get an advanced config reader for a given file
   * @param path Path of the target config file (no leading slash, no .yml)
   */
  Optional<ConfigReader> reader(String path);

  /**
   * Get the file global LUT resolver
   * @param path Path of the target config file (no leading slash, no .yml)
   */
  Optional<ILutResolver> getLutResolver(String path);

}
