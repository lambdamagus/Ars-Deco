package lambdamagus.ars_deco.client.model;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import java.util.IdentityHashMap;
import java.util.Map;

public class RetexturedQuadCache {
    private final Map<BakedQuad, Map<TextureAtlasSprite, BakedQuad>> cache = new IdentityHashMap<>();

    public BakedQuad retexture(BakedQuad original, TextureAtlasSprite newSprite) {
        return cache.computeIfAbsent(original, unused -> new IdentityHashMap<>())
                .computeIfAbsent(newSprite, sprite -> makeQuad(original, sprite));
    }

    private BakedQuad makeQuad(BakedQuad original, TextureAtlasSprite newSprite) {
        TextureAtlasSprite oldSprite = original.getSprite();
        int[] vertices = original.getVertices().clone();
        int stride = vertices.length / 4;
        for (int vertex = 0; vertex < 4; vertex++) {
            int offset = vertex * stride;
            float oldU = Float.intBitsToFloat(vertices[offset + 4]);
            float oldV = Float.intBitsToFloat(vertices[offset + 5]);
            float localU = (oldU - oldSprite.getU0()) / (oldSprite.getU1() - oldSprite.getU0());
            float localV = (oldV - oldSprite.getV0()) / (oldSprite.getV1() - oldSprite.getV0());
            vertices[offset + 4] = Float.floatToRawIntBits(newSprite.getU0() + localU * (newSprite.getU1() - newSprite.getU0()));
            vertices[offset + 5] = Float.floatToRawIntBits(newSprite.getV0() + localV * (newSprite.getV1() - newSprite.getV0()));
        }

        return new BakedQuad(vertices, original.getTintIndex(), original.getDirection(), newSprite, original.isShade());
    }
}
