package net.fabricmc.example.lua.api;

import org.terasology.jnlua.LuaState;

/**
 * A LuaApi can add itself to a provided LuaState.
 * Generally this is done by pushing a table, then
 * assigning it to a global.
 */
public interface LuaApi {
    void addTo(LuaState luaState);
}
