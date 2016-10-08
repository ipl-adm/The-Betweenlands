package thebetweenlands.common.world;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thebetweenlands.client.event.handler.FogHandler;
import thebetweenlands.client.render.shader.ShaderHelper;
import thebetweenlands.client.render.sky.BLSkyRenderer;
import thebetweenlands.common.TheBetweenlands;
import thebetweenlands.common.registries.BlockRegistry;
import thebetweenlands.common.world.biome.BiomeBetweenlands;
import thebetweenlands.common.world.event.EnvironmentEventRegistry;
import thebetweenlands.common.world.gen.ChunkGeneratorBetweenlands;
import thebetweenlands.common.world.gen.biome.BiomeProviderBetweenlands;
import thebetweenlands.common.world.storage.world.global.BetweenlandsWorldData;
import thebetweenlands.common.world.storage.world.shared.location.LocationAmbience;
import thebetweenlands.common.world.storage.world.shared.location.LocationStorage;
import thebetweenlands.util.config.ConfigHandler;

/**
 *
 * @author A long time ago in a galaxy far, far away: The Erebus Team
 *
 */
public class WorldProviderBetweenlands extends WorldProvider {
	public static final int LAYER_HEIGHT = 120;

	public static final int CAVE_WATER_HEIGHT = 15;

	public static final int PITSTONE_HEIGHT = CAVE_WATER_HEIGHT + 30;

	public static final int CAVE_START = LAYER_HEIGHT - 10;

	public float[] originalLightBrightnessTable = new float[16];

	@SideOnly(Side.CLIENT)
	private float[] currentFogColor;

	@SideOnly(Side.CLIENT)
	private float[] lastFogColor;

	private boolean allowHostiles, allowAnimals;
	private BetweenlandsWorldData worldData;

	public WorldProviderBetweenlands() {
		this.hasNoSky = true;
	}

	/**
	 * Returns a WorldProviderBetweenlands instance if world is not null and world#provider is an instance of WorldProviderBetweenlands
	 *
	 * @param world
	 */
	public static final WorldProviderBetweenlands getProvider(World world) {
		if (world != null && world.provider instanceof WorldProviderBetweenlands) {
			return (WorldProviderBetweenlands) world.provider;
		}
		return null;
	}

	@Override
	public boolean canRespawnHere() {
		return true;
	}

	@Override
	public float calculateCelestialAngle(long worldTime, float partialTickTime) {
		return 0.35F;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getSunBrightness(float partialTicks) {
		return 0.2F;
	}

	@Override
	public IChunkGenerator createChunkGenerator() {
		return new ChunkGeneratorBetweenlands(this.worldObj, this.worldObj.getSeed(), BlockRegistry.BETWEENSTONE, /*Blocks.WATER*/BlockRegistry.SWAMP_WATER, LAYER_HEIGHT);
	}

	@Override
	protected void generateLightBrightnessTable() {
		float minBrightness = (float) (1.0F / 10000000.0F * Math.pow(ConfigHandler.dimensionBrightness, 3.2F) + 0.002F);
		for(int i = 0; i <= 15; i++) {
			float f1 = 1F - (i*i) / (15F*15F);
			this.lightBrightnessTable[i] = ((1F - f1) / (f1 * 6F + 1F) * (1F - minBrightness) + minBrightness);
		}
		System.arraycopy(this.lightBrightnessTable, 0, this.originalLightBrightnessTable, 0, 16);
	}

	@Override
	public void createBiomeProvider() {
		this.setDimension(ConfigHandler.dimensionId);
		this.biomeProvider = new BiomeProviderBetweenlands(this.worldObj.getWorldInfo());
	}

	@Override
	public boolean isSurfaceWorld() {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean doesXZShowFog(int x, int z) {
		return false;
	}

	@Override
	public void setAllowedSpawnTypes(boolean allowHostiles, boolean allowAnimals) {
		super.setAllowedSpawnTypes(allowHostiles, allowAnimals);
		//TODO: This only seems to work on the client side...
		this.allowHostiles = allowHostiles;
		this.allowAnimals = allowAnimals;
	}

	public boolean getCanSpawnHostiles() {
		//TODO: See setAllowedSpawnTypes
		return /*this.allowHostiles*/this.worldObj.getDifficulty() != EnumDifficulty.PEACEFUL;
	}

	public boolean getCanSpawnAnimals() {
		//TODO: See setAllowedSpawnTypes
		return /*this.allowAnimals*/true;
	}

	@Override
	public BlockPos getRandomizedSpawnPoint() {
		BlockPos spawnPos = worldObj.getSpawnPoint();

		boolean isAdventure = worldObj.getWorldInfo().getGameType() == GameType.ADVENTURE;
		int spawnFuzz = 100;
		int spawnFuzzHalf = spawnFuzz / 2;

		if(!isAdventure) {
			spawnPos = spawnPos.add(worldObj.rand.nextInt(spawnFuzz) - spawnFuzzHalf, 0, worldObj.rand.nextInt(spawnFuzz) - spawnFuzzHalf);
			spawnPos = worldObj.getTopSolidOrLiquidBlock(spawnPos);
		}

		return spawnPos;
	}

	public BetweenlandsWorldData getWorldData() {
		if(this.worldData == null) {
			this.worldData = BetweenlandsWorldData.forWorld(this.worldObj);
		}
		return this.worldData;
	}

	@Override
	public boolean canDoRainSnowIce(Chunk chunk) {
		return false;
	}

	@Override
	public void updateWeather() {
		EnvironmentEventRegistry eeRegistry = this.getWorldData().getEnvironmentEventRegistry();
		this.worldObj.getWorldInfo().setRaining(eeRegistry.HEAVY_RAIN.isActive());
		this.worldObj.getWorldInfo().setThundering(false);
		this.worldObj.prevRainingStrength = this.worldObj.rainingStrength;
		if(!this.worldObj.isRemote) {
			float rainingStrength = this.worldObj.rainingStrength;
			if(eeRegistry.HEAVY_RAIN.isActive()) {
				if (rainingStrength < 0.5F) {
					rainingStrength += 0.0125F;
				}
				if (rainingStrength > 0.5F) {
					rainingStrength = 0.5F;
				}
			} else {
				if (rainingStrength > 0) {
					rainingStrength -= 0.0125F;
				}
				if (rainingStrength < 0) {
					rainingStrength = 0;
				}
			}
			this.worldObj.rainingStrength = rainingStrength;
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public Vec3d getFogColor(float celestialAngle, float partialTickTime) {
		if(this.currentFogColor == null || this.lastFogColor == null)
			this.initFogColors(Minecraft.getMinecraft().thePlayer);

		double r = this.currentFogColor[0] + (this.currentFogColor[0] - this.lastFogColor[0]) * partialTickTime;
		double g = this.currentFogColor[1] + (this.currentFogColor[1] - this.lastFogColor[1]) * partialTickTime;
		double b = this.currentFogColor[2] + (this.currentFogColor[2] - this.lastFogColor[2]) * partialTickTime;
		return new Vec3d(r / 255D, g / 255D, b / 255D);
	}

	private int[] getTargetFogColor(EntityPlayer player) {
		Biome biome = this.worldObj.getBiomeGenForCoords(player.getPosition());
		int[] targetFogColor;

		if(biome instanceof BiomeBetweenlands) {
			((BiomeBetweenlands) biome).updateFog();
			targetFogColor = ((BiomeBetweenlands) biome).getFogRGB().clone();
		} else {
			targetFogColor = new int[]{(int) 255, (int) 255, (int) 255};
		}

		//Change fog color if shaders are not available
		if(!ShaderHelper.INSTANCE.isWorldShaderActive()) {
			if(WorldProviderBetweenlands.getProvider(this.worldObj).getEnvironmentEventRegistry().BLOODSKY.isActive()) {
				targetFogColor = new int[] {(int) (0.74F * 255), (int) (0.18F * 255), (int) (0.08F * 255)};
			} else if(WorldProviderBetweenlands.getProvider(this.worldObj).getEnvironmentEventRegistry().SPOOPY.isActive()) {
				targetFogColor = new int[] {(int) (0.4F * 255), (int) (0.22F * 255), (int) (0.08F * 255)};
			}
		}

		return targetFogColor;
	}

	private void initFogColors(EntityPlayer player) {
		int[] targetFogColor = this.getTargetFogColor(player);

		if(this.currentFogColor == null) {
			this.currentFogColor = new float[3];
			for(int i = 0; i < 3; i++) {
				this.currentFogColor[i] = targetFogColor[i];
			}
		}

		if(this.lastFogColor == null) {
			this.lastFogColor = new float[3];
		} else {
			for(int i = 0; i < 3; i++) {
				this.lastFogColor[i] = this.currentFogColor[i];
			}
		}
	}

	/**
	 * Updates the fog colors every tick
	 */
	public void updateFogColors() {
		EntityPlayer player = TheBetweenlands.proxy.getClientPlayer();

		if(player == null) return;

		this.initFogColors(player);

		int[] targetFogColor = this.getTargetFogColor(player);

		final int transitionStart = WorldProviderBetweenlands.CAVE_START;
		final int transitionEnd = WorldProviderBetweenlands.CAVE_START - 15;
		float m = 0;
		if(FogHandler.hasDenseFog()) {
			float y = (float) player.posY;
			if (y < transitionStart) {
				if (transitionEnd < y) {
					m = (y - transitionEnd) / (transitionStart - transitionEnd) * 80;
				}
			} else {
				m = 80;
			}
		}

		LocationAmbience ambience = LocationStorage.getAmbience(player);

		if(ambience != null && ambience.hasFogBrightness()) {
			m = ambience.getFogBrightness();
		}

		if(!ShaderHelper.INSTANCE.isWorldShaderActive()) {
			if(WorldProviderBetweenlands.getProvider(this.worldObj).getEnvironmentEventRegistry().BLOODSKY.isActive()
					|| WorldProviderBetweenlands.getProvider(this.worldObj).getEnvironmentEventRegistry().SPOOPY.isActive()) {
				m = 0;
			}
		}

		if(ambience != null && ambience.hasFogColor()) {
			for(int i = 0; i < 3; i++) {
				int diff = 255 - ambience.getFogColor()[i];
				targetFogColor[i] = (int) (ambience.getFogColor()[i] + (diff / 255.0D * m));
			}
		} else {
			for(int i = 0; i < 3; i++) {
				int diff = 255 - targetFogColor[i];
				targetFogColor[i] = (int) (targetFogColor[i] + (diff / 255.0D * m));
			}
		}

		for(int a = 0; a < 3; a++) {
			this.lastFogColor[a] = this.currentFogColor[a];
		}

		for(int a = 0; a < 3; a++) {
			if(this.currentFogColor[a] != targetFogColor[a]) {
				if(this.currentFogColor[a] < targetFogColor[a]) {
					this.currentFogColor[a] += 0.2F;
					if(this.currentFogColor[a] > targetFogColor[a]) {
						this.currentFogColor[a] = targetFogColor[a];
					}
				} else if(this.currentFogColor[a] > targetFogColor[a]) {
					this.currentFogColor[a] -= 0.2F;
					if(this.currentFogColor[a] < targetFogColor[a]) {
						this.currentFogColor[a] = targetFogColor[a];
					}
				}
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IRenderHandler getSkyRenderer() {
		return BLSkyRenderer.INSTANCE;
	}

	//Fix for buggy rain (?)
	@Override
	public void calculateInitialWeather() {
		EnvironmentEventRegistry eeRegistry = this.getWorldData().getEnvironmentEventRegistry();
		this.worldObj.getWorldInfo().setRaining(eeRegistry.HEAVY_RAIN.isActive());
		this.worldObj.getWorldInfo().setThundering(false);
		super.calculateInitialWeather();
	}

	public EnvironmentEventRegistry getEnvironmentEventRegistry() {
		return this.getWorldData().getEnvironmentEventRegistry();
	}

	@Override
	public DimensionType getDimensionType() {
		return TheBetweenlands.dimensionType;
	}
}
