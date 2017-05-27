package eyeq.honeybees.block;

import java.util.Random;

import eyeq.honeybees.HoneyBees;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockTorchHoney extends BlockTorch {
    public BlockTorchHoney() {
        this.setTickRandomly(false);
        this.setSoundType(SoundType.WOOD);
        this.setCreativeTab(HoneyBees.TAB_HONEY);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
    }
}
