package de.myandres.optolink.viessmann;

import de.myandres.optolink.entity.Telegram;

import java.util.Optional;

public interface ViessmannHandler extends AutoCloseable {
    Optional<String> setValue(Telegram telegram, String value);

    Optional<String> getValue(Telegram telegram);
}
