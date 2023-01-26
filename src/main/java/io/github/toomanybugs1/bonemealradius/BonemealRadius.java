package io.github.toomanybugs1.bonemealradius;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BonemealRadius extends JavaPlugin implements Listener {
	
	int bonemealRadius;
	int hitRatio;
	int flowerRatio;
	
	@Override
    public void onEnable() {
		Bukkit.getPluginManager().registerEvents(this, this);
		
		this.saveDefaultConfig();
		
		this.bonemealRadius = this.getConfig().getInt("bonemeal-radius");
		this.hitRatio = this.getConfig().getInt("hit-ratio");
		this.flowerRatio = this.getConfig().getInt("flower-ratio");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!cmd.getName().equalsIgnoreCase("bm") || args.length != 2)
            return false;

        if (sender instanceof Player && sender.hasPermission("bonemealradius.commands")) {
        	int newNumber;
        	
        	try {
        		newNumber = Integer.parseInt(args[1]);
        	}
        	catch (Exception e) {
        		sender.sendMessage("Radius or ratio must be a number.");
        		return false;
        	}
        	
        	if (args[0] == "hit") {
        		
        		if (newNumber > 100 || newNumber < 0) {
        			sender.sendMessage("Hit ratio must be a percentage between 0 and 100 ");
        			return false;
        		}
        			
        		this.hitRatio = newNumber;
        		this.getConfig().set("hit-ratio", this.hitRatio);
        		
        		sender.sendMessage("Set hit ratio to " + this.hitRatio);
        		
        		return true;
        	}
        	
        	if (args[0] == "flower") {
        		
        		if (newNumber > 100 || newNumber < 0) {
        			sender.sendMessage("Flower ratio must be a percentage between 0 and 100 ");
        			return false;
        		}
        			
        		this.flowerRatio = newNumber;
        		this.getConfig().set("flower-ratio", this.flowerRatio);
        		
        		sender.sendMessage("Set flower ratio to " + this.flowerRatio);
        		
        		return true;
        	}
        	
        	if (args[0] == "radius") {
        		if (newNumber < 0) {
        			sender.sendMessage("Radius must be a positive integer");
        			return false;
        		}
        		
        		this.bonemealRadius = newNumber;
        		this.getConfig().set("bonemeal-radius", this.bonemealRadius);
        		
        		sender.sendMessage("Set bonemeal radius to " + this.bonemealRadius);
        		return true;
        	}

        	return false;
        }
        else {
            sender.sendMessage("Only players with permissions can use this command.");
            return false;
        }
    }
    
    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {
    	if (event == null) {
    		return;
    	}
    	
    	Player player = event.getPlayer();
    	
    	if (player.getInventory().getItemInMainHand().getType() != Material.BONE_MEAL)
    		return;
    	
    	if (player.hasPermission("bonemealradius.use") ) {
    		applyBonemeal(event.getClickedBlock());
    		player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
    	}
    }
    
    private void applyBonemeal(Block center) {
    	Random rnd = new Random();
        int radius_squared = this.bonemealRadius * this.bonemealRadius;
        Block toHandle;
        
        for (int x = -this.bonemealRadius; x <= this.bonemealRadius; x++) {
            for (int z = -this.bonemealRadius; z <= this.bonemealRadius; z++) {
                toHandle =  center.getWorld().getHighestBlockAt(center.getX() + x, center.getZ() + z);
                
                if (toHandle.getRelative(BlockFace.DOWN).getType() == Material.GRASS) { // Block beneath is grass
                    if (center.getLocation().distanceSquared(toHandle.getLocation()) <= radius_squared) { // Block is in radius
                        if (rnd.nextInt(100) < this.hitRatio) { 
                        	if (rnd.nextInt(100) < this.flowerRatio) {
                        		Material newFlower = applyFlower(toHandle.getBiome());
                        		if (newFlower != null) {
                        			toHandle.setType(newFlower);
                                    toHandle.setBlockData(null);
                        		}
                        		else {
                        			toHandle.setType(Material.GRASS);
                        		}
                        	}
                        	else {
                        		toHandle.setType(Material.GRASS);
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
    			return getFlower(null);
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
}
