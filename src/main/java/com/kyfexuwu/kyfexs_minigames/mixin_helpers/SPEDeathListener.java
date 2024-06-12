package com.kyfexuwu.kyfexs_minigames.mixin_helpers;

import java.util.function.Supplier;

public interface SPEDeathListener {
    void addDeathOrLogoutListener(Supplier<Boolean> listener);
}
