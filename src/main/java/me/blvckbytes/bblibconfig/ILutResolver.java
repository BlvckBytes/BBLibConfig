package me.blvckbytes.bblibconfig;

import java.util.Map;
import java.util.Optional;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 07/09/2022

  Describes a global (config-scope) lookup table section resolver.
*/
public interface ILutResolver {

  /**
   * Get a lookup table by it's name
   * @param name Name of the target table
   * @return Optional map, empty if the LUT didn't exist
   */
  Optional<Map<String, String>> getLut(String name);

}
