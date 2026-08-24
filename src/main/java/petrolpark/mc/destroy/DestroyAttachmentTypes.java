package petrolpark.mc.destroy;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import petrolpark.mc.destroy.core.oil.ChunkCrudeOil;
import petrolpark.mc.destroy.core.pollution.ChunkPollution;
import petrolpark.mc.destroy.core.pollution.LevelPollution;

public class DestroyAttachmentTypes {

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Destroy.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<LevelPollution>> LEVEL_POLLUTION = ATTACHMENT_TYPES.register("level_pollution", AttachmentType.builder(LevelPollution::create)
        .serialize(LevelPollution.SERIALIZER)
        ::build
    );

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ChunkPollution>> CHUNK_POLLUTION = ATTACHMENT_TYPES.register("chunk_pollution", AttachmentType.builder(ChunkPollution::create)
        .serialize(ChunkPollution.SERIALIZER)
        ::build
    );

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ChunkCrudeOil>> CHUNK_CRUDE_OIL = ATTACHMENT_TYPES.register("crude_oil", AttachmentType.builder(ChunkCrudeOil::create)
        .serialize(ChunkCrudeOil.SERIALIZER)
        ::build
    );

    public static final void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    };
};
