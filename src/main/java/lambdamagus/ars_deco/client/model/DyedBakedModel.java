package lambdamagus.ars_deco.client.model;

import lambdamagus.ars_deco.dye.ClientPlacedDyeColors;
import lambdamagus.ars_deco.dye.DyeTarget;
import lambdamagus.ars_deco.dye.DyeTextures;
import lambdamagus.ars_deco.dye.DyeableArsBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class DyedBakedModel extends BakedModelWrapper<BakedModel> {
    private final Function<Material, TextureAtlasSprite> textureGetter;
    @Nullable
    private final DyeColor fixedColor;
    @Nullable
    private final DyeTarget fixedTarget;
    private final RetexturedQuadCache quadCache = new RetexturedQuadCache();

    public DyedBakedModel(BakedModel originalModel, Function<Material, TextureAtlasSprite> textureGetter) {
        this(originalModel, textureGetter, null, null);
    }

    private DyedBakedModel(BakedModel originalModel, Function<Material, TextureAtlasSprite> textureGetter, @Nullable DyeColor fixedColor, @Nullable DyeTarget fixedTarget) {
        super(originalModel);
        this.textureGetter = textureGetter;
        this.fixedColor = fixedColor;
        this.fixedTarget = fixedTarget;
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
        DyeColor color = ClientPlacedDyeColors.getColor(pos, state).orElse(null);
        if (color == null) {
            return originalModel.getModelData(level, pos, state, modelData);
        }
        DyeTarget target = DyeableArsBlocks.target(state.getBlock()).orElse(null);
        if (target == null) {
            return originalModel.getModelData(level, pos, state, modelData);
        }
        return originalModel.getModelData(level, pos, state, modelData).derive()
                .with(DyeModelProperties.DYE_COLOR, color)
                .with(DyeModelProperties.DYE_TARGET, target)
                .build();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data, @Nullable RenderType renderType) {
        DyeColor color = fixedColor != null ? fixedColor : data.get(DyeModelProperties.DYE_COLOR);
        if (color == null) {
            return originalModel.getQuads(state, side, rand, data, renderType);
        }

        DyeTarget target = fixedTarget != null ? fixedTarget : state == null ? null : DyeableArsBlocks.target(state.getBlock()).orElse(null);
        if (target == null) {
            return originalModel.getQuads(state, side, rand, data, renderType);
        }

        List<BakedQuad> originalQuads = originalModel.getQuads(state, side, rand, data, renderType);
        List<BakedQuad> retextured = new ArrayList<>(originalQuads.size());
        for (BakedQuad quad : originalQuads) {
            retextured.add(quadCache.retexture(quad, sprite(target, color, quad.getSprite().contents().name())));
        }
        return retextured;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        if (fixedColor == null || fixedTarget == null) {
            return originalModel.getQuads(state, side, rand);
        }

        List<BakedQuad> originalQuads = originalModel.getQuads(state, side, rand);
        List<BakedQuad> retextured = new ArrayList<>(originalQuads.size());
        for (BakedQuad quad : originalQuads) {
            retextured.add(quadCache.retexture(quad, sprite(fixedTarget, fixedColor, quad.getSprite().contents().name())));
        }
        return retextured;
    }

    @Override
    public TextureAtlasSprite getParticleIcon(ModelData data) {
        DyeColor color = fixedColor != null ? fixedColor : data.get(DyeModelProperties.DYE_COLOR);
        if (color == null) {
            return originalModel.getParticleIcon(data);
        }

        DyeTarget target = fixedTarget != null ? fixedTarget : data.get(DyeModelProperties.DYE_TARGET);
        if (target == null) {
            return originalModel.getParticleIcon(data);
        }
        return sprite(target, color);
    }

    @Override
    public List<BakedModel> getRenderPasses(ItemStack itemStack, boolean fabulous) {
        DyeColor color = DyeableArsBlocks.color(itemStack).orElse(null);
        DyeTarget target = DyeableArsBlocks.target(itemStack.getItem()).orElse(null);
        if (color == null || target == null) {
            return originalModel.getRenderPasses(itemStack, fabulous);
        }
        return List.of(new DyedBakedModel(originalModel, textureGetter, color, target));
    }

    @Override
    public BakedModel applyTransform(ItemDisplayContext transformType, PoseStack poseStack, boolean applyLeftHandTransform) {
        originalModel.applyTransform(transformType, poseStack, applyLeftHandTransform);
        return this;
    }

    @Override
    public ItemOverrides getOverrides() {
        return originalModel.getOverrides();
    }

    private TextureAtlasSprite sprite(DyeTarget target, DyeColor color) {
        return textureGetter.apply(new Material(TextureAtlas.LOCATION_BLOCKS, DyeTextures.spriteLocation(target, color)));
    }

    private TextureAtlasSprite sprite(DyeTarget target, DyeColor color, ResourceLocation originalSprite) {
        return textureGetter.apply(new Material(TextureAtlas.LOCATION_BLOCKS, DyeTextures.spriteLocation(target, color, originalSprite)));
    }
}
