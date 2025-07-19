package com.mople.global.event.data.notify.handler;

import com.mople.core.exception.custom.BadRequestException;
import com.mople.dto.event.data.EventData;
import com.mople.global.enums.NotifyType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mople.global.enums.ExceptionReturnCode.NOT_FOUND_NOTIFY_TYPE;

@Component
@RequiredArgsConstructor
public class NotifyHandlerRegistry {

    private final List<NotifyHandler<? extends EventData>> handlers;
    private final Map<NotifyType, NotifyHandler<? extends EventData>> handlerMap = new HashMap<>();
    private final Map<NotifyType, Class<? extends EventData>> handledTypeMap = new HashMap<>();

    @PostConstruct
    public void init() {
        for (NotifyHandler<?> handler : handlers) {
            handlerMap.put(handler.getType(), handler);
            handledTypeMap.put(handler.getType(), handler.getHandledType());
        }
    }

    public NotifyHandler<? extends EventData> getHandler(NotifyType type) {
        NotifyHandler<? extends EventData> handler = handlerMap.get(type);

        if (handler == null) {
            throw new BadRequestException(NOT_FOUND_NOTIFY_TYPE);
        }

        return handler;
    }

    public Class<? extends EventData> getHandledType(NotifyType type) {
        return handledTypeMap.get(type);
    }
}
