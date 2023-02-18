package io.github.toomanybugs1.bonemealradius;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BonemealRadius extends JavaPlugin implements Listener {
	
	HashMap<String, List<Integer>> playerSettings;
	int defaultRadius = 0;
	int defaultFlowerRatio = 30;
	int defaultHitRatio = 66;

	int doubleGrassRatio = 15;
	
	@Override
    public void onEnable() {
		Bukkit.getPluginManager().registerEvents(this, this);
		
		this.saveDefaultConfig();
		
		this.playerSettings = loadSettings();
		
		if (this.playerSettings == null || !(this.playerSettings instanceof HashMap)) {
			this.playerSettings = new HashMap<String, List<Integer>>();
		}
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!cmd.getName().equalsIgnoreCase("bm") || args.length != 2)
            return false;

        if (sender instanceof Player && sender.hasPermission("bonemealradius.commands")) {
        	int newNumber;
        	List<Integer> playerValues = this.playerSettings.get(sender.getName());
        	
        	try {
        		newNumber = Integer.parseInt(args[1]);
        	}
        	catch (Exception e) {
        		sender.sendMessage(ChatColor.GOLD + "[BonemealRadius] " + ChatColor.RED + "Radius or ratio must be a number.");
        		return false;
        	}
        	
        	if (playerValues == null || playerValues.size() != 3) 
        		playerValues = Arrays.asList(new Integer[] {this.defaultRadius, this.defaultFlowerRatio, this.defaultHitRatio});
        	
        	if (args[0].equalsIgnoreCase("hit")) {
        		
        		if (newNumber > 100 || newNumber < 0) {
        			sender.sendMessage(ChatColor.GOLD + "[BonemealRadius] " + ChatColor.RED + "Hit ratio must be a percentage between 0 and 100 ");
        			return false;
        		}
        			
        		playerValues.set(2, newNumber);
        		this.playerSettings.put(sender.getName(), playerValues);
        		this.saveSettings();
        		
        		sender.sendMessage(ChatColor.GOLD + "[BonemealRadius] " + ChatColor.WHITE + "Set hit ratio to " + playerValues.get(2));
        		
        		return true;
        	}
        	
        	if (args[0].equalsIgnoreCase("flower")) {
        		
        		if (newNumber > 100 || newNumber < 0) {
        			sender.sendMessage(ChatColor.GOLD + "[BonemealRadius] " + ChatColor.RED + "Flower ratio must be a percentage between 0 and 100 ");
        			return false;
        		}
        			
        		playerValues.set(1, newNumber);
        		this.playerSettings.put(sender.getName(), playerValues);
        		this.saveSettings();
        		
        		sender.sendMessage(ChatColor.GOLD + "[BonemealRadius] " + ChatColor.WHITE + "Set flower ratio to " + playerValues.get(1));
        		
        		return true;
        	}
        	
        	if (args[0].equalsIgnoreCase("radius")) {
        		if (newNumber < 0) {
        			sender.sendMessage(ChatColor.GOLD + "[BonemealRadius] " + ChatColor.RED + "Radius must be a positive integer");
        			return false;
        		}

				if (newNumber > 20)
					sender.sendMessage(ChatColor.GOLD + "[BonemealRadius] " + ChatColor.RED + "Warning: A large radius can cause server lag. Proceed with caution.");

				playerValues.set(0, newNumber);
        		this.playerSettings.put(sender.getName(), playerValues);
        		this.saveSettings();
        		
        		sender.sendMessage(ChatColor.GOLD + "[BonemealRadius] " + ChatColor.WHITE + "Set bonemeal radius to " + playerValues.get(0));
        		return true;
        	}

        	return false;
        }
        else {
            sender.sendMessage(ChatColor.GOLD + "[BonemealRadius] " + ChatColor.RED + "Only players with permissions can use this command.");
            return false;
        }
    }
    
    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {
    	if (event == null)
    		return;

		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
    	
    	Player player = event.getPlayer();
    	
    	if (player.getInventory().getItemInMainHand().getType() != Material.BONE_MEAL)
    		return;
    	
    	if (player.hasPermission("bonemealradius.use") && event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.GRASS_BLOCK) {
			List<Integer> playerValues = this.playerSettings.get(player.getName());
			if (playerValues == null)
				playerValues = Arrays.asList(new Integer[] {this.defaultRadius, this.defaultFlowerRatio, this.defaultHitRatio});

			// if radius is set to 0, apply bonemeal as normal
			if (playerValues.get(0) == 0)
				return;

			event.setCancelled(true);
    		applyBonemeal(event.getClickedBlock(), player, playerValues);
    		player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
    	}
    }
    
    private void applyBonemeal(Block center, Player player, List<Integer> playerValues) {

    	int bonemealRadius = playerValues.get(0);
    	int flowerRatio = playerValues.get(1);
    	int hitRatio = playerValues.get(2);
    	
    	Random rnd = new Random();
        int radius_squared = bonemealRadius * bonemealRadius;
        Block toHandle;
        
        for (int x = -bonemealRadius; x <= bonemealRadius; x++) {
            for (int z = -bonemealRadius; z <= bonemealRadius; z++) {
                toHandle =  getRelativeHighest(center.getX() + x, center.getY(), center.getZ() + z, center.getWorld());
                if (toHandle.getRelative(BlockFace.DOWN).getType() == Material.GRASS_BLOCK) { // Block beneath is grass
                    if (center.getLocation().distanceSquared(toHandle.getLocation()) <= radius_squared) { // Block is in radius
                    	if (rnd.nextInt(100) <= hitRatio) {
                        	if (rnd.nextInt(100) <= flowerRatio) {
                        		Material newFlower = applyFlower(toHandle.getBiome());
                        		if (newFlower != null) {
                        			toHandle.setType(newFlower);
                                    toHandle.setBlockData(newFlower.createBlockData());
                        		}
                        		else {
									if (rnd.nextInt(100) <= doubleGrassRatio && toHandle.getRelative(BlockFace.UP).getType() == Material.AIR) {
										placeDoubleGrass(toHandle);
									}
									else {
										toHandle.setType(Material.GRASS);
										toHandle.setBlockData(Material.GRASS.createBlockData());
									}
                        		}
                        	}
                        	else {
								if (rnd.nextInt(100) <= doubleGrassRatio && toHandle.getRelative(BlockFace.UP).getType() == Material.AIR) {
									placeDoubleGrass(toHandle);
								}
								else {
									toHandle.setType(Material.GRASS);
									toHandle.setBlockData(Material.GRASS.createBlockData());
								}
                        	}
                        }
                    }
                }
            }
        }
    }
    
    // generates a flower based on the biome
    private Material applyFlower(Biome biome) {
    	switch (biome) {
    		case PLAINS:
    		case SUNFLOWER_PLAINS:
    			return getFlower(new Material[] {Material.DANDELION, Material.POPPY, Material.AZURE_BLUET, Material.ORANGE_TULIP, Material.RED_TULIP, Material.PINK_TULIP, Material.WHITE_TULIP});
    		case DRIPSTONE_CAVES:
    		case DEEP_DARK:
    			return getFlower(new Material[] {Material.DANDELION, Material.POPPY, Material.AZURE_BLUET, Material.ORANGE_TULIP, Material.RED_TULIP, Material.PINK_TULIP, Material.WHITE_TULIP, Material.OXEYE_DAISY, Material.CORNFLOWER});
    		case SWAMP:
    			return getFlower(new Material[] {Material.BLUE_ORCHID});
    		case FLOWER_FOREST:
    			return getFlower(new Material[] {Material.DANDELION, Material.POPPY, Material.ALLIUM, Material.AZURE_BLUET, Material.ORANGE_TULIP, Material.RED_TULIP, Material.PINK_TULIP, Material.WHITE_TULIP, Material.OXEYE_DAISY, Material.CORNFLOWER, Material.LILY_OF_THE_VALLEY});
    		case MEADOW: 
    			return getFlower(new Material[] {Material.DANDELION, Material.POPPY, Material.ALLIUM, Material.AZURE_BLUET, Material.OXEYE_DAISY, Material.CORNFLOWER});
    		case BADLANDS:
    		case ERODED_BADLANDS:
    		case WOODED_BADLANDS:
    		case MUSHROOM_FIELDS:
    		case GROVE:
    		case SNOWY_SLOPES:
    		case FROZEN_PEAKS:
    		case JAGGED_PEAKS:
    		case STONY_PEAKS:
    		case LUSH_CAVES:
    		case MANGROVE_SWAMP:
    		case THE_VOID:
    		case WARPED_FOREST:
    		case BASALT_DELTAS:
    		case SOUL_SAND_VALLEY:
    		case THE_END:
    		case END_HIGHLANDS:
    		case END_MIDLANDS:
    		case END_BARRENS:
    		case SMALL_END_ISLANDS:
    			return null;
    		default:
    			return getFlower(new Material[] {Material.DANDELION, Material.POPPY});
    			
    	}
    }
    
    // takes a list of flowers and returns a random one
    private Material getFlower(Material[] possibleFlowers) {
    	int len = possibleFlowers.length;
    	int randomInt = (int) (Math.random() * len);
    	
    	return possibleFlowers[randomInt];
    }
    
    private HashMap<String, List<Integer>> loadSettings() {
    	HashMap<String, List<Integer>> settings = new HashMap<String, List<Integer>>();
    	
    	if (this.getConfig().getConfigurationSection("HashMap") != null) {
	    	for (String key : this.getConfig().getConfigurationSection("HashMap").getKeys(false)) {
	    		settings.put(key, this.getConfig().getIntegerList("HashMap."+key));
			}
    	}

		return settings;
    }
    
    private void saveSettings() {
    	for (String key : this.playerSettings.keySet()) {
			if (this.getConfig().getConfigurationSection("HashMap") == null)
				this.getConfig().createSection("HashMap");

    		this.getConfig().getConfigurationSection("HashMap").set(key, this.playerSettings.get(key));
    	}
    	
    	this.saveConfig();
    }

	// get the nearest air block ABOVE a certain coordinate
	private Block getRelativeHighest(int relX, int relY, int relZ, World world) {
		Block curBlock = world.getBlockAt(relX, relY, relZ);

		// block is not air, so we must move up
		if (curBlock.getType() != Material.AIR) {
			while (curBlock.getType() != Material.AIR) {
				curBlock = world.getBlockAt(curBlock.getRelative(BlockFace.UP).getLocation());
			}

		}
		// block is air, so we must move down until the block below isn't air
		else {
			while (curBlock.getRelative(BlockFace.DOWN).getType() == Material.AIR) {
				curBlock = world.getBlockAt(curBlock.getRelative(BlockFace.DOWN).getLocation());
			}
		}
		return curBlock;
	}

	private void placeDoubleGrass(Block b) {
		b.getRelative(BlockFace.UP).setType(Material.TALL_GRASS, false);
		b.setType(Material.TALL_GRASS, false);

		Bisected dataUpper = (Bisected) b.getRelative(BlockFace.UP).getBlockData();
		dataUpper.setHalf(Bisected.Half.TOP);

		Bisected dataLower = (Bisected) b.getBlockData();
		dataLower.setHalf(Bisected.Half.BOTTOM);

		b.getRelative(BlockFace.UP).setBlockData(dataUpper);
		b.setBlockData(dataLower);
	}
}
