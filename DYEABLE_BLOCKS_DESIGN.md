# Dyeable Ars Nouveau Blocks Design

## Goal

Make Ars Nouveau blocks dyeable in all 16 Minecraft dye colors using the new texture variants under `src/main/resources/assets/ars_deco/textures`.

Players should be able to dye supported objects in two ways:

- Combine the item with a dye in a crafting table.
- Right-click the placed block in the world with a dye.

Dyed objects must not break existing recipes. Recipes that currently accept an Ars Nouveau item should continue to accept both dyed and undyed versions of that same item.

## Core Approach

Use one item/block ID with color data, not 16 separate registered block variants per block.

Illustration:

- Undyed sourcestone remains `ars_nouveau:sourcestone`.
- Red sourcestone is still `ars_nouveau:sourcestone`, but its `ItemStack` has a color component.
- Existing recipe ingredients continue to match because the item ID has not changed.
- Placed blocks store their color separately and render with the matching texture.

Use vanilla's existing item component for item color:

```java
DataComponents.BASE_COLOR
```

Undyed means the component is absent.

## Namespace Note

The current mod id and asset namespace are `ars_deco`, and the Java package namespace is `lambdamagus.ars_deco`.

Before implementation, choose one of these paths:

- Keep generated data, assets, recipes, sounds, and networking payloads under the `ars_deco` resource namespace.
- Keep Java classes under the `lambdamagus.ars_deco` package namespace.

This matters because generated resource locations must match the actual asset namespace.

## Dye Target Registry

Create a central manifest of dyeable blocks:

```java
public final class DyeableArsBlocks {
    public static final Map<Block, DyeTarget> TARGETS = ...;
}
```

Each `DyeTarget` should define:

- Target block.
- Item form.
- Texture/model key, such as `sourcestone`, `source_relay`, or `arcane_core`.
- Render path type: baked JSON model, GeckoLib block entity model, fluid texture, multipart texture, etc.
- Related recipe family for preserving color through stairs, slabs, stonecutting, and upgrades.

The texture folders already provide most of the manifest shape:

```text
sourcestone
smooth_sourcestone
source_relay
source_splitter
ritual_brazier
arcane_core
imbuement_chamber
...
```

Do not hard-code the 16 color names repeatedly. Use `DyeColor.values()` and validate during startup or datagen that each dyeable texture target has all 16 files.

## Item Color Storage

Add helper methods:

```java
Optional<DyeColor> getColor(ItemStack stack)
void setColor(ItemStack stack, DyeColor color)
boolean isDyeableTarget(ItemStack stack)
```

The implementation should read and write `DataComponents.BASE_COLOR`.

Illustration:

```java
ItemStack stack = new ItemStack(BlockRegistry.SOURCESTONE);
stack.set(DataComponents.BASE_COLOR, DyeColor.CYAN);
```

Crafting and drops should preserve all unrelated components, such as source jar fill data or potion jar contents.

## Crafting Table Dyeing

Register one custom recipe serializer:

```text
ars_deco:dye_ars_block
```

The recipe should match:

- Exactly one dyeable Ars block item.
- Exactly one vanilla dye item.

The assembled result should:

- Copy the input block stack.
- Set count to `1`.
- Apply `DataComponents.BASE_COLOR`.
- Preserve existing components from the input stack.

Use a single custom recipe JSON:

```json
{
  "type": "ars_deco:dye_ars_block",
  "category": "building"
}
```

This avoids generating hundreds or thousands of dye recipes.

## Right-Click Dyeing In World

Subscribe to `PlayerInteractEvent.RightClickBlock`.

Behavior:

```text
player right-clicks block with dye
-> block is in DyeableArsBlocks.TARGETS
-> placed color differs from dye color
-> store the new color
-> consume one dye unless the player is in creative mode
-> play a dye sound and particles
-> update the block render on the client
```

For existing Ars block entities, add color support by mixin or helper interface.

