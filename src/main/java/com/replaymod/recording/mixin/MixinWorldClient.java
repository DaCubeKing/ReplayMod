package com.replaymod.recording.mixin;

//#if MC>=10904
import com.replaymod.recording.handler.RecordingEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC>=11300
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.storage.WorldSavedDataStorage;
//#else
//$$ import net.minecraft.world.WorldProvider;
//#endif


@Mixin(WorldClient.class)
public abstract class MixinWorldClient extends World implements RecordingEventHandler.RecordingEventSender {
    @Shadow
    private Minecraft mc;

    protected MixinWorldClient(ISaveHandler saveHandlerIn,
                               //#if MC>=11300
                               WorldSavedDataStorage mapStorage,
                               //#endif
                               WorldInfo info,
                               //#if MC>=11300
                               Dimension providerIn,
                               //#else
                               //$$ WorldProvider providerIn,
                               //#endif
                               Profiler profilerIn, boolean client) {
        super(saveHandlerIn,
                //#if MC>=11300
                mapStorage,
                //#endif
                info, providerIn, profilerIn, client);
    }

    private RecordingEventHandler replayModRecording_getRecordingEventHandler() {
        return ((RecordingEventHandler.RecordingEventSender) mc.renderGlobal).getRecordingEventHandler();
    }

    // Sounds that are emitted by thePlayer no longer take the long way over the server
    // but are instead played directly by the client. The server only sends these sounds to
    // other clients so we have to record them manually.
    // E.g. Block place sounds
    @Inject(method = "playSound(Lnet/minecraft/entity/player/EntityPlayer;DDDLnet/minecraft/util/SoundEvent;Lnet/minecraft/util/SoundCategory;FF)V",
            at = @At("HEAD"))
    public void replayModRecording_recordClientSound(EntityPlayer player, double x, double y, double z, SoundEvent sound, SoundCategory category,
                          float volume, float pitch, CallbackInfo ci) {
        if (player == mc.player) {
            RecordingEventHandler handler = replayModRecording_getRecordingEventHandler();
            if (handler != null) {
                handler.onClientSound(sound, category, x, y, z, volume, pitch);
            }
        }
    }

    // Same goes for level events (also called effects). E.g. door open, block break, etc.
    // These are handled in the World class, so we override the method in WorldClient and add our special handling.
    @Override
    public void playEvent(EntityPlayer player, int type, BlockPos pos, int data) {
        if (player == mc.player) {
            // We caused this event, the server won't send it to us
            RecordingEventHandler handler = replayModRecording_getRecordingEventHandler();
            if (handler != null) {
                handler.onClientEffect(type, pos, data);
            }
        }
        super.playEvent(player, type, pos, data);
    }
}
//#endif
