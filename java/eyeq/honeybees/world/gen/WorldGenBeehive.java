package eyeq.honeybees.world.gen;

import java.util.Random;

import eyeq.honeybees.HoneyBees;
import eyeq.util.world.gen.WorldGenDecorations;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldGenBeehive extends WorldGenDecorations {
    public WorldGenBeehive() {
        super(1.0F);
    }

    @Override
    public boolean generate(World world, Random rand, BlockPos pos) {
        for(int i = 0; i < 20; i++) {
            BlockPos blockPos = pos.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4), rand.nextInt(8) - rand.nextInt(8));
            Block block = world.getBlockState(blockPos.down()).getBlock();
            if(!isLeaves(block)) {
                continue;
            }
            while(isLeaves(world.getBlockState(blockPos.down()).getBlock())) {
                blockPos = blockPos.down();
            }
            blockPos = blockPos.down();
            if(world.getBlockState(blockPos).getBlock() != Blocks.AIR) {
                continue;
            }
            EnumFacing facing = null;
            for(EnumFacing horizontal : EnumFacing.HORIZONTALS) {
                if(isLog(world.getBlockState(blockPos.offset(horizontal)).getBlock())) {
                    facing = horizontal.getOpposite();
                    break;
                }
            }
            if(facing != null) {
                world.setBlockState(blockPos, HoneyBees.beehive.getDefaultState().withProperty(BlockDirectional.FACING, facing));
            }
        }
        return true;
    }

    protected boolean isLeaves(Block block) {
        return block instanceof BlockLeaves;
    }

    protected boolean isLog(Block block) {
        return block instanceof BlockLog;
    }
}
