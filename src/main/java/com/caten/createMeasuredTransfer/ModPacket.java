package com.caten.createMeasuredTransfer;

import com.caten.createMeasuredTransfer.packet.MeteringBarrelActionPacket;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import static com.caten.createMeasuredTransfer.CreateMeasuredTransfer.MOD_ID;

public class ModPacket {

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MOD_ID);

        // 注册 MeteringBarrelActionPacket
        registrar.playToServer(
                MeteringBarrelActionPacket.TYPE,
                MeteringBarrelActionPacket.STREAM_CODEC,
                MeteringBarrelActionPacket::handle
        );
    }

}
