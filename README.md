# Fuzzy Command History

**Adds fuzzy search for your chat command history – find any previous command instantly.**

Available on [Modrinth](https://modrinth.com/mod/fuzzy-command-history) and [CurseForge](https://www.curseforge.com/minecraft/mc-mods/fuzzy-command-history).

## 📖 Features

- Search your command history to quickly find any previously used command.
- Space-separated words each narrow the results independently
- Configurable via Mod Menu

## 📋 Usage

**Searching**

1. Press `Y` to open the search field
2. Type part of the command you're looking for - e.g. `give boat` to find all previously used `/give` commands involving a boat
3. Use `UP ARROW` / `DOWN ARROW` to select a result
4. Press `Enter` to send it to the chat box, or `Ctrl+Enter` to execute it immediately

**Multi-word filtering**

1. Each space-separated word must appear somewhere in the result
2. For example, `give oak boat` will match `/give Player minecraft:oak_boat` but not `/give Player minecraft:acacia_boat`

## 🔗 Dependencies

### Required
- [Fabric Loader](https://fabricmc.net/use/)
- [Fabric API](https://modrinth.com/mod/fabric-api)
- [Cloth Config](https://modrinth.com/mod/cloth-config)

### Optional
- [Mod Menu](https://modrinth.com/mod/modmenu) - for configuration (not strictly necessary if defaults work for you)

## 🏷️ Tags

`minecraft, mod, fabric, quality of life, qol, chat, command history, fuzzy search, fzf`
