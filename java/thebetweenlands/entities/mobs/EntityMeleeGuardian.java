package thebetweenlands.entities.mobs;

import net.minecraft.world.World;
import thebetweenlands.manual.ManualManager;

/**
 * Created by jnad325 on 7/14/15.
 */
public class EntityMeleeGuardian extends EntityTempleGuardian implements IEntityBL {
    public EntityMeleeGuardian(World worldObj)
    {
        super(worldObj);
    }
    
	@Override
	protected String getLivingSound() {
		int randomSound = rand.nextInt(3) + 1;
		return "thebetweenlands:templeGuardianMeleeLiving" + randomSound;
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		ManualManager.PlayerDiscoverPage(this, "meleeGuardian");
	}
}