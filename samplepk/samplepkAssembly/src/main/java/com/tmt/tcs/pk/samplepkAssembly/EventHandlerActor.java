package com.tmt.tcs.pk.samplepkAssembly;

import akka.actor.Cancellable;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import csw.event.api.javadsl.IEventService;
import csw.logging.javadsl.JLoggerFactory;
import csw.params.core.generics.Key;
import csw.params.core.models.Prefix;
import csw.params.events.Event;
import csw.params.events.EventName;
import csw.params.events.SystemEvent;
import csw.params.javadsl.JKeyType;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

public class EventHandlerActor extends AbstractBehavior<EventHandlerActor.EventMessage> {
    private IEventService eventService;

    private static final Prefix prefix = new Prefix("tcs.pk");
    private Cancellable cancellable;
    private int counterMcs = 0;

    private static final int LIMIT = 100000;
    private Key<Double> azDoubleKey = JKeyType.DoubleKey().make("mcs.az");
    private Key<Double> elDoubleKey = JKeyType.DoubleKey().make("mcs.el");
    private Key<Instant> publishTimeKey = JKeyType.TimestampKey().make("timeStamp");

    private EventHandlerActor(ActorContext<EventMessage> actorContext, IEventService eventService, JLoggerFactory loggerFactory) {
        this.eventService = eventService;

    }

    static <EventMessage> Behavior<EventMessage> behavior(IEventService eventService, JLoggerFactory loggerFactory) {
        return Behaviors.setup(ctx -> (AbstractBehavior<EventMessage>) new EventHandlerActor((ActorContext<EventHandlerActor.EventMessage>) ctx, eventService, loggerFactory));
    }

    private Supplier<Event> demandGenerator = new Supplier<Event>() {
        double az = 0.0000001;
        double el = 0.0000001;

        @Override
        public Event get() {
            counterMcs++;
            az += 0.0000001;
            el += 0.0000001;
            if (counterMcs < LIMIT) {
                return new SystemEvent(prefix, new EventName("mcsdemandpositions"))
                        .add(azDoubleKey.set(az))
                        .add(elDoubleKey.set(el))
                        .add(publishTimeKey.set(Instant.now()));
            }
            cancellable.cancel();
            return null;
        }
    };

    @Override
    public Receive<EventMessage> createReceive() {

        ReceiveBuilder<EventMessage> builder = receiveBuilder()
                .onMessage(McsDemandMessage.class,
                        message -> {
                            cancellable = eventService.defaultPublisher().publish(demandGenerator, Duration.ofMillis(10));
                            return Behaviors.same();
                        });

        return builder.build();
    }


    // add messages here
    public interface EventMessage {
    }

    public static final class McsDemandMessage implements EventMessage {
        private final double az;
        private final double el;

        public McsDemandMessage(double az, double el) {
            this.az = az;
            this.el = el;
        }

        public double getAz() {
            return az;
        }

        public double getEl() {
            return el;
        }
    }
}
