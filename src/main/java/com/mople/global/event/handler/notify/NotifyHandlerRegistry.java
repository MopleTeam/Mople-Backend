package com.mople.global.event.handler.notify;

import com.mople.core.exception.custom.IllegalStatesException;
import com.mople.dto.event.data.notify.NotifyEvent;
import com.mople.global.event.handler.EventHandlerRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mople.global.enums.ExceptionReturnCode.MUST_SINGLE_HANDLER;

@Component
public class NotifyHandlerRegistry
        extends EventHandlerRegistry<NotifyEventHandler<? extends NotifyEvent>, NotifyEvent> {

    @Autowired
    public NotifyHandlerRegistry(List<NotifyEventHandler<? extends NotifyEvent>> handlers) {
        super(handlers, NotifyEventHandler::getHandledType);
    }

    public NotifyEventHandler<? extends NotifyEvent> getSingleHandler(NotifyEvent event) {
        List<NotifyEventHandler<? extends NotifyEvent>> handlers = super.getHandler(event);

        if (handlers.size() != 1) {
            throw new IllegalStatesException(MUST_SINGLE_HANDLER);
        }

        return handlers.get(0);
    }
}
