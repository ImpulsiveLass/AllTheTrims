package com.bawnorton.allthetrims.forge.network;

import com.bawnorton.allthetrims.client.util.PaletteHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public class SyncTrimPacket {
    public static final String PROTOCOL_VERSION = "1";
    public static SimpleChannel CHANNEL;

    public static void registerNetwork() {
        CHANNEL = NetworkRegistry.newSimpleChannel(
                new Identifier("allthetrims", "main"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
        );

        int id = 0;
        CHANNEL.registerMessage(
                id++,
                SyncTrimPacket.class,
                SyncTrimPacket::encode,
                SyncTrimPacket::decode,
                SyncTrimPacket::handle
        );
    }
    private final ItemStack stack;

    public SyncTrimPacket(ItemStack stack) {
        this.stack = stack;
    }

    public static void encode(SyncTrimPacket pkt, PacketByteBuf buf) {
        buf.writeItemStack(pkt.stack);
    }

    public static SyncTrimPacket decode(PacketByteBuf buf) {
        return new SyncTrimPacket(buf.readItemStack());
    }

    public static void handle(SyncTrimPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Client-side only
            ItemStack stack = pkt.stack;
            if (stack.hasNbt() && stack.getNbt().contains("Trim", 10)) { // 10 = NbtCompound
                NbtCompound trimTag = stack.getNbt().getCompound("Trim");
                PaletteHelper.cacheFromNbt(stack, trimTag);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
