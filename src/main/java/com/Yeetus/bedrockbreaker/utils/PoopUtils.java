package com.Yeetus.bedrockbreaker.utils;

import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import static meteordevelopment.meteorclient.MeteorClient.mc;
import static meteordevelopment.meteorclient.utils.player.ChatUtils.info;

public class PoopUtils {

    public static void PistonPlace(Direction dir, BlockPos pos) {
        if (dir.equals(Direction.UP) || dir.equals(Direction.DOWN)){
            float pitch = switch (dir) {
            case UP -> 90f;
            case DOWN -> -90f;
            default -> 90f;
            };
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(mc.player.getYaw(1.0f), pitch, mc.player.isOnGround()));
        } else {
            float yaw = switch (dir){
                case NORTH -> 0.0f;
                case EAST -> 90.0f;
                case WEST -> -90.0f;
                default -> -180.0f;
            };
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(yaw,mc.player.getPitch(1.0f),mc.player.isOnGround()));
        }
        place(pos);
    }

    public static void place(BlockPos pos){
        if (pos == null) {
            info("couldn't place " + pos);
            return;
        }
        BlockHitResult hitResult = new BlockHitResult(new Vec3d(pos.getX(), pos.getY(), pos.getZ()), Direction.UP, pos, false);
        mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, hitResult, 0));
    }

}
