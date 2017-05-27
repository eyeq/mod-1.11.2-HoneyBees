package eyeq.honeybees.block;

import java.util.Random;

import net.minecraft.block.BlockDirectional;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import eyeq.honeybees.HoneyBees;
import eyeq.honeybees.entity.passive.EntityBee;

public class BlockBeehive extends BlockDirectional {
    protected static final AxisAlignedBB NATURAL_AABB = new AxisAlignedBB(0.0625, 0.0, 0.0625, 0.9375, 1.0, 0.9375);

    private final boolean isSim;

    public BlockBeehive(boolean isSim) {
        super(Material.WOOD);
        this.isSim = isSim;
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        this.setTickRandomly(true);
        this.setSoundType(SoundType.WOOD);
        this.setCreativeTab(HoneyBees.TAB_HONEY);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        if(isSim) {
            return FULL_BLOCK_AABB;
        }
        return NATURAL_AABB;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return isSim;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return isSim;
    }

    @Override
    public int quantityDropped(Random rand) {
        return 2 + rand.nextInt(3);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return HoneyBees.honey;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing facing) {
        if(isSim) {
            return super.shouldSideBeRendered(state, world, pos, facing);
        }
        return true;
    }

    @Override
    public int tickRate(World world) {
        return 100 + world.rand.nextInt(100);
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        world.scheduleUpdate(pos, this, this.tickRate(world));
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.getFront(meta & 7));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex();
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rotation) {
        return state.withProperty(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirror) {
        return state.withRotation(mirror.toRotation(state.getValue(FACING)));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    private EntityBee spawnBee(World world, int posX, int posY, int posZ, EntityPlayer player, Random rand) {
        EntityBee bee = new EntityBee(world);
        bee.setLocationAndAngles(posX + rand.nextFloat(), posY, posZ + rand.nextFloat(), rand.nextFloat() * 360.0F, 0.0F);
        bee.becomeAngryAt(player);
        world.spawnEntity(bee);
        return bee;
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if(world.isRemote) {
            return;
        }
        world.scheduleUpdate(pos, this, this.tickRate(world));
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        EnumFacing face = state.getValue(FACING);
        if(world.getEntitiesWithinAABB(EntityBee.class, new AxisAlignedBB(pos).expand(4.0, 4.0, 4.0)).size() < 6) {
            this.spawnBee(world, x + face.getFrontOffsetX(), y - 1, z + face.getFrontOffsetZ(), null, rand);
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(world.isRemote) {
            return true;
        }
        if(player.isCreative()) {
            return true;
        }
        ItemStack itemStack = player.getHeldItem(hand);
        if(itemStack.getItem() != Items.BUCKET) {
            return false;
        }
        itemStack.shrink(1);
        ItemStack honey = new ItemStack(HoneyBees.bucketHoney);
        if(!player.inventory.addItemStackToInventory(honey)) {
            player.dropItem(honey, false);
        }
        if(!isSim) {
            this.spawnBee(world, pos.getX(), pos.getY(), pos.getZ(), player, world.rand);
        }
        return true;
    }

    @Override
    public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        super.breakBlock(world, pos, state);
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        Random rand = world.rand;
        int n = 6 + rand.nextInt(6);
        for(int i = 0; i < n; i++) {
            this.spawnBee(world, x, y, z, player, rand);
        }
    }
}
