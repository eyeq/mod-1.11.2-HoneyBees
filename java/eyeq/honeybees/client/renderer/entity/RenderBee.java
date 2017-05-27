package eyeq.honeybees.client.renderer.entity;

import eyeq.honeybees.client.model.ModelBee;
import eyeq.util.client.renderer.EntityRenderResourceLocation;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import static eyeq.honeybees.HoneyBees.MOD_ID;

public class RenderBee extends RenderLiving {
    protected static final ResourceLocation textures = new EntityRenderResourceLocation(MOD_ID, "bee");

    public RenderBee(RenderManager renderManager) {
        super(renderManager, new ModelBee(), 0.1F);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return textures;
    }
}
