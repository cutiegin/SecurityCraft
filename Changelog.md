--------------------------Changelog for v1.9.11 of SecurityCraft--------------------------

- New: More blocks now support the Disguise Module: Keypad Door, Keypad Trapdoor, Passcode-Protected Chest, Reinforced Dispenser, Reinforced Dropper, Reinforced Hopper, Scanner Door, Scanner Trapdoor, Security Camera, Sonic Security System
- New: Server config setting to set the amount of damage received when suffocating in a reinforced block
- New: Item tag "securitycraft:keycards" for all keycards (this notably exclused the Limited Use Keycard, as it's merely a crafting component)
- New: Item tag "securitycraft:keycard_holder_can_hold" for all items that can be put into a Keycard Holder (contains the securitycraft:keycards item tag and the Limited Use Keycard by default)
- New: Reinforced Soul Sand and Reinforced Magma Block
- New: The Block Change Detector can now be placed on walls and the ceiling
- New: Russian translation (Thanks cutiegin!)
- New: The Smart Module in a Security Camera now also saves the zoom amount
- New: Support for NeoForge's configuration screen
- New: HUD mods like Jade and The One Probe now show whether an installed module is disabled
- New: ɥsᴉꞁᵷuƎ (uʍoᗡ ǝpᴉsd∩) translation
- Change: Security Camera head display
- Change: The damage taken when suffocating in a reinforced block is now halved (from 5 hearts to two and a half hearts)
- Change: IMS bombs can no longer be diverted from their path using explosions
- Change: The "(team)" suffix for owners is now only shown when the team has more than one player in it
- API: New interface IDisguisable to abstract away from the DisguisableBlock class. This means blocks no longer need to extend it to be disguisable, but instead need to implement the interface
- Fix: The Alarm ceases to emit any sound when selecting a different sound
- Fix: When closing the screen of a Briefcase, Disguise Module or Keycard Holder, items carried by the mouse are deleted
- Fix: Players are kicked from the world when a Disguise Module is removed from certain blocks
- Fix: Disabled security cameras sometimes start rotating again when near other rotating cameras
- Fix: Disguising reinforced observers can have visual issues
- Fix: Disguised secure redstone interfaces have incorrect collision, and are see-through
- Fix: The Secure Redstone Interface screen cannot be closed by pressing the inventory key
- Fix: Reinforced stairs can be destroyed through explosions
- Fix: Potential crash when hovering over specific items of SecurityCraft in the inventory
- Fix: Note info tooltips on items are positioned out of place sometimes
- Fix: Client crash when trying to un-/reinforce blocks in a crafting table on a server
- Fix: The Protecto can attack armor stands
- Fix: The Protecto attacks when it shouldn't
- Fix: Fake Water can be placed in the nether
- Fix: Buckets containing Fake Water/Lava are incompatible with tanks of other mods
- Fix: Lily pads cannot be placed on Reinforced Ice
- Fix: The Block Change Detector keeps outputting a redstone signal when all its entries are cleared
- Fix: The Denylist Module does not work
- Fix: Option slider text shows the value in the incorrect position
- Fix: Potential crash
- Fix: Setting the signal length option to 0 on a Keypad Trapdoor makes the trapdoor not open at all
- Fix: Block Pocket and Block Change Detector outline/block highlights are drawn too thin at certain angles

--------------------------Changelog for v1.9.10-beta9 of SecurityCraft--------------------------

- Fix: Crash when pushing or pulling reinforced blocks with reinforced pistons

--------------------------Changelog for v1.9.10-beta8 of SecurityCraft--------------------------

- Fix: Compatibility with Minecraft 1.21.1

--------------------------Changelog for v1.9.10-beta7 of SecurityCraft--------------------------

- Fix: Compatibility with NeoForge 21.0.66-beta and newer
- Misc.: The minimum required NeoForge version is now 21.0.76-beta

--------------------------Changelog for v1.9.10-beta6 of SecurityCraft--------------------------

- Fix: IMS bombs do not fly towards their target after ascending
- Fix: Compatibility with NeoForge 21.0.39-beta and newer
- Misc.: The minimum required NeoForge version is now 21.0.39-beta

--------------------------Changelog for v1.9.10-beta5 of SecurityCraft--------------------------

- New: Readded JEI integration
- Fix: Compatibility with NeoForge 21.0.31-beta and newer
- Fix: Crash when a Sentry shoots through a portal
- Misc.: The minimum required NeoForge version is now 21.0.31-beta

--------------------------Changelog for v1.9.10-beta4 of SecurityCraft--------------------------

- New: Block and item tags "securitycraft:reinforced/concrete" and "securitycraft:reinforced/glazed_terracotta"
- Fix: Compatibility with NeoForge 21.0.10-beta and newer
- Fix: Crash when a Sentry shoots through a portal
- Misc.: The minimum required NeoForge version is now 21.0.10-beta

--------------------------Changelog for v1.9.10-beta3 of SecurityCraft--------------------------

- Fix: Crash when a Sentry shoots

--------------------------Changelog for v1.9.10-beta2 of SecurityCraft--------------------------

- Fix: Passcodes set in prior Minecraft versions are no longer accepted
- Fix: Crash when viewing a camera while Embeddium is installed

--------------------------Changelog for v1.9.10-beta1 of SecurityCraft--------------------------

- New: Server config setting "allow_camera_night_vision" to set whether players are able to activate night vision without having the actual potion effect
- New: Pressing "Enter" while typing a player name in an Allowlist/Denylist Module will now add the player to the list without needing to press the "Add Player" button
- New: Security Sea Boats: Chest boats with a passcode-protected chest
- New: Damage Type Tag "securitycraft:security_sea_boat_vulnerable_to" to define which damage types the Security Sea Boat can be destroyed by
- New: Server config option "passcode_check_cooldown" to configure the time that needs to pass between two separate attempts from a player to enter a passcode
- New: Secure Redstone Interface for owner-restricted redstone signal transfer
- New: Operators in creative mode can now teleport to a camera via the camera monitor
- New: The Reinforced Cobweb now supports the weaving effect
- New: The Trophy System can now target wind charges
- New: Reinforced Blocks: Chiseled Copper, Exposed Chiseled Copper, Weathered Chiseled Copper, Oxidized Chiseled Copper, Copper Grate, Exposed Copper Grate, Weathered Copper Grate, Oxidized Copper Grate, Copper Bulb, Exposed Copper Bulb, Weathered Copper Bulb, Oxidized Copper Bulb, Tuff Stairs, Tuff Slab, Tuff Wall, Chiseled Tuff, Polished Tuff, Polished Tuff Stairs, Polished Tuff Slab, Polished Tuff Wall, Tuff Bricks, Tuff Brick Stairs, Tuff Brick Slab, Tuff Brick Wall, Chiseled Tuff Bricks
- New: The reinforcing and unreinforcing of blocks can now be automated by putting the convertible block with a Universal Block Reinforcer in a Crafter
- Change: The cameraSpeed client side config setting has been moved to be a per-block option, accessible with the Universal Block Modifier
- Change: Some SecurityCraft tip messages have been reworded for clarity
- Change: Increased suffocation damage inside reinforced blocks no longer affects non-player entities and players owning the reinforced blocks
- Change: The "preventReinforcedFloorGlitching" configuration option no longer affects players trying to glitch through reinforced blocks that they are the owner of
- Change: Players in creative mode can once again use the codebreaker on their own blocks
- Change: The "codebreaker_chance" config setting has been moved to the "securitycraft:success_chance" item component
- Change: When picking up a placed sentry, the resulting sentry item will now be named according to the custom name of the removed sentry
- Change: The "respect_invisibility" config setting has been moved to a per-block option
- Change: The Sentry can no longer attack invisible entities
- API: Changed constructors for IntOption and DoubleOption, they are now always sliders by default
- API: Removed FloatOption. Use DoubleOption instead
- API: IModuleInventory is no longer hardcoded to just block entities
- API: New method IPasscodeProtected#openSetPasscodeScreen to define how to open the screen to set the passcode of the object
- API: New method ICodebreakable#handleCodebreaking to define behavior when a codebreaker is used to break the code
- API: The BlockState parameters in ICodebreakable's methods have been removed
- API: New Option "EntityDataWrappedOption" that connects an EntityDataAccessor with an Option, and corresponding converter method "wrapForEntityData"
- API: New method Option#getValueText for getting a textual representation of the option's value
- API: ICustomizable#onOptionChanged now has a proper generic type
- API: New method IViewActivated#isConsideredInvisible
- API: New method Owner#copy to copy the owner into a new object
- API: IOwnable#onOwnerChanged now has two new parameters: oldOwner and newOwner
- API: New method IOwnable#onValidate that gets called when the underlying owner is validated
- Fix: Trying to place a Panic Button where a normal button cannot be placed crashes the game
- Fix: Occasional crash when opening the inventory in creative mode in certain situations
- Fix: Reinforced fence gates don't properly retain their owner when reloading the world
- Fix: The debug world does not work with SecurityCraft installed
- Fix: The block pocket can be assembled without the necessary items
- Fix: Reinforcing a placed end rod will make the resulting reinforced end rod behave as if it had no owner until rejoining the world
- Fix: The Reinforced Lever has incorrect break/place sounds
- Fix: SecurityCraft's WTHIT config does not work on the client
- Fix: Crash when trying to toggle the redstone state of a camera immediately after mounting it
- Fix: Crash when trying to remove the passcode of a Briefcase using a Universal Key Changer
- Fix: The Display Case doesn't drop inserted modules when the block the display case is placed on is removed
- Fix: A previously open Display Case would replay its opening animation when joining a world or teleporting to it
- Fix: Fake Water/Fake Lava can be brewed using any kind of potion instead of only harming/healing potions
- Fix: Randomizing the signature of a Keycard Reader stops working when interacting with the block from certain angles
- Fix: Floor Trap cloud particles do not spawn when standing at certain positions relative to the Floor Trap
- Fix: Cloning a passcode-protected block using the /clone command will invalidate the passcode of the original block if the clone is removed
- Fix: Sonic Security System settings sometimes do not persist through world reloads
- Fix: The Block Pocket Manager's storage does not persist through world reloads
- Fix: Potential crash in SaltData (ConcurrentModificationException)
- Fix: Crash when Laser Block/Inventory Scanner ranges are set high
- Fix: Players are able to mount security cameras that have been shut down by an EMP from another mod
- Fix: A Portable Radar configured to not send repeating messages still repeats messages when multiple players are in its range
- Fix: Mine remote access tools automatically remove positions of mines that are no longer in the world from their list
- Fix: Some reinforced blocks can conduct redstone while their vanilla counterpart cannot do so
- Fix: The behaviour of reinforced pistons sometimes deviates from vanilla piston behaviour in advanced redstone contraptions
- Fix: SecurityCraft's doors, trapdoors and fence gates are sometimes erroneously in their open state when placed down
- Fix: The Codebreaker's cooldown still applies to players in creative mode
- Fix: The Sentry is not immune to infested/oozing, causing unintentional side effects