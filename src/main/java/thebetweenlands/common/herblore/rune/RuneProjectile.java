package thebetweenlands.common.herblore.rune;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import thebetweenlands.api.rune.INodeComposition;
import thebetweenlands.api.rune.INodeConfiguration;
import thebetweenlands.api.rune.IRuneChainUser;
import thebetweenlands.api.rune.impl.AbstractRune;
import thebetweenlands.api.rune.impl.InputSerializers;
import thebetweenlands.api.rune.impl.RuneChainComposition.RuneExecutionContext;
import thebetweenlands.api.rune.impl.RuneConfiguration;
import thebetweenlands.api.rune.impl.RuneConfiguration.InputPort;
import thebetweenlands.api.rune.impl.RuneConfiguration.OutputPort;
import thebetweenlands.api.rune.impl.RuneEffectModifier;
import thebetweenlands.api.rune.impl.RuneStats;
import thebetweenlands.api.rune.impl.RuneTokenDescriptors;
import thebetweenlands.common.entity.EntityRunicBeetleProjectile;
import thebetweenlands.common.registries.AspectRegistry;

public final class RuneProjectile extends AbstractRune<RuneProjectile> {

	public static final class Blueprint extends AbstractRune.Blueprint<RuneProjectile> {
		public Blueprint() {
			super(RuneStats.builder()
					.aspect(AspectRegistry.ORDANIIS, 1)
					.duration(5.0f)
					.build());

			this.setRecursiveRuneEffectModifierCount(3);
		}

		public static final RuneConfiguration CONFIGURATION_1;

		private static final InputPort<?> IN_ENTITY_1;
		private static final OutputPort<BlockPos> OUT_POSITION_1;
		private static final OutputPort<Entity> OUT_ENTITY_1;

		public static final RuneConfiguration CONFIGURATION_2;

		private static final InputPort<?> IN_ENTITY_2;
		private static final InputPort<Vec3d> IN_RAY_2;
		private static final OutputPort<BlockPos> OUT_POSITION_2;
		private static final OutputPort<Entity> OUT_ENTITY_2;

		public static final RuneConfiguration CONFIGURATION_3;

		private static final InputPort<Vec3d> IN_POSITION_3;
		private static final InputPort<Vec3d> IN_RAY_3;
		private static final OutputPort<BlockPos> OUT_POSITION_3;
		private static final OutputPort<Entity> OUT_ENTITY_3;

		public static final RuneConfiguration CONFIGURATION_4;

		private static final InputPort<BlockPos> IN_BLOCK_4;
		private static final InputPort<Vec3d> IN_RAY_4;
		private static final OutputPort<BlockPos> OUT_POSITION_4;
		private static final OutputPort<Entity> OUT_ENTITY_4;

		static {
			RuneConfiguration.Builder builder = RuneConfiguration.builder();

			IN_ENTITY_1 = builder.in(RuneTokenDescriptors.ENTITY, InputSerializers.USER, Entity.class, IRuneChainUser.class);
			OUT_POSITION_1 = builder.out(RuneTokenDescriptors.BLOCK, BlockPos.class);
			OUT_ENTITY_1 = builder.out(RuneTokenDescriptors.ENTITY, Entity.class);

			CONFIGURATION_1 = builder.build();

			IN_ENTITY_2 = builder.in(RuneTokenDescriptors.ENTITY, InputSerializers.USER, Entity.class, IRuneChainUser.class);
			IN_RAY_2 = builder.in(RuneTokenDescriptors.DIRECTION, InputSerializers.VECTOR, Vec3d.class);
			OUT_POSITION_2 = builder.out(RuneTokenDescriptors.BLOCK, BlockPos.class);
			OUT_ENTITY_2 = builder.out(RuneTokenDescriptors.ENTITY, Entity.class);

			CONFIGURATION_2 = builder.build();

			IN_POSITION_3 = builder.in(RuneTokenDescriptors.POSITION, InputSerializers.VECTOR, Vec3d.class);
			IN_RAY_3 = builder.in(RuneTokenDescriptors.DIRECTION, InputSerializers.VECTOR, Vec3d.class);
			OUT_POSITION_3 = builder.out(RuneTokenDescriptors.BLOCK, BlockPos.class);
			OUT_ENTITY_3 = builder.out(RuneTokenDescriptors.ENTITY, Entity.class);

			CONFIGURATION_3 = builder.build();

			IN_BLOCK_4 = builder.in(RuneTokenDescriptors.BLOCK, InputSerializers.BLOCK, BlockPos.class);
			IN_RAY_4 = builder.in(RuneTokenDescriptors.DIRECTION, InputSerializers.VECTOR, Vec3d.class);
			OUT_POSITION_4 = builder.out(RuneTokenDescriptors.BLOCK, BlockPos.class);
			OUT_ENTITY_4 = builder.out(RuneTokenDescriptors.ENTITY, Entity.class);

			CONFIGURATION_4 = builder.build();
		}

		@Override
		public List<RuneConfiguration> getConfigurations(IConfigurationLinkAccess linkAccess, boolean provisional) {
			return ImmutableList.of(CONFIGURATION_1, CONFIGURATION_2, CONFIGURATION_3, CONFIGURATION_4);
		}

		@Override
		public RuneProjectile create(int index, INodeComposition<RuneExecutionContext> composition, INodeConfiguration configuration) {
			return new RuneProjectile(this, index, composition, (RuneConfiguration) configuration);
		}

		@Override
		protected RuneEffectModifier.Subject activate(RuneProjectile state, RuneExecutionContext context, INodeIO io) {
			EntityRunicBeetleProjectile projectile = null;

			if(state.getConfiguration() == CONFIGURATION_1) {
				projectile = IN_ENTITY_1.run(io, Entity.class, entity -> {
					return run(io, context.getUser().getWorld(), entity instanceof EntityLivingBase ? (EntityLivingBase) entity : null, entity.getPositionEyes(1), entity.getLookVec(), OUT_POSITION_1, OUT_ENTITY_1);
				}, projectile);
				projectile = IN_ENTITY_1.run(io, IRuneChainUser.class, user -> {
					return run(io, context.getUser().getWorld(), user.getEntity() instanceof EntityLivingBase ? (EntityLivingBase) user.getEntity() : null, user.getEyesPosition(), user.getLook(), OUT_POSITION_1, OUT_ENTITY_1);
				}, projectile);
			} else if(state.getConfiguration() == CONFIGURATION_2) {
				projectile = IN_ENTITY_2.run(io, Entity.class, entity -> {
					return run(io, context.getUser().getWorld(), entity instanceof EntityLivingBase ? (EntityLivingBase) entity : null, entity.getPositionEyes(1), IN_RAY_2.get(io), OUT_POSITION_2, OUT_ENTITY_2);
				}, projectile);
				projectile = IN_ENTITY_2.run(io, IRuneChainUser.class, user -> {
					return run(io, context.getUser().getWorld(), user.getEntity() instanceof EntityLivingBase ? (EntityLivingBase) user.getEntity() : null, user.getEyesPosition(), IN_RAY_2.get(io), OUT_POSITION_2, OUT_ENTITY_2);
				}, projectile);
			} else if(state.getConfiguration() == CONFIGURATION_3) {
				projectile = run(io, context.getUser().getWorld(), null, IN_POSITION_3.get(io), IN_RAY_3.get(io), OUT_POSITION_3, OUT_ENTITY_3);
			} else if(state.getConfiguration() == CONFIGURATION_4) {
				BlockPos block = IN_BLOCK_4.get(io);
				projectile = run(io, context.getUser().getWorld(), null, new Vec3d(block.getX() + 0.5f, block.getY() + 0.5f, block.getZ() + 0.5f), IN_RAY_4.get(io), OUT_POSITION_4, OUT_ENTITY_4);
			}

			if(projectile != null) {
				return new RuneEffectModifier.Subject(projectile);
			}

			return null;
		}

		private EntityRunicBeetleProjectile run(INodeIO io, World world, EntityLivingBase thrower, Vec3d pos, Vec3d dir, OutputPort<BlockPos> outPos, OutputPort<Entity> outEntity) {
			EntityRunicBeetleProjectile projectile;
			if(thrower != null) {
				projectile = new EntityRunicBeetleProjectile(world, thrower);
				projectile.setPosition(pos.x, pos.y, pos.z);
			} else {
				projectile = new EntityRunicBeetleProjectile(world, pos.x, pos.y, pos.z);
			}

			projectile.shoot(dir.x, dir.y, dir.z, 1, 0);

			world.spawnEntity(projectile);

			io.schedule(scheduler -> {
				if(projectile.isEntityAlive()) {
					scheduler.sleep(1);
				} else {
					outPos.set(io, projectile.getHitBlock());
					outEntity.set(io, projectile.getHitEntity());
					scheduler.terminate();
				}
			});

			return projectile;
		}

		@Override
		protected RuneEffectModifier createRuneEffectModifier(RuneProjectile state) {
			return new RuneEffectModifier() {
				private List<Pair<AbstractRune<?>, Subject>> projectiles = new ArrayList<>();

				@Override
				public boolean activate(AbstractRune<?> rune, IRuneChainUser user, Subject subject) {
					if(subject != null && subject.getEntity() instanceof EntityRunicBeetleProjectile) {
						if(user.getWorld().isRemote) {
							this.projectiles.add(Pair.of(rune, subject));
						}
						return true;
					}
					return false;
				}

				@Override
				public void update() {
					for(Pair<AbstractRune<?>, Subject> projectile : this.projectiles) {
						RuneEffectModifier modifier = projectile.getLeft().getRuneEffectModifier();
						Subject subject = projectile.getRight();
						EntityRunicBeetleProjectile entity = (EntityRunicBeetleProjectile) subject.getEntity();
						
						if(modifier != null) {
							entity.setVisualModifier(Pair.of(modifier, subject));
						} else {
							entity.setVisualModifier(null);
						}
					}
				}
			};
		}
	}

	private RuneProjectile(Blueprint blueprint, int index, INodeComposition<RuneExecutionContext> composition, RuneConfiguration configuration) {
		super(blueprint, index, composition, configuration);
	}
}