For simple decorative blocks that do not currently have block entities, the preferred solution is to give dyeable targets a lightweight color block entity. This keeps the block/item ID stable and avoids recipe breakage.

## Placed Block Persistence

Define a small interface:

```java
public interface DyeableBlockEntity {
    Optional<DyeColor> ars_deco$getColor();
    void ars_deco$setColor(@Nullable DyeColor color);
}
```

Store the color in block entity NBT:

```nbt
ars_deco_color: "cyan"
```

Sync it to the client using the same pattern Ars Nouveau already uses in `ModdedTile`, including update tags and block updates.

## Placement And Drops

Placement:

- When a dyed `ItemStack` places a dyeable block, copy `DataComponents.BASE_COLOR` from the item to the placed block entity.

Drops:

- When a dyed block drops itself, apply the stored color back onto the dropped `ItemStack`.
- Preserve unrelated item components where applicable.

This can be implemented with targeted mixins or NeoForge placement/drop events.

## Rendering

Texture path convention:

```text
ars_deco:textures/<texture_name>/<color>.png
```

Undyed blocks should use the original Ars Nouveau texture:

```text
ars_nouveau:textures/block/<texture_name>.png
```

### Baked JSON Models

For normal block models:

- Wrap the target baked models during model baking.
- At render time, inspect the color from model data supplied by the block entity.
- Delegate to the pre-baked colored model for that dye color.
- For item models, inspect `DataComponents.BASE_COLOR` from the `ItemStack`.

### GeckoLib Block Entity Models

Many Ars Nouveau blocks use `GenericModel`, `GenericTileRenderer`, and custom GeckoLib renderers.

For these:

- Override or mix into texture lookup so block entities can return a colored texture.
- Use the block entity color for placed blocks.
- Use the item stack color for rendered inventory items.

Texture mapping:

```text
und dyed source relay -> ars_nouveau:textures/block/source_relay.png
red source relay -> ars_deco:textures/source_relay/red.png
```

Ars Nouveau's current `GenericItemModel` does not naturally pass the `ItemStack` into texture lookup, so item rendering may need a small client-side mixin or context holder.

## Recipe Compatibility

The main compatibility guarantee comes from keeping the same item IDs.

Existing recipe ingredients like this:

```json
{
  "item": "ars_nouveau:sourcestone"
}
```

will still accept dyed sourcestone because dyed sourcestone is still the same item.

Add optional color propagation for transformation recipes:

- Dyed sourcestone to dyed sourcestone stairs.
- Dyed sourcestone to dyed sourcestone slabs.
- Dyed slabs/stairs back to dyed base blocks where applicable.
- Stonecutter outputs preserve input color.
- Source relay upgrades preserve input color.

This can be done with targeted custom recipe wrappers or result hooks for known Ars Nouveau decorative families.

## Implementation Order

1. Keep all mod/resource ids under `ars_deco` and Java packages under `lambdamagus.ars_deco`.
2. Build the `DyeableArsBlocks` manifest from texture folders.
3. Add item component helpers using `DataComponents.BASE_COLOR`.
4. Add the custom crafting recipe for block plus dye.
5. Add right-click dye handling for existing block entities first.
6. Add persistence, sync, placement, and drop support.
7. Add rendering for GeckoLib block entity models.
8. Add rendering for normal baked block models.
9. Add color propagation through stairs, slabs, stonecutting, and upgrades.
10. Add validation/tests for all 16 colors per dyeable texture target.

## Implementation Status

Current implemented foundation:

