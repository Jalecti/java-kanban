package utils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class Constant {
    //используется в качестве значения по умолчанию при создании задач без времени
    public static final LocalDateTime UNIX_EPOCH_START = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
}
