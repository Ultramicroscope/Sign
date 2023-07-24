package wtf.ultra;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.impl.event.lifecycle.LoadedChunksCache;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class SignMod implements ModInitializer {
    private final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private final Map<BlockPos, String[]> signs = new TreeMap<>();

    @Override
    public void onInitialize() {
        ClientTickEvents.END_WORLD_TICK.register(world -> ((LoadedChunksCache) world).fabric_getLoadedChunks().stream().flatMap(chunk -> chunk.getBlockEntities().values().stream()).filter(blockEntity -> blockEntity instanceof SignBlockEntity).map(blockEntity -> (SignBlockEntity) blockEntity).forEach(sign -> signs.put(sign.getPos().toImmutable(), Arrays.stream(sign.getText(true).getMessages(false)).map(Text::getString).toArray(String[]::new))));
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> signs.clear());
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("dump_signs").executes(context -> {
            JsonObject jsonObject = new JsonObject();
            signs.forEach((pos, arr) -> jsonObject.add(pos.toShortString().replace(",", ""), GSON.toJsonTree(arr)));
            System.out.println(GSON.toJson(jsonObject));

            return 1;
        })));
    }
}