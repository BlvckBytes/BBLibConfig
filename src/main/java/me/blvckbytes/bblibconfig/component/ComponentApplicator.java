package me.blvckbytes.bblibconfig.component;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import me.blvckbytes.bblibdi.AutoConstruct;
import me.blvckbytes.bblibdi.AutoInject;
import me.blvckbytes.bblibreflect.*;
import me.blvckbytes.bblibreflect.communicator.ChatCommunicator;
import me.blvckbytes.bblibreflect.communicator.parameter.ChatMessageParameter;
import me.blvckbytes.bblibutil.logger.ILogger;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

/*
  Author: BlvckBytes <blvckbytes@gmail.com>
  Created On: 08/12/2022

  Reflection based implementation of the component applicator.

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
public class ComponentApplicator extends AReflectedAccessor implements IComponentApplicator {

  private final Class<?> C_PO_TITLE, C_CLB_TITLES_ANIMATION, C_CLB_TITLE, C_CLB_SUBTITLE,
    C_ENUM_TITLE_ACTION, C_PO_PLAYER_INFO, C_ENUM_PLAYER_INFO_ACTION, C_PLAYER_INFO_DATA, C_ENUM_GAME_MODE, C_BASE_COMPONENT;

  private final Field F_CLB_TITLE__BASE_COMPONENT, F_CLB_SUBTITLE__BASE_COMPONENT,
    F_CLB_TITLES_ANIMATION__FADE_IN, F_CLB_TITLES_ANIMATION__STAY, F_CLB_TITLES_ANIMATION__FADE_OUT,
    F_PO_TITLE__BASE_COMPONENT, F_PO_TITLE__ENUM_TITLE_ACTION, F_PO_TITLE__FADE_IN, F_PO_TITLE__STAY, F_PO_TITLE__FADE_OUT,
    F_CRAFT_META_ITEM__NAME_BASE_COMPONENT, F_CRAFT_META_ITEM__NAME_STRING,
    F_CRAFT_META_ITEM__LORE_STRING_LIST, F_PO_PLAYER_INFO__ENUM, F_PO_PLAYER_INFO__LIST;

  private final Method M_CHAT_SERIALIZER__FROM_JSON, M_CRAFT_PLAYER__GET_PROFILE;

  private final boolean isNewerTitles;

  private final ChatCommunicator chatCommunicator;
  private final IPacketInterceptor interceptor;

  public ComponentApplicator(
    @AutoInject IReflectionHelper helper,
    @AutoInject ILogger logger,
    @AutoInject ChatCommunicator chatCommunicator,
    @AutoInject IPacketInterceptor interceptor
  ) throws Exception {
    super(logger, helper);

    this.chatCommunicator = chatCommunicator;
    this.interceptor = interceptor;

    Class<?> C_CHAT_SERIALIZER = requireClass(RClass.CHAT_SERIALIZER);
    Class<?> C_CRAFT_PLAYER    = requireClass(RClass.CRAFT_PLAYER);
    Class<?> C_CRAFT_META_ITEM = requireClass(RClass.CRAFT_META_ITEM);

    C_BASE_COMPONENT          = requireClass(RClass.I_CHAT_BASE_COMPONENT);
    C_PO_TITLE                = optionalClass(RClass.PACKET_O_TITLE);
    C_PO_PLAYER_INFO          = optionalClass(RClass.PACKET_O_PLAYER_INFO);
    C_PLAYER_INFO_DATA        = optionalClass(RClass.PLAYER_INFO_DATA);
    C_ENUM_GAME_MODE          = optionalClass(RClass.ENUM_GAME_MODE);
    C_ENUM_PLAYER_INFO_ACTION = optionalClass(RClass.ENUM_PLAYER_INFO_ACTION);
    C_ENUM_TITLE_ACTION       = optionalClass(RClass.ENUM_TITLE_ACTION);
    C_CLB_TITLES_ANIMATION    = optionalClass(RClass.CLIENTBOUND_TITLES_ANIMATION);
    C_CLB_TITLE               = optionalClass(RClass.CLIENTBOUND_TITLE_SET);
    C_CLB_SUBTITLE            = optionalClass(RClass.CLIENTBOUND_SUBTITLE_SET);

    M_CHAT_SERIALIZER__FROM_JSON = requireArgsMethod(C_CHAT_SERIALIZER, new Class[] { JsonElement.class }, C_BASE_COMPONENT, false);
    M_CRAFT_PLAYER__GET_PROFILE  = requireNamedMethod(C_CRAFT_PLAYER, "getProfile", GameProfile.class, false);

    isNewerTitles = C_CLB_TITLE != null && C_CLB_SUBTITLE != null && C_CLB_TITLES_ANIMATION != null;

    F_CRAFT_META_ITEM__NAME_BASE_COMPONENT = optionalScalarField(C_CRAFT_META_ITEM, C_BASE_COMPONENT, 0, false, false, null);
    F_CRAFT_META_ITEM__NAME_STRING = optionalScalarField(C_CRAFT_META_ITEM, String.class, 0, false, false, null);

    if (F_CRAFT_META_ITEM__NAME_BASE_COMPONENT == null && F_CRAFT_META_ITEM__NAME_STRING == null)
      throw new IllegalStateException("Couldn't find neither base component nor string lore field");

    F_CRAFT_META_ITEM__LORE_STRING_LIST = requireCollectionField(C_CRAFT_META_ITEM, List.class, String.class, 0, false, false, null);

    // PacketPlayOut (older)
    if (C_PO_TITLE != null) {
      F_PO_TITLE__BASE_COMPONENT    = requireScalarField(C_PO_TITLE, C_BASE_COMPONENT, 0, false, false, null);
      F_PO_TITLE__ENUM_TITLE_ACTION = requireScalarField(C_PO_TITLE, C_ENUM_TITLE_ACTION, 0, false, false, null);
      F_PO_TITLE__FADE_IN           = requireScalarField(C_PO_TITLE, int.class, 0, false, false, null);
      F_PO_TITLE__STAY              = requireScalarField(C_PO_TITLE, int.class, 1, false, false, null);
      F_PO_TITLE__FADE_OUT          = requireScalarField(C_PO_TITLE, int.class, 2, false, false, null);

      F_CLB_TITLE__BASE_COMPONENT = null;
      F_CLB_SUBTITLE__BASE_COMPONENT = null;
      F_CLB_TITLES_ANIMATION__FADE_IN = null;
      F_CLB_TITLES_ANIMATION__STAY = null;
      F_CLB_TITLES_ANIMATION__FADE_OUT = null;
    }

    // Clientbound packets (newer)
    else if (isNewerTitles) {
      F_CLB_TITLE__BASE_COMPONENT = requireScalarField(C_CLB_TITLE, C_BASE_COMPONENT, 0, false, false, null);
      F_CLB_SUBTITLE__BASE_COMPONENT = requireScalarField(C_CLB_SUBTITLE, C_BASE_COMPONENT, 0, false, false, null);
      F_CLB_TITLES_ANIMATION__FADE_IN = requireScalarField(C_CLB_TITLES_ANIMATION, int.class, 0, false, false, null);
      F_CLB_TITLES_ANIMATION__STAY = requireScalarField(C_CLB_TITLES_ANIMATION, int.class, 1, false, false, null);
      F_CLB_TITLES_ANIMATION__FADE_OUT = requireScalarField(C_CLB_TITLES_ANIMATION, int.class, 2, false, false, null);

      F_PO_TITLE__BASE_COMPONENT = null;
      F_PO_TITLE__ENUM_TITLE_ACTION = null;
      F_PO_TITLE__FADE_IN = null;
      F_PO_TITLE__STAY = null;
      F_PO_TITLE__FADE_OUT = null;
    }

    else
      throw new IllegalStateException("Couldn't find neither newer nor older title packets.");

    F_PO_PLAYER_INFO__ENUM = requireScalarField(C_PO_PLAYER_INFO, C_ENUM_PLAYER_INFO_ACTION, 0, false, false, null);
    F_PO_PLAYER_INFO__LIST = requireCollectionField(C_PO_PLAYER_INFO, List.class, C_PLAYER_INFO_DATA, 0, false, false, null);
  }

  @Override
  public void setDisplayName(IComponent displayName, boolean approximateColors, ItemStack item) {
    ItemMeta meta = item.getItemMeta();

    // Meta unavailable
    if (meta == null)
      return;

    try {
      if (F_CRAFT_META_ITEM__NAME_STRING != null)
        F_CRAFT_META_ITEM__NAME_STRING.set(meta, displayName.toJson(approximateColors).toString());

      if (F_CRAFT_META_ITEM__NAME_BASE_COMPONENT != null)
        F_CRAFT_META_ITEM__NAME_BASE_COMPONENT.set(meta, M_CHAT_SERIALIZER__FROM_JSON.invoke(null, displayName.toJson(approximateColors)));

      item.setItemMeta(meta);
    } catch (Exception e) {
      logger.logError(e);
    }
  }

  @Override
  public void setLore(List<? extends IComponent> lines, boolean approximateColors, ItemStack item) {
    ItemMeta meta = item.getItemMeta();

    // Meta unavailable
    if (meta == null)
      return;

    try {
      // Jsonify all lore lines
      List<String> lore = lines.stream()
        .map(c -> c.toJson(approximateColors).toString())
        .collect(Collectors.toList());

      // Overwrite the list ref
      F_CRAFT_META_ITEM__LORE_STRING_LIST.set(meta, lore);

      item.setItemMeta(meta);
    } catch (Exception e) {
      logger.logError(e);
    }
  }

  @Override
  public void sendChat(IComponent message, ICustomizableViewer viewer) {
    try {
      chatCommunicator.sendParameterized(
        viewer,
        new ChatMessageParameter(message.toJson(viewer.cannotRenderHexColors()), true)
      );
    } catch (Exception e) {
      logger.logError(e);
    }
  }

  @Override
  public void sendChat(IComponent message, Player p) {
    sendChat(message, interceptor.getPlayerAsViewer(p));
  }

  @Override
  public void sendActionBar(IComponent text, ICustomizableViewer viewer) {
    try {
      chatCommunicator.sendParameterized(
        viewer,
        new ChatMessageParameter(text.toJson(viewer.cannotRenderHexColors()), false)
      );
    } catch (Exception e) {
      logger.logError(e);
    }
  }

  @Override
  public void sendActionBar(IComponent text, Player p) {
    sendActionBar(text, interceptor.getPlayerAsViewer(p));
  }

  @Override
  @SuppressWarnings("unchecked")
  public void sendTitle(IComponent title, IComponent subtitle, int fadeIn, int duration, int fadeOut, ICustomizableViewer viewer) {
    try {
      // Older version, create three different instances of the same packet
      if (!isNewerTitles) {
        Object setTimes = helper.createEmptyPacket(C_PO_TITLE), setTitle = helper.createEmptyPacket(C_PO_TITLE), setSubtitle = helper.createEmptyPacket(C_PO_TITLE);

        // 0 TITLE, 1 SUBTITLE, 2 ACTIONBAR, 3 TIMES, 4 CLEAR, 5 RESET
        Enum<?>[] titleActions = ((Class<? extends Enum<?>>) C_ENUM_TITLE_ACTION).getEnumConstants();

        F_PO_TITLE__ENUM_TITLE_ACTION.set(setTimes, titleActions[3]);
        F_PO_TITLE__FADE_IN.set(setTimes, fadeIn);
        F_PO_TITLE__STAY.set(setTimes, duration);
        F_PO_TITLE__FADE_OUT.set(setTimes, fadeOut);

        F_PO_TITLE__ENUM_TITLE_ACTION.set(setTitle, titleActions[0]);
        F_PO_TITLE__BASE_COMPONENT.set(setTitle, M_CHAT_SERIALIZER__FROM_JSON.invoke(null, title.toJson(viewer.cannotRenderHexColors())));

        F_PO_TITLE__ENUM_TITLE_ACTION.set(setTimes, titleActions[1]);
        F_PO_TITLE__BASE_COMPONENT.set(setSubtitle, M_CHAT_SERIALIZER__FROM_JSON.invoke(null, subtitle.toJson(viewer.cannotRenderHexColors())));

        viewer.sendPackets(setTimes, setTimes, setSubtitle);
        return;
      }

      Object setTimes = helper.createEmptyPacket(C_CLB_TITLES_ANIMATION), setTitle = helper.createEmptyPacket(C_CLB_TITLE), setSubtitle = helper.createEmptyPacket(C_CLB_SUBTITLE);

      F_CLB_TITLES_ANIMATION__FADE_IN.set(setTimes, fadeIn);
      F_CLB_TITLES_ANIMATION__STAY.set(setTimes, duration);
      F_CLB_TITLES_ANIMATION__FADE_OUT.set(setTimes, fadeOut);

      F_CLB_TITLE__BASE_COMPONENT.set(setTitle, M_CHAT_SERIALIZER__FROM_JSON.invoke(null, title.toJson(viewer.cannotRenderHexColors())));

      F_CLB_SUBTITLE__BASE_COMPONENT.set(setSubtitle, M_CHAT_SERIALIZER__FROM_JSON.invoke(null, subtitle.toJson(viewer.cannotRenderHexColors())));

      viewer.sendPackets(setTimes, setTitle, setSubtitle);
    } catch (Exception e) {
      logger.logError(e);
    }
  }

  @Override
  public void sendTitle(IComponent title, IComponent subtitle, int fadeIn, int duration, int fadeOut, Player p) {
    sendTitle(title, subtitle, fadeIn, duration, fadeOut, interceptor.getPlayerAsViewer(p));
  }

  @Override
  @SuppressWarnings("unchecked")
  public void updatePlayerlistName(@Nullable IComponent name, Player p) {
    // TODO: Don't broadcast but rather scope per player to have proper individualized #cannotRenderHexColors

    try {
      Object info = helper.createEmptyPacket(C_PO_PLAYER_INFO);
      ICustomizableViewer viewer = interceptor.getPlayerAsViewer(p);
      JsonObject json = name == null ? null : name.toJson(viewer.cannotRenderHexColors());

      // 0: add player, 1: update gamemode, 2: update latency, 3: update display name, 4: remove player
      Enum<?> updateEnum = ((Class<? extends Enum<?>>) C_ENUM_PLAYER_INFO_ACTION).getEnumConstants()[3];
      GameProfile profile = (GameProfile) M_CRAFT_PLAYER__GET_PROFILE.invoke(p);

      List<?> dataList = List.of(
        C_PLAYER_INFO_DATA
          .getConstructor(GameProfile.class, int.class, C_ENUM_GAME_MODE, C_BASE_COMPONENT)
          .newInstance(
            profile, p.getEntityId(), null,
            M_CHAT_SERIALIZER__FROM_JSON.invoke(null, json)
          )
      );

      F_PO_PLAYER_INFO__ENUM.set(info, updateEnum);
      F_PO_PLAYER_INFO__LIST.set(info, dataList);

      interceptor.broadcastPackets(info);
    } catch (Exception e) {
      logger.logError(e);
    }
  }
}
