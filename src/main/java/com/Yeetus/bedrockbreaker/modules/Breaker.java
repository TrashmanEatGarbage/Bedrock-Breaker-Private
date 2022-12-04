package com.Yeetus.bedrockbreaker.modules;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonBlock;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import com.Yeetus.bedrockbreaker.utils.PoopUtils;
import static net.minecraft.block.Block.sideCoversSmallSquare;

public class Breaker extends Module {

    public Breaker(){super(Categories.Misc,"BedrockBreaker","my custom version of the china bedrock breaker (requires haste 2)");}

    BlockPos blockpos, pistonpos, torchpos, supportpos;
    State state;
    FindItemResult piston,pickaxe,slime,torch;
    boolean placedsupport = false;

    @Override
    public void onActivate(){
        BlockHitResult hitresult = mc.crosshairTarget instanceof BlockHitResult ? (BlockHitResult) mc.crosshairTarget: null;
        if (hitresult != null && !mc.world.isAir(hitresult.getBlockPos())){
            blockpos = hitresult.getBlockPos();
            torchpos = findVerticalTorchPos(blockpos);
            pistonpos = blockpos.offset(Direction.UP);
        }
        placedsupport = false;
        state = State.notStarted;
    }

    @Override
    public void onDeactivate() {
        placedsupport = false;
        blockpos = null;
    }

    @EventHandler
    public void onTickPre(TickEvent.Pre event){
        piston = findItem(Items.PISTON);
        torch = findItem(Items.REDSTONE_TORCH);
        pickaxe = findItem(Items.NETHERITE_PICKAXE);
        slime = findItem(Items.SLIME_BLOCK);
    }


    @EventHandler
    public void onTickPost(TickEvent.Post event){
        switch (state) {
            //when it first activates it places a piston and torch to power it
            case notStarted:
                if (blockpos == null) return;
                if (torchpos == null && !placedsupport) {
                    supportpos = findSlimeSupportPos(blockpos);
                    move(slime, () -> PoopUtils.place(supportpos));
                    placedsupport = true;
                    return;
                } else if (placedsupport && torchpos == null) {
                    torchpos = findVerticalTorchPos(blockpos);
                    return;
                }
                move(piston, () -> PoopUtils.PistonPlace(Direction.UP,pistonpos));
                move(torch, () -> PoopUtils.place(torchpos));
                state = State.started;
                return;
            case started:
                if (mc.world.getBlockState(pistonpos).isOf(Blocks.PISTON)) {
                    if (!mc.world.getBlockState(pistonpos).get(PistonBlock.EXTENDED)) {
                        return;
                    }
                }else return;
                state = State.extended;
                //if the piston is extended it then breaks both the torch and piston, then places a new piston facing into the bedrock
            case extended:
                InvUtils.swap(pickaxe.slot(),true);
                mc.interactionManager.attackBlock(torchpos, Direction.UP);
                mc.interactionManager.attackBlock(pistonpos, Direction.UP);

                move(piston, () -> PoopUtils.PistonPlace(Direction.DOWN,pistonpos));
                state = State.broken;
                return;
            //breaks piston after bedrock is broken
            case broken:
                if (!mc.world.getBlockState(blockpos).isAir()) return;
                if (supportpos != null){
                    mc.interactionManager.attackBlock(supportpos,Direction.UP);
                }
                mc.interactionManager.attackBlock(pistonpos,Direction.UP);
                blockpos = null;
                toggle();
        }
    }


    @EventHandler
    private void onRender(Render3DEvent event){
        if (blockpos == null) return;
        event.renderer.box(blockpos, Color.RED,Color.RED, ShapeMode.Both,0);
        event.renderer.box(blockpos,Color.GREEN,Color.GREEN,ShapeMode.Lines, 0);
    }

    public static FindItemResult findItem(Item item) {
        return InvUtils.find(item);
    }

    //shamelessly stolen
    public void move(FindItemResult itemResult, Runnable runnable) {
        if (!itemResult.found()){
            info("no %s found in inventory" , itemResult.toString());
            return;
        }
        move(mc.player.getInventory().selectedSlot, itemResult.slot());
        runnable.run();
        move(mc.player.getInventory().selectedSlot, itemResult.slot());
    }
    private void move(int from, int to) {
        ScreenHandler handler = mc.player.currentScreenHandler;
        Int2ObjectArrayMap<ItemStack> stack = new Int2ObjectArrayMap<>();
        stack.put(to, handler.getSlot(to).getStack());
        mc.getNetworkHandler().sendPacket(new ClickSlotC2SPacket(handler.syncId, handler.getRevision(), PlayerInventory.MAIN_SIZE + from, to, SlotActionType.SWAP, handler.getCursorStack().copy(), stack));
    }

    public BlockPos findVerticalTorchPos(BlockPos blockpos) {
        for (Direction dir: Direction.values()){
            if (!dir.getAxis().isHorizontal()) continue;
            if ((sideCoversSmallSquare(mc.world , blockpos.offset(dir), Direction.UP) && (mc.world.getBlockState(blockpos.offset(dir).offset(Direction.UP)).getMaterial().isReplaceable())) ||
                mc.world.getBlockState(blockpos.offset(dir).offset(Direction.UP)).isOf(Blocks.REDSTONE_TORCH)){
                return blockpos.offset(dir).offset(Direction.UP);
            }
        }
        return null;
    }

    public BlockPos findSlimeSupportPos(BlockPos blockpos) {
        for (Direction dir: Direction.values()){
            if (!dir.getAxis().isHorizontal()) continue;
            if ((mc.world.getBlockState(blockpos.offset(dir)).getMaterial().isReplaceable())){
                return blockpos.offset(dir);
            }
        }
        return null;
    }

    enum State{
        notStarted,
        started,
        extended,
        broken,
    }
}

