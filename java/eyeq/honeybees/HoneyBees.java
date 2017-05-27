package eyeq.honeybees;

import eyeq.honeybees.block.BlockBeehive;
import eyeq.honeybees.block.BlockTorchHoney;
import eyeq.honeybees.client.renderer.entity.RenderBee;
import eyeq.honeybees.entity.passive.EntityBee;
import eyeq.honeybees.item.ItemArmorImker;
import eyeq.honeybees.world.gen.WorldGenBeehive;
import eyeq.util.client.model.UModelCreator;
import eyeq.util.client.model.UModelLoader;
import eyeq.util.client.model.gson.ItemmodelJsonFactory;
import eyeq.util.client.renderer.ResourceLocationFactory;
import eyeq.util.client.resource.ULanguageCreator;
import eyeq.util.client.resource.USoundCreator;
import eyeq.util.client.resource.gson.SoundResourceManager;
import eyeq.util.client.resource.lang.LanguageResourceManager;
import eyeq.util.common.registry.UEntityRegistry;
import eyeq.util.common.registry.USoundEventRegistry;
import eyeq.util.creativetab.UCreativeTab;
import eyeq.util.item.UItemBottle;
import eyeq.util.item.UItemJuice;
import eyeq.util.item.crafting.ShapelessPotionRecipe;
import eyeq.util.oredict.CategoryTypes;
import eyeq.util.oredict.UOreDictionary;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.PotionTypes;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.ShapedOreRecipe;

import java.io.File;

import static eyeq.honeybees.HoneyBees.MOD_ID;

@Mod(modid = MOD_ID, version = "1.0", dependencies = "after:eyeq_util")
@Mod.EventBusSubscriber
public class HoneyBees {
    public static final String MOD_ID = "eyeq_honeybees";

    @Mod.Instance(MOD_ID)
    public static HoneyBees instance;

    private static final ResourceLocationFactory resource = new ResourceLocationFactory(MOD_ID);

    public static SoundEvent entityBeeAmbient;

    public static Item honey;
    public static final CreativeTabs TAB_HONEY = new UCreativeTab("HoneyBees", () -> new ItemStack(honey));

    public static Block beehive;
    public static Block beehiveSim;
    public static Block honeyTorch;

