package com.caten.createMeasuredTransfer.item;

/**
 * 占位能力注册器（目前不做任何工作）。
 *
 * 我们尝试自动注册 item-side capability，但目标 API 在当前类路径中不可见。
 * 为避免编译失败，这里保留一个空的注册方法；之后可以用实际的注册实现替换。
 */
public class MeteringBarrelCapabilityRegistrar {
    public static void register() {
        // Intentionally left blank — best-effort registration can be implemented later when the
        // target NeoForge capability-registration API is available at compile time.
    }
}