# PyzaNodeGameSelector

Example Bukkit/Spigot/Paper plugin that demonstrates how to:

- Call the **PyzaNode controller API** from a Minecraft plugin using the Java client (`pyzanode-api`)
- Open a configurable **GUI game selector**
- For a chosen **server group**:
  - Find an existing running server *or*
  - Create a new server via PyzaNode if none are available
  - Then send the player to that server via BungeeCord plugin messaging

---

## Features

- Bukkit / Spigot / Paper compatible (built against Spigot 1.20.x, works on 1.13+)
- Uses the `pyzanode-api` Java client to talk to the controller:
  - `getOrCreateServerForGroup(group, presetId)` – scale group and wait for a server
  - Returns `host:port` and a `serverName` suitable for proxy `/server` / BungeeCord "Connect"
- Fully configurable GUI in `config.yml`:
  - Menu title, rows, filler items
  - Per-button:
    - Slot, material, name, lore
    - Group name, optional preset ID override
    - Required permission, click sound
- Sends the player via **BungeeCord plugin messaging** (`BungeeCord` / `Connect` by default)
- Optional **PlaceholderAPI** integration stub (easy to extend with your own placeholders)

---

## Building

1. **Clone the repo** (requires Git, Java 17+, and Maven).
  ```bash
   git clone https://github.com/PyzaNode/PyzaNodeGameSelector.git
  ```
2. **Go to the project directory:**
  ```bash
   cd PyzaNodeGameSelector
  ```
3. **Build:**
  ```bash
   mvn clean compile
  ```
   For a deployable plugin JAR, use `mvn clean package` instead; the jar is at `target/PyzaNodeGameSelector-*.jar`.

---

## Installation & configuration

1. Drop the jar into your **Bukkit/Spigot/Paper** server's `plugins/` folder.
2. Start the server once to generate `config.yml`, then stop it.
3. Edit `plugins/PyzaNodeGameSelector/config.yml`:
  ```yaml
   pyzanode:
     controller-url: "http://127.0.0.1:9451"
     api-token: "PASTE_API_TOKEN_HERE" # from PyzaNode dashboard Settings → API key
     default-preset-id: "PRESET_ID_FROM_CONTROLLER"

   menu:
     title: "&aSelect a game"
     rows: 3
     entries:
       lobby:
         slot: 11
         material: "NETHER_STAR"
         name: "&bLobby"
         lore:
           - "&7Join a lobby server."
         group: "Lobby"
         preset-id: ""        # leave empty to use default-preset-id
       survival:
         slot: 15
         material: "DIAMOND_SWORD"
         name: "&aSurvival"
         lore:
           - "&7Join Survival."
         group: "Survival"
         preset-id: ""
  ```
4. Ensure your proxies are set up to route to PyzaNode-managed servers (e.g. via short codes / server names).
5. Start the server.

---

## Usage

- Command: `/gameselector`
  - Permission: `pyzanode.gameselector.open` (default: everyone)
- Admin reload: `/gameselector reload`
  - Permission: `pyzanode.gameselector.admin` (default: op)

When a player clicks a game icon:

1. The plugin looks up the **group** and **preset ID** from `config.yml`.
2. Calls `getOrCreateServerForGroup(group, presetId)` on the PyzaNode API:
  - If a running server exists → returns its connection info.
  - Otherwise → scales the group, waits for a server to become running, then returns that.
3. Sends the player to the selected server using the BungeeCord `BungeeCord` / `Connect` plugin channel.

---

## Customization ideas

- Add more menu entries under `menu.entries` (no code changes required).
- Use PlaceholderAPI to expose live PyzaNode metrics and show them in your GUI.
- Add per-network / per-preset routing logic (e.g. different groups per world).

Feel free to fork this repo and customize it for your network. 