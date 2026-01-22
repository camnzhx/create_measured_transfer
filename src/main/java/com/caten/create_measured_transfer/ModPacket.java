package com.caten.create_measured_transfer;

import com.caten.create_measured_transfer.packet.MeteringBarrelActionPacket;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import static com.caten.create_measured_transfer.Create_measured_transfer.MODID;

public class ModPacket {

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MODID);

        // 注册 MeteringBarrelActionPacket
        registrar.playToServer(
                MeteringBarrelActionPacket.TYPE,
                MeteringBarrelActionPacket.STREAM_CODEC,
                MeteringBarrelActionPacket::handle
        );
    }

}