    public static Item honeyBread;
    public static Item mead;
    public static Item bucketHoney;
    public static Item imkerHelmet;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        addRecipes();
        registerEntities();
        registerSoundEvents();
        if(event.getSide().isServer()) {
            return;
        }
        renderItemModels();
        registerEntityRenderings();
        createFiles();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        GameRegistry.registerWorldGenerator(new WorldGenBeehive(), 8);
    }

    @SubscribeEvent
    protected static void registerBlocks(RegistryEvent.Register<Block> event) {
        beehive = new BlockBeehive(false).setHardness(1.0F).setUnlocalizedName("beehive");
        beehiveSim = new BlockBeehive(true).setHardness(1.0F).setUnlocalizedName("beehive");
        honeyTorch = new BlockTorchHoney().setHardness(0.0F).setUnlocalizedName("honeyTorch");

        GameRegistry.register(beehive, resource.createResourceLocation("beehive"));
        GameRegistry.register(beehiveSim, resource.createResourceLocation("sim_beehive"));
        GameRegistry.register(honeyTorch, resource.createResourceLocation("honey_torch"));
    }

    @SubscribeEvent
    protected static void registerItems(RegistryEvent.Register<Item> event) {
        honey = new ItemFood(4, 0.0F, false).setUnlocalizedName("honey").setCreativeTab(TAB_HONEY);
        honeyBread = new ItemFood(10, 0.6F, false).setPotionEffect(new PotionEffect(MobEffects.HASTE, 60, 3), 1.0F).setUnlocalizedName("honeyBread").setCreativeTab(TAB_HONEY);
        mead = new UItemBottle(0, 0.6F, false).setPotionEffect(new PotionEffect(MobEffects.STRENGTH, 60, 0), 1.0F).setUnlocalizedName("mead").setCreativeTab(TAB_HONEY);
        bucketHoney = new UItemJuice(4, 0.6F, false).setPotionEffect(new PotionEffect(MobEffects.SPEED, 30, 0), 1.0F).setRestItem(new ItemStack(Items.BUCKET)).setContainerItem(Items.BUCKET).setMaxStackSize(1).setUnlocalizedName("honey").setCreativeTab(TAB_HONEY);
        imkerHelmet = new ItemArmorImker(EntityEquipmentSlot.HEAD).setUnlocalizedName("imkerHelmet").setCreativeTab(TAB_HONEY);

        GameRegistry.register(new ItemBlock(beehive), beehive.getRegistryName());
        GameRegistry.register(new ItemBlock(beehiveSim), beehiveSim.getRegistryName());
        GameRegistry.register(new ItemBlock(honeyTorch), honeyTorch.getRegistryName());

        GameRegistry.register(honey, resource.createResourceLocation("honey"));
        GameRegistry.register(honeyBread, resource.createResourceLocation("honey_bread"));
        GameRegistry.register(mead, resource.createResourceLocation("mead"));
        GameRegistry.register(bucketHoney, resource.createResourceLocation("bucket_honey"));
        GameRegistry.register(imkerHelmet, resource.createResourceLocation("imker_helmet"));

        UOreDictionary.registerOre(CategoryTypes.COOKED, "bread", honeyBread);
        UOreDictionary.registerOre(CategoryTypes.SWEET, "honey", honey);
        UOreDictionary.registerOre(CategoryTypes.SWEET, "honey", mead);
        UOreDictionary.registerOre(CategoryTypes.SWEET, "honey", bucketHoney);
    }

    public static void addRecipes() {
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(beehiveSim), "XXX", "YYY", "XXX",
                'X', "stickWood", 'Y', honey));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(honeyTorch, 4), "X", "Y", "Y",
                'X', honey, 'Y', "stickWood"));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(imkerHelmet), "XXX", "XYX", "YYY",
                'X', UOreDictionary.OREDICT_IRON_INGOT, 'Y', UOreDictionary.OREDICT_STRING));
        GameRegistry.addShapelessRecipe(new ItemStack(honeyBread), Items.BREAD, honey);
        GameRegistry.addRecipe(new ShapelessPotionRecipe(new ItemStack(mead), PotionTypes.WATER, honey));
    }

    public static void registerEntities() {
        UEntityRegistry.registerModEntity(resource, EntityBee.class, "Bee", 0, instance, 0xFFD800, 0x000000);
    }

    public static void registerSoundEvents() {
        entityBeeAmbient = new SoundEvent(resource.createResourceLocation("bee"));

        USoundEventRegistry.registry(entityBeeAmbient);
    }

    @SideOnly(Side.CLIENT)
    public static void renderItemModels() {
        UModelLoader.setCustomModelResourceLocation(beehive);
        UModelLoader.setCustomModelResourceLocation(beehiveSim);
        UModelLoader.setCustomModelResourceLocation(honeyTorch);

        UModelLoader.setCustomModelResourceLocation(honey);
        UModelLoader.setCustomModelResourceLocation(honeyBread);
        UModelLoader.setCustomModelResourceLocation(mead);
        UModelLoader.setCustomModelResourceLocation(bucketHoney);
        UModelLoader.setCustomModelResourceLocation(imkerHelmet);
    }

    @SideOnly(Side.CLIENT)
    public static void registerEntityRenderings() {
        RenderingRegistry.registerEntityRenderingHandler(EntityBee.class, RenderBee::new);
    }

    public static void createFiles() {
        File project = new File("../1.11.2-HoneyBees");

        LanguageResourceManager language = new LanguageResourceManager();

        language.register(LanguageResourceManager.EN_US, TAB_HONEY, "Honey Bees");
        language.register(LanguageResourceManager.JA_JP, TAB_HONEY, "ミツバチ");

        language.register(LanguageResourceManager.EN_US, beehive, "Beehive");
        language.register(LanguageResourceManager.JA_JP, beehive, "巣箱");
        language.register(LanguageResourceManager.EN_US, honeyTorch, "Honey Torch");
        language.register(LanguageResourceManager.JA_JP, honeyTorch, "ハチ寄せ棒");

        language.register(LanguageResourceManager.EN_US, honey, "Honey");
        language.register(LanguageResourceManager.JA_JP, honey, "はちみつ");
        language.register(LanguageResourceManager.EN_US, honeyBread, "Honey Bread");
        language.register(LanguageResourceManager.JA_JP, honeyBread, "はちみつパン");
        language.register(LanguageResourceManager.EN_US, mead, "Mead");
        language.register(LanguageResourceManager.JA_JP, mead, "はちみつ酒");
        language.register(LanguageResourceManager.EN_US, imkerHelmet, "Imker Helmet");
        language.register(LanguageResourceManager.JA_JP, imkerHelmet, "防護ヘルメット");

        language.register(LanguageResourceManager.EN_US, EntityBee.class, "Bee");
        language.register(LanguageResourceManager.JA_JP, EntityBee.class, "ハチ");

        ULanguageCreator.createLanguage(project, MOD_ID, language);

        SoundResourceManager sound = new SoundResourceManager();

        sound.register(entityBeeAmbient, SoundCategory.AMBIENT.getName());

        USoundCreator.createSoundJson(project, MOD_ID, sound);

        UModelCreator.createItemJson(project, honey, ItemmodelJsonFactory.ItemmodelParent.GENERATED);
        UModelCreator.createItemJson(project, honeyBread, ItemmodelJsonFactory.ItemmodelParent.GENERATED);
        UModelCreator.createItemJson(project, mead, ItemmodelJsonFactory.ItemmodelParent.GENERATED);
        UModelCreator.createItemJson(project, bucketHoney, ItemmodelJsonFactory.ItemmodelParent.GENERATED);
        UModelCreator.createItemJson(project, imkerHelmet, ItemmodelJsonFactory.ItemmodelParent.GENERATED);
    }
}
