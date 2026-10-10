package dev.raybert.garrison;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod(GarrisonMod.ID)
public final class GarrisonMod {
    public static final String ID = "garrison";
    public GarrisonMod() {
        GarrisonNetwork.register();
        MinecraftForge.EVENT_BUS.register(new GarrisonController());
    }
}
