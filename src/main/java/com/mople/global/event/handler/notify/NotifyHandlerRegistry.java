package com.mople.global.event.handler.notify;

import com.mople.dto.event.data.notify.NotifyEvent;
import com.mople.global.event.handler.EventHandlerRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotifyHandlerRegistry
        extends EventHandlerRegistry<NotifyEventHandler<? extends NotifyEvent>, NotifyEvent> {

    @Autowired
    public NotifyHandlerRegistry(List<NotifyEventHandler<? extends NotifyEvent>> handlers) {
        super(handlers, NotifyEventHandler::getHandledType);
    }

    public NotifyEventHandler<? extends NotifyEvent> getHandler(NotifyEvent event) {
        return super.getHandler(event);
    }
}
