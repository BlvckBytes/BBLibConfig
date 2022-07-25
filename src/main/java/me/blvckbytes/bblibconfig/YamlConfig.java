package me.blvckbytes.bblibconfig;

import me.blvckbytes.bblibdi.AutoConstruct;
import me.blvckbytes.bblibdi.AutoInject;
import me.blvckbytes.bblibdi.AutoInjectLate;
import me.blvckbytes.bblibdi.IAutoConstructed;
import me.blvckbytes.bblibutil.Tuple;
import me.blvckbytes.bblibutil.logger.ILogger;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 04/22/2022

  Load a yaml configuration from it's file into memory.
*/
@AutoConstruct
public class YamlConfig implements IConfig, IAutoConstructed {

  // Mapping config paths to a tuple of the in-memory config and it's underlying file
  private final Map<String, Tuple<YamlConfiguration, File>> configs;
  private final Map<String, ConfigReader> readers;
  private final Map<String, ILutResolver> resolvers;
  private final JavaPlugin plugin;

  @AutoInjectLate
  private ILogger logger;

  public YamlConfig(
    @AutoInject JavaPlugin plugin
  ) {
    this.configs = new HashMap<>();
    this.readers = new HashMap<>();
    this.resolvers = new HashMap<>();
    this.plugin = plugin;

    // Copy default config files from the resource folder
    this.copyDefaults(new String[] {}, new String[] {"config"});

    // Initially load the main config (always needed)
    this.load("config");
  }

  //=========================================================================//
  //                                   API                                   //
  //=========================================================================//

  @Override
  public Optional<ConfigValue> get(String path, String key) {
    Tuple<YamlConfiguration, File> handle = load(path).orElse(null);

    if (handle == null)
      return Optional.empty();

    return retrieve(path, handle, key);
  }

  @Override
  public Optional<ConfigReader> reader(String path) {
    Tuple<YamlConfiguration, File> handle = load(path).orElse(null);

    if (handle == null)
      return Optional.empty();

    // Cache readers to be re-used
    if (readers.containsKey(path))
      return Optional.of(readers.get(path));

    ConfigReader reader = new ConfigReader(this, path, logger);
    readers.put(path, reader);
    return Optional.of(reader);
  }

  @Override
  public void cleanup() {
    // Nothing to do here (yet)
  }

  @Override
  public void initialize() {
    // Nothing to do here (yet)
  }

  //=========================================================================//
  //                               Utilities                                 //
  //=========================================================================//

  @SuppressWarnings("unchecked")
  private Optional<ConfigValue> retrieveIndexed(String path, Tuple<YamlConfiguration, File> handle, String key) {
    // Object of the currently iterated level within the loop
    Object obj = null;

    // Iterate all key levels
    String[] levels = key.split("\\.");
    for (int j = 0; j < levels.length; j++) {
      String level = levels[j];
      Integer i = null;

      // Indexed level, splice off the index notation and parse the requested level
      if (level.matches("(.*)\\[\\d+\\]$")) {
        // Get the requested index and splice off the index notation
        String index = level.substring(level.indexOf('[') + 1, level.indexOf(']'));
        level = level.substring(0, level.indexOf('['));

        try {
          i = Integer.parseInt(index);
        }

        // Invalid index provided
        catch (NumberFormatException e) {
          return Optional.empty();
        }
      }

      // Get the initial value
      if (j == 0)
        obj = handle.getA().get(level);

      // Get the next level from the map
      else {
        // Maps are usually encountered when indexing into memory sections
        if (obj instanceof Map<?, ?>)
          obj = ((Map<?, ?>) obj).get(level);

        // Memory sections are usually encountered at top level scope
        else if (obj instanceof MemorySection)
          obj = ((MemorySection) obj).get(level);

        // Unknown data-structure, cannot get the key
        else
          return Optional.empty();
      }

      // Get the target index value from the current list
      if (i != null) {
        // Can only index when the result is a list
        if (!(obj instanceof List<?>))
          return Optional.empty();

        List<?> l = (List<?>) obj;

        // Out of range
        if (i < 0 || i >= l.size())
          return Optional.empty();

        // Retrieve the target list item
        obj = l.get(i);
      }

      // Cannot resolve further
      if (obj == null)
        return Optional.empty();
    }

    // Didn't yield anything
    if (obj == null)
      return Optional.empty();

    // Is a list, pass as a list
    if (obj instanceof List<?>)
      return Optional.of(new ConfigValue((List<Object>) obj, getLutResolver(path).orElse(null)));

    // Pass as an unknown blob
    return Optional.of(new ConfigValue(obj, getLutResolver(path).orElse(null)));
  }

  /**
   * Retrieve a config value from a given handle by it's key
   * @param handle Config handle
   * @param key Key to retrieve
   * @return Optional value, empty if the handle was null or the key is invalid
   */
  @SuppressWarnings("unchecked")
  private Optional<ConfigValue> retrieve(String path, Tuple<YamlConfiguration, File> handle, String key) {
    // Config failed to load
    if (handle == null)
      return Optional.empty();

    // Caller wants to index at least once, the path needs to be walked manually
    if (key.matches("(.*)\\[\\d+\\](.*)"))
      return retrieveIndexed(path, handle, key);

    // Key unknown
    Object val = handle.getA().get(key);
    if (val == null)
      return Optional.empty();

    // Is a list
    Class<?> valC = val.getClass();
    if (List.class.isAssignableFrom(valC))
      return Optional.of(new ConfigValue((List<Object>) val, getLutResolver(path).orElse(null)));

    // Is a scalar
    else
      return Optional.of(new ConfigValue(val, getLutResolver(path).orElse(null)));
  }