- Dyeable Ars block target manifest.
- Dyed item stack storage using `DataComponents.BASE_COLOR`.
- Custom crafting recipe for dyeable Ars block item plus dye.
- Right-click dye handling for supported placed blocks that already have block entities.
- Block entity color persistence and client sync.
- Drop color preservation for dyed block entities.
- Placement color transfer from dyed item stacks to placed block entities.
- GeckoLib texture lookup for dyed placed block entities.
- GeckoLib item renderer texture lookup for dyed item stacks.
- Tooltip line showing the current dye color on dyed stacks.
- External position color storage for non-block-entity decorative blocks.
- Clientbound chunk and single-block dye color sync.
- Placement, right-click dyeing, and drop color preservation for non-block-entity decorative blocks.
- Baked model wrapper with quad UV remapping for dyed decorative block/item rendering.

Still pending:

- Color propagation through stairs, slabs, stonecutting, and source relay upgrades.
- Texture validation for all dye targets.
- In-game visual QA across representative block families.
- Runtime confirmation that `assets/minecraft/atlases/blocks.json` stitches every `ars_deco` dye texture into the block atlas.

## Future Considerations

These are not part of the initial implementation, but the design should leave room for them.

### Dyeable Ars Nouveau Portals

Ars Nouveau portals should eventually be dyeable.

The initial dye storage model should therefore support blocks whose visible state is primarily rendered by a block entity or special renderer, not only normal block models.

Portal dye color should likely be stored the same way as other placed dyeable blocks, with the item form using `DataComponents.BASE_COLOR` and the placed portal storing `ars_deco_color`.

### Source Relay Particle Color

Dyed source relays should eventually recolor source particles that pass through them.

This should apply to all relay types:

- Basic source relay.
- Source splitter.
- Source deposit.
- Source warp.
- Source collector.
- Brazier relay, if it participates in source movement visuals.

The dye data should be exposed through a small runtime helper, not only through rendering code, so future source-transfer logic can ask:

```java
Optional<DyeColor> color = DyeableArsBlocks.getPlacedColor(level, relayPos);
```

Particle recoloring should be treated as a behavior layer on top of the same stored dye color, rather than a separate particle-only color system.

### Source Jar Fill Color

Dyed source jars should eventually fill with source matching the jar's dye color.

This means source jar rendering should distinguish between:

- The jar/block texture color.
- The visible source fluid/fill color.
- The amount of source stored.

For the future behavior, a dyed source jar should use its stored dye color when rendering source contents. Undyed jars should keep the current Ars Nouveau source color.

The initial implementation should avoid baking assumptions that source color is always the default Ars Nouveau color.

## Main Risk

The difficult part is not crafting. The difficult part is storing and rendering per-position color for normal non-block-entity decorative blocks like sourcestone variants.

The robust solution is to give dyeable decorative targets a lightweight block entity via targeted mixins, then use that data for rendering, drops, and world synchronization.

Avoid registering 16 separate block IDs per target unless absolutely necessary. It would greatly increase recipe, tag, loot table, model, and compatibility maintenance.

## Next Step Design: Non-Block-Entity Decorative Blocks

This section covers the harder next step: dyed baked-model blocks that do not already have block entities, especially sourcestone, slabs, stairs, grates, archwood planks, trapdoors, and similar decorative blocks.

### Constraint Analysis

The current GeckoLib path works because those blocks already have block entities. Decorative sourcestone-style blocks usually do not.

Adding a lightweight block entity directly to existing Ars Nouveau decorative blocks sounds attractive, but it is risky for these reasons:

- Many decorative variants are registered as vanilla classes such as `StairBlock`, `SlabBlock`, `DoorBlock`, and `TrapDoorBlock`.
- A mixin that makes `StairBlock` or `SlabBlock` implement `EntityBlock` would affect every vanilla/modded stair or slab of that class, not just Ars Nouveau's instances.
- A mixin on base `Block` would be even broader and could make block entity checks happen for far more blocks than intended.
- Replacing Ars Nouveau's already-registered blocks with subclassed versions is not viable from an addon without deep registry surgery.

The safer design is therefore:

- Store dye color externally by world position for non-block-entity targets.
- Sync that position color to the client.
- Feed the color into baked model rendering via NeoForge model data.
- Preserve color in drops and placement by reading/writing the same position store.

