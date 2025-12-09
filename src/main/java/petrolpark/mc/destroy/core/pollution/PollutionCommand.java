package petrolpark.mc.destroy.core.pollution;

import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyRegistries;

@EventBusSubscriber
public class PollutionCommand {
    
    @SubscribeEvent
    public static final void onRegisterCommands(RegisterCommandsEvent event) {
        final CommandBuildContext context = event.getBuildContext();
        event.getDispatcher().register(Commands.literal(Destroy.MOD_ID)
            .then(Commands.literal("pollution")
                .requires(cs -> cs.hasPermission(2))    
                .then(Commands.literal("level")
                    .then(Commands.argument("type", ResourceArgument.resource(context, DestroyRegistries.Keys.LEVEL_POLLUTION_TYPE))
                        .then(Commands.literal("query")
                            .executes(ctx -> {
                                return queryLevelPollution(ctx.getSource(), ResourceArgument.getResource(ctx, "type", DestroyRegistries.Keys.LEVEL_POLLUTION_TYPE).value());
                            })
                        ).then(Commands.literal("set")
                            .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                .executes(ctx -> {
                                    return setLevelPollution(ctx.getSource(), ResourceArgument.getResource(ctx, "type", DestroyRegistries.Keys.LEVEL_POLLUTION_TYPE).value(), IntegerArgumentType.getInteger(ctx, "value"));
                                })
                            )
                        ).then(Commands.literal("add")
                            .then(Commands.argument("change", IntegerArgumentType.integer())
                                .executes(ctx -> {
                                    return addLevelPollution(ctx.getSource(), ResourceArgument.getResource(ctx, "type", DestroyRegistries.Keys.LEVEL_POLLUTION_TYPE).value(), IntegerArgumentType.getInteger(ctx, "change"));
                                })
                            )
                        )
                    )
                ).then(Commands.literal("chunk")
                    .then(Commands.argument("position", BlockPosArgument.blockPos())
                        .then(Commands.argument("type", ResourceArgument.resource(context, DestroyRegistries.Keys.CHUNK_POLLUTION_TYPE))
                            .then(Commands.literal("query")
                                .executes(ctx -> {
                                    return queryChunkPollution(ctx.getSource(), BlockPosArgument.getLoadedBlockPos(ctx, "position"), ResourceArgument.getResource(ctx, "type", DestroyRegistries.Keys.CHUNK_POLLUTION_TYPE).value());
                                })
                            ).then(Commands.literal("set")
                                .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                    .executes(ctx -> {
                                        return setChunkPollution(ctx.getSource(), BlockPosArgument.getLoadedBlockPos(ctx, "position"), ResourceArgument.getResource(ctx, "type", DestroyRegistries.Keys.CHUNK_POLLUTION_TYPE).value(), IntegerArgumentType.getInteger(ctx, "value"));
                                    })
                                )
                            ).then(Commands.literal("add")
                                .then(Commands.argument("change", IntegerArgumentType.integer())
                                    .executes(ctx -> {
                                        return addChunkPollution(ctx.getSource(), BlockPosArgument.getLoadedBlockPos(ctx, "position"), ResourceArgument.getResource(ctx, "type", DestroyRegistries.Keys.CHUNK_POLLUTION_TYPE).value(), IntegerArgumentType.getInteger(ctx, "change"));
                                    })
                                )
                            )
                        )
                    )
                )
            )
        );
    };

    private static final int queryLevelPollution(CommandSourceStack source, PollutionType<Level> pollutionType) {
        final int pollutionLevel = PollutionHelper.getPollution(source.getLevel(), pollutionType);
        source.sendSuccess(() ->  Component.translatable("commands.destroy.pollution.query", pollutionType.getName(), pollutionLevel), true);
        return pollutionLevel;
    };

    private static final int setLevelPollution(CommandSourceStack source, PollutionType<Level> pollutionType, int value) {
        final int pollutionLevel = PollutionHelper.setPollution(source.getLevel(), pollutionType, value);
        source.sendSuccess(() ->  Component.translatable("commands.destroy.pollution.set", pollutionType.getName(), pollutionLevel), true);
        return pollutionLevel;
    };

    private static final int addLevelPollution(CommandSourceStack source, PollutionType<Level> pollutionType, int change) {
        final int pollutionLevel = PollutionHelper.setPollution(source.getLevel(), pollutionType, change);
        source.sendSuccess(() ->  Component.translatable("commands.destroy.pollution.set", pollutionType.getName(), pollutionLevel), true);
        return pollutionLevel;
    };

    private static final int queryChunkPollution(CommandSourceStack source, BlockPos pos, PollutionType<ChunkAccess> pollutionType) {
        final int pollutionLevel = PollutionHelper.getPollution(source.getLevel().getChunk(pos), pollutionType);
        source.sendSuccess(() ->  Component.translatable("commands.destroy.pollution.query", pollutionType.getName(), pollutionLevel), true);
        return pollutionLevel;
    };

    private static final int setChunkPollution(CommandSourceStack source, BlockPos pos, PollutionType<ChunkAccess> pollutionType, int value) {
        final int pollutionLevel = PollutionHelper.setPollution(source.getLevel().getChunk(pos), pollutionType, value);
        source.sendSuccess(() ->  Component.translatable("commands.destroy.pollution.set", pollutionType.getName(), pollutionLevel), true);
        return pollutionLevel;
    };

    private static final int addChunkPollution(CommandSourceStack source, BlockPos pos, PollutionType<ChunkAccess> pollutionType, int change) {
        final int pollutionLevel = PollutionHelper.changePollution(source.getLevel().getChunk(pos), pollutionType, change);
        source.sendSuccess(() ->  Component.translatable("commands.destroy.pollution.set", pollutionType.getName(), pollutionLevel), true);
        return pollutionLevel;
    };
};
