package petrolpark.mc.destroy.core.oil;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyAttachmentTypes;

@EventBusSubscriber
public class CrudeOilCommand {
  
    @SubscribeEvent
    public static final void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal(Destroy.MOD_ID)
            .requires(cs -> cs.hasPermission(2))
            .then(Commands.argument("position", BlockPosArgument.blockPos())
                .then(Commands.literal("query")
                    .executes(CrudeOilCommand::queryCrudeOil)
                ).then(Commands.literal("set")
                    .then(Commands.argument("amount", IntegerArgumentType.integer())
                        .executes(CrudeOilCommand::setCrudeOil)
                    )
                ).then(Commands.literal("change")
                    .then(Commands.argument("amount", IntegerArgumentType.integer())
                        .executes(CrudeOilCommand::changeCrudeOil)
                    )
                )
            )
        );
    };

    private static int queryCrudeOil(CommandContext<CommandSourceStack> context) {
        final CommandSourceStack source = context.getSource();
        final Player player = source.getPlayer(); // May be null
        final BlockPos pos = context.getArgument("position", Coordinates.class).getBlockPos(source);
        final LevelChunk chunk = source.getLevel().getChunkAt(pos);
        final ChunkCrudeOil crudeOil = chunk.getData(DestroyAttachmentTypes.CHUNK_CRUDE_OIL);
        crudeOil.generate(chunk, player);
        source.sendSuccess(() ->  Component.translatable("commands.destroy.crudeoil", crudeOil.getAmount(), pos.getX(), pos.getY(), pos.getZ()), true);
        return crudeOil.getAmount();
    };

    private static int setCrudeOil(CommandContext<CommandSourceStack> context) {
        final CommandSourceStack source = context.getSource();
        final BlockPos pos = context.getArgument("position", Coordinates.class).getBlockPos(source);
        final LevelChunk chunk = source.getLevel().getChunkAt(pos);
        final ChunkCrudeOil crudeOil = chunk.getData(DestroyAttachmentTypes.CHUNK_CRUDE_OIL);
        crudeOil.generate(chunk, null);
        final int amount = crudeOil.setAmount(context.getArgument("amount", Integer.class));
        source.sendSuccess(() ->  Component.translatable("commands.destroy.crudeoil", amount, pos.getX(), pos.getY(), pos.getZ()), true);
        return amount;
    };

    private static int changeCrudeOil(CommandContext<CommandSourceStack> context) {
        final CommandSourceStack source = context.getSource();
        final BlockPos pos = context.getArgument("position", Coordinates.class).getBlockPos(source);
        final LevelChunk chunk = source.getLevel().getChunkAt(pos);
        final ChunkCrudeOil crudeOil = chunk.getData(DestroyAttachmentTypes.CHUNK_CRUDE_OIL);
        crudeOil.generate(chunk, null);
        final int amount = crudeOil.decreaseAmount(-context.getArgument("amount", Integer.class));
        source.sendSuccess(() ->  Component.translatable("commands.destroy.crudeoil", amount, pos.getX(), pos.getY(), pos.getZ()), true);
        return amount;
    };
};