Existing block-entity targets can continue using the block entity color path already implemented.

### Position Color Store

Add a server-side store for dyed non-block-entity placements:

```java
public final class PlacedDyeColors extends SavedData {
    Optional<DyeColor> getColor(ResourceKey<Level> dimension, BlockPos pos);
    void setColor(ResourceKey<Level> dimension, BlockPos pos, DyeColor color);
    void clearColor(ResourceKey<Level> dimension, BlockPos pos);
    Map<BlockPos, DyeColor> getColorsInChunk(ChunkPos chunkPos);
}
```

Implementation details:

- Store data per dimension using `SavedData`.
- Internally group by `ChunkPos` first, then `BlockPos.asLong()`, so chunk sync and cleanup are cheap.
- Persist dye colors as dye names, not ordinals.
- Only store entries for non-block-entity dyeable targets.
- Remove entries when the block at that position is no longer the expected dyeable target.

Suggested internal shape:

```java
Map<Long, ChunkColors> colorsByChunk;
Map<Long, PlacedColor> colorsByBlockPos;

record PlacedColor(ResourceLocation blockId, DyeColor color) {}
```

Store the `blockId` alongside the color. This prevents stale color data from applying if a dyed block is replaced by another block at the same position.

### Client Cache And Networking

Add a client-side cache:

```java
public final class ClientPlacedDyeColors {
    Optional<DyeColor> getColor(BlockPos pos, BlockState state);
    void applyChunk(ChunkPos chunkPos, Map<BlockPos, PlacedColor> colors);
    void applySingle(BlockPos pos, @Nullable PlacedColor color);
    void clearChunk(ChunkPos chunkPos);
}
```

Add two clientbound payloads:

```java
SyncChunkDyeColorsPayload(ChunkPos chunkPos, List<PlacedColorEntry> entries)
SyncBlockDyeColorPayload(BlockPos pos, @Nullable PlacedColor color)
```

Sync flow:

- On `ChunkWatchEvent.Sent`, send all dyed positions for that chunk to the watching player.
- On right-click dye or dyed placement, update the server store and send `SyncBlockDyeColorPayload` to players tracking that chunk.
- On block break/removal, send a clear payload for that position.
- On chunk unload client-side, clear that chunk from the client cache if needed.

Use `PacketDistributor.sendToPlayer` for initial chunk sync and `PacketDistributor.sendToPlayersTrackingChunk` for updates.

### Placement, Dyeing, Drops, And Cleanup

Extend the existing placement mixin:

- If the placed block has a dyeable block entity, keep using the block entity path.
- If the placed block is dyeable but has no block entity, write the item stack's `BASE_COLOR` to `PlacedDyeColors`.
- Send a single-position sync packet.
- Request a render update for that block.

Extend right-click dyeing:

- If the target has a dyeable block entity, keep the current behavior.
- Otherwise, if the target is in `DyeableArsBlocks`, write the dye color to `PlacedDyeColors`.
- Consume dye, play sound, sync to tracking clients, and request a render update.

Extend drops:

- In `BlockDropsEvent`, first check the block entity path.
- If no dyeable block entity exists, query `PlacedDyeColors` using the event level and position.
- Apply `DataComponents.BASE_COLOR` to matching dropped item stacks.
- Clear the stored color after applying it.
- Sync a clear packet to tracking clients.

Also handle non-drop replacement cases:

- Watch block placement/replacement events and clear stale stored color when a non-matching block replaces a dyed position.
- Use the stored `blockId` guard during rendering so stale client cache data cannot visually color the wrong block.

### Baked Model Rendering

Use NeoForge's `ModelEvent.ModifyBakingResult` to wrap baked models for dyeable normal blocks.

Create:

