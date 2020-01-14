package com.griefdefender.api.claim;

import org.spongepowered.api.util.generator.dummy.DummyObjectProvider;

/**
 * The type Trust types.
 *
 * @author bloodmc
 */
public class TrustTypes {
    /**
     * The constant NONE.
     */
    public static final TrustType NONE = DummyObjectProvider.createFor(TrustType.class, "NONE");

    /**
     * The constant ACCESSOR.
     */
    public static final TrustType ACCESSOR = DummyObjectProvider.createFor(TrustType.class, "ACCESSOR");

    /**
     * The constant BUILDER.
     */
    public static final TrustType BUILDER = DummyObjectProvider.createFor(TrustType.class, "BUILDER");

    /**
     * The constant CONTAINER.
     */
    public static final TrustType CONTAINER = DummyObjectProvider.createFor(TrustType.class, "CONTAINER");

    /**
     * The constant MANAGER.
     */
    public static final TrustType MANAGER = DummyObjectProvider.createFor(TrustType.class, "MANAGER");
}
