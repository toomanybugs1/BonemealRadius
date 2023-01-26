# BonemealRadius

A simple plugin for Bukkit that allows users to set the radius in which bonemeal fertilizes grass. 

## Commands
- /bm hit `<ratio>` : Sets the percentage of blocks in the radius that will grow grass or flowers (ratio must be between 0 and 100)
- /bm flower `<ratio>` : Sets the percentage of blocks that will grow flowers instead of grass (ratio must be between 0 and 100)
- /bm radius `<radius>` : Sets the total radius of the bonemeal effect area

## Permissions
- bonemealradius.* : Gives all permissions
- bonemealradius.use : Applies modified radius and ratios when bonemeal is used
- bonemealradius.commands : Gives access to commands that modify the radius and ratios

If a user does not have the bonemealradius.use permission, their bonemeal will work as it does in vanilla.

This is a pretty simple plugin with room to grow. Open an issue here if you have any suggestions.