```java
public final class DyedBakedModel extends BakedModelWrapper<BakedModel> {
    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData);

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData data, RenderType renderType);

    @Override
    public List<BakedModel> getRenderPasses(ItemStack stack, boolean fabulous);

    @Override
    public TextureAtlasSprite getParticleIcon(ModelData data);
}
```

Add a model property:

```java
public static final ModelProperty<DyeColor> DYE_COLOR = new ModelProperty<>();
```

Block rendering:

- `getModelData` reads from `ClientPlacedDyeColors` using `level`, `pos`, and `state`.
- If a color exists, return derived model data with `DYE_COLOR`.
- `getQuads` checks `DYE_COLOR`.
- If absent, delegate to the original model.
- If present, return retextured quads.

Item rendering:

- `getRenderPasses(ItemStack stack, boolean fabulous)` reads `DataComponents.BASE_COLOR`.
- If absent, return the original model.
- If present, return a fixed-color wrapper for that stack render.

### Quad Retexturing Strategy

Do not try to replace blockstate variants with standalone colored models. That risks losing baked rotations for stairs, slabs, doors, trapdoors, and multipart states.

Instead, retexture the quads produced by the already-baked original model.

Create:

```java
public final class RetexturedQuadCache {
    BakedQuad retexture(BakedQuad original, TextureAtlasSprite oldSprite, TextureAtlasSprite newSprite);
}
```

The retexture operation should:

- Copy the original quad vertex data.
- Convert each vertex UV from old sprite atlas coordinates to normalized sprite-local coordinates.
- Convert those local coordinates into the new sprite's atlas coordinates.
- Construct a new `BakedQuad` with the same direction, tint index, shade, light emission, and ambient occlusion flags, but with the new sprite and remapped UVs.
- Cache by original quad identity plus target sprite.

This preserves:

- Existing blockstate rotations.
- Stair inner/outer/straight variants.
- Slab half/double variants.
- Ambient occlusion and lighting behavior.
- Multipart or weighted baked model behavior.

### Sprite Availability

The colored textures must be present in the block atlas or quad retexturing cannot obtain `TextureAtlasSprite`s for them.

Add or generate an atlas source file, likely:

```text
src/main/resources/assets/minecraft/atlases/blocks.json
```

The atlas should include the dye texture folders under `assets/ars_deco/textures`.

If the atlas source format proves awkward for the current texture layout, use generated tiny model references or move/copy generated texture references under a predictable `block/` path. The important requirement is that every `ars_deco:<textureKey>/<color>` sprite is stitched into the block atlas.

### Model Wrapping Targets

Only wrap baked models whose block or item is in `DyeableArsBlocks` and whose rendering is not already handled by the GeckoLib path.

Initial non-block-entity candidates:

- `sourcestone`
- `smooth_sourcestone`
- sourcestone decorative variants
- gilded/smooth gilded sourcestone variants
- sourcestone slabs and stairs
- grates
- archwood planks
- archwood trapdoor/door, if their model path can be safely retextured

Keep GeckoLib/block-entity targets on the existing texture lookup path:

- source relays
- source jar
- sourcelinks
- arcane core
- imbuement chamber
- enchanting apparatus
- ritual brazier
- spell turrets
- repository
- scribe table

### Feasibility Verdict

This approach should work because it uses stable NeoForge hooks:

- `BlockDropsEvent` for recoloring drops.
- `ChunkWatchEvent.Sent` for initial client sync.
- `PacketDistributor.sendToPlayersTrackingChunk` for live updates.
- `BakedModelWrapper` and `ModelData` for position-aware rendering.
- `getRenderPasses(ItemStack, boolean)` for item-stack-aware model rendering.

The main technical risk is quad UV remapping. It needs careful testing with:

- cube-all blocks
- stairs, including inner and outer shapes
- slabs, including double slabs
- trapdoors and doors
- blocks with transparent/cutout render types

This route is still safer than adding block entities to vanilla block classes, because it does not alter block class behavior globally and does not require replacing Ars Nouveau's registered blocks.