  /**
   * Load the config from the corresponding file
   * @param path Path to the file
   */
  private Optional<Tuple<YamlConfiguration, File>> load(String path) {
    Tuple<YamlConfiguration, File> handle = configs.get(path);

    // Config already loaded
    if (handle != null)
      return Optional.of(handle);

    try {
      File df = plugin.getDataFolder();

      // Create data folder if absent
      if (!df.exists())
        if (!df.mkdirs())
          throw new RuntimeException("Could not create data-folder to store the config in");

      File yf = new File(df.getAbsolutePath() + "/" + path + ".yml");

      // File does not exist
      if (!yf.exists()) {

        // Create file if absent for the main config
        if (path.equals("config")) {
          if (!yf.createNewFile())
            throw new RuntimeException("Could not create config file");
        }

        // Skip missing files otherwise
        else
          return Optional.empty();
      }

      // Load configuration from file
      YamlConfiguration cfg = YamlConfiguration.loadConfiguration(yf);
      handle = new Tuple<>(cfg, yf);
      configs.put(path, handle);

      return Optional.of(handle);
    } catch (Exception e) {
      if (logger == null)
        e.printStackTrace();
      else
        logger.logError(e);
    }

    return Optional.empty();
  }

  /**
   * Save the local yaml config into it's file
   * @param handle Handle to the file
   */
  private void save(Tuple<YamlConfiguration, File> handle) {
    // No config loaded yet
    if (handle.getA() == null || handle.getB() == null)
      return;

    try {
      // Save config using the file handle
      handle.getA().save(handle.getB());
    } catch (Exception e) {
      if (logger == null)
        e.printStackTrace();
      else
        logger.logError(e);
    }
  }

  /**
   * Copies default .yml files from the resources folder if they are not yet exising
   * @param folders Folders to copy from the resources-folder
   * @param files Top level files to copy from the resources-folder
   */
  private void copyDefaults(String[] folders, String[] files) {
    try {

      List<Tuple<String, InputStream>> targets = new ArrayList<>();

      // Add all top level files
      Arrays.stream(files)
        .map(file -> new Tuple<>(file + ".yml", plugin.getResource(file + ".yml")))
        .forEach(targets::add);

      // Add all files within a folder
      for (String folder : folders)
        targets.addAll(getResourceFiles(folder));

      for (Tuple<String, InputStream> file : targets) {
        // Not a yaml configuration file
        if (!file.getA().endsWith(".yml"))
          continue;

        File f = new File(plugin.getDataFolder(), file.getA());

        // This yaml has already been copied before
        if (f.exists())
          continue;

        // Create parent directories
        if (f.getParentFile().exists() || f.getParentFile().mkdirs()) {
          // Copy stream contents into the file
          FileOutputStream fos = new FileOutputStream(f);
          InputStream is = file.getB();
          fos.write(is.readAllBytes());
          is.close();
          fos.close();
        }
      }

    } catch (Exception e) {
      if (logger == null)
        e.printStackTrace();
      else
        logger.logError(e);
    }
  }

  /**
   * Gets all files of a folder within the jar's resources folder
   * @param directoryName Name of the target directory
   * @return List of tuple from file path to it's input stream containing the data
   */
  public List<Tuple<String, InputStream>> getResourceFiles(String directoryName) throws Exception {
    List<Tuple<String, InputStream>> streams = new ArrayList<>();
    URL url = getClass().getClassLoader().getResource(directoryName);

    // Resource not found
    if (url == null)
      return streams;

    // This routine only supports listing within the resources folder
    if (!url.getProtocol().equals("jar"))
      return streams;

    String dirname = directoryName + "/";
    String path = url.getPath();
    String jarPath = path.substring(5, path.indexOf("!"));

    try (
      JarFile jar = new JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8.name()))
    ) {
      Enumeration<JarEntry> entries = jar.entries();
      while (entries.hasMoreElements()) {
        JarEntry entry = entries.nextElement();
        String name = entry.getName();

        // Is a child of this directory
        if (name.startsWith(dirname) && !dirname.equals(name)) {
          InputStream resource = getClass().getClassLoader().getResourceAsStream(name);
          streams.add(new Tuple<>(name, resource));
        }
      }
    }

    return streams;
  }

  /**
   * Get a LUT resolver for a specific configuration file by it's path
   * @param path Path of the target file
   * @return Optional LUT resolver, empty if the path was invalid
   */
  @SuppressWarnings("unchecked")
  @Override
  public Optional<ILutResolver> getLutResolver(String path) {
    // Perform a cache lookup first
    ILutResolver cache = resolvers.get(path);
    if (cache != null)
      return Optional.of(cache);

    // Could not get a corresponding reader
    ConfigReader reader = reader(path).orElse(null);
    if (reader == null)
      return Optional.empty();

    // Create a new instance of the lut resolver
    ILutResolver resolver = name -> (
      reader.parseValue("lut." + name, Map.class, true)
        .map(m -> (Map<String, String>) m)
    );

    // Cache and respond
    resolvers.put(path, resolver);
    return Optional.of(resolver);
  }
}
