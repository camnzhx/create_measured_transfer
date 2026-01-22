package com.caten.create_measured_transfer.packet;

import com.caten.create_measured_transfer.item.MeteringBarrelItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * 计量桶通用操作包
 * 支持多种按钮操作
 */
public record MeteringBarrelActionPacket(ActionType action, int value) implements CustomPacketPayload {

    public enum ActionType {
        CLEAR,          // 清空
        SET_CAPACITY,   // 设置容量
        SET_THRESHOLD   // 设置阈值
    }

    public static final Type<MeteringBarrelActionPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath("create_measured_transfer", "metering_barrel_action")
    );

    // StreamCodec：序列化枚举和整数值
    public static final StreamCodec<FriendlyByteBuf, MeteringBarrelActionPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(
                    ActionType::valueOf,
                    ActionType::name
            ),
            MeteringBarrelActionPacket::action,
            ByteBufCodecs.INT,
            MeteringBarrelActionPacket::value,
            MeteringBarrelActionPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // 便捷构造方法
    public static MeteringBarrelActionPacket clear() {
        return new MeteringBarrelActionPacket(ActionType.CLEAR, 0);
    }

    public static MeteringBarrelActionPacket setCapacity(int capacity) {
        return new MeteringBarrelActionPacket(ActionType.SET_CAPACITY, capacity);
    }

    public static MeteringBarrelActionPacket setThreshold(int threshold) {
        return new MeteringBarrelActionPacket(ActionType.SET_THRESHOLD, threshold);
    }

    // 处理所有操作
    public static void handle(MeteringBarrelActionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                ItemStack heldItem = serverPlayer.getMainHandItem();

                if (heldItem.getItem() instanceof MeteringBarrelItem) {
                    switch (packet.action()) {
                        case CLEAR -> MeteringBarrelItem.emptyFluid(heldItem);
                        case SET_CAPACITY -> MeteringBarrelItem.setCapacity(heldItem, packet.value());
                    }

                    // 同步更新
                    serverPlayer.containerMenu.broadcastChanges();
                }
            }
        });
    }

//    private static void handleSetThreshold(MeteringBarrelItem item, ItemStack stack, int threshold) {
//        // 设置阈值逻辑
//        item.setThreshold(stack, threshold);
//    }

}