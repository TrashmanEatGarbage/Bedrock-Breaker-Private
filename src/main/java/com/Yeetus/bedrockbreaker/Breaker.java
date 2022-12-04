package com.Yeetus.bedrockbreaker;

import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;

public class Breaker extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        MeteorClient.EVENT_BUS.registerLambdaFactory("com.Yeetus.bedrockbreaker.modules.Breaker", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
        MeteorClient.EVENT_BUS.registerLambdaFactory("bedrockbreaker.utils", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

        LOG.info("Initializing bedrock breaker");
        Modules.get().add(new com.Yeetus.bedrockbreaker.modules.Breaker());
    }

    @Override
    public String getPackage() {
        return "com.yeetus.bedrockbreaker";
    }
}
