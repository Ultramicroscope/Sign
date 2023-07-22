package wtf.ultra;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.impl.event.lifecycle.LoadedChunksCache;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.*;

public class SignMod implements ModInitializer {

    private final Map<BlockPos, String[]> signs = new HashMap<>();

    @Override
    public void onInitialize() {
        ClientTickEvents.END_WORLD_TICK.register(world -> {
            Set<WorldChunk> loadedChunks = ((LoadedChunksCache) world).fabric_getLoadedChunks();
            Set<BlockEntity> blockEntities = new HashSet<>();
            loadedChunks.forEach(chunk -> blockEntities.addAll(chunk.getBlockEntities().values()));
            blockEntities.forEach(blockEntity -> {
                if (blockEntity.getType() == BlockEntityType.SIGN) {
                    SignBlockEntity sign = (SignBlockEntity) blockEntity;
                    Text[] signText = sign.getText(true).getMessages(false);
                    String[] text = new String[4];
                    for (int i = 0; i < 4; i++) text[i] = signText[i].getString();
                    signs.put(sign.getPos().toImmutable(), text);
                }
            });
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("dump_signs").executes(context -> {
            StringBuilder sb = new StringBuilder("\n[\n");
            signs.forEach((pos, arr) -> sb.append("{\"xyz\":\"").append(pos.toShortString()).append("\",\"text\":[\"").append(arr[0]).append("\",\"").append(arr[1]).append("\",\"").append(arr[2]).append("\",\"").append(arr[3]).append("\"]},\n"));
            System.out.println(sb.append("]"));

            return 1;
        })));

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> signs.clear());
    }
}