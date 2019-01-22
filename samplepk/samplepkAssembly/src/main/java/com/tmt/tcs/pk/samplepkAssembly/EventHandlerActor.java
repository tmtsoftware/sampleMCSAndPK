package com.tmt.tcs.pk.samplepkAssembly;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import csw.event.api.javadsl.IEventService;
import csw.logging.javadsl.ILogger;
import csw.logging.javadsl.JLoggerFactory;
import csw.params.core.generics.Key;
import csw.params.core.models.Prefix;
import csw.params.events.Event;
import csw.params.events.EventName;
import csw.params.events.SystemEvent;
import csw.params.javadsl.JKeyType;
import java.time.Instant;
import java.util.concurrent.*;
public class EventHandlerActor extends AbstractBehavior<EventHandlerActor.EventMessage> {
    private ActorContext<EventMessage> actorContext;
    private IEventService eventService;
    private JLoggerFactory loggerFactory;
    private ILogger log;

    private static final Prefix prefix = new Prefix("tcs.pk");

    private int counterMcs = 0;

    private static final int LIMIT = 100000;
    private Key<Double> azDoubleKey = JKeyType.DoubleKey().make("mcs.az");
    private Key<Double> elDoubleKey = JKeyType.DoubleKey().make("mcs.el");
    private Key<Instant> publishTimeKey = JKeyType.TimestampKey().make("timeStamp");

    private static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    private EventHandlerActor(ActorContext<EventMessage> actorContext, IEventService eventService, JLoggerFactory loggerFactory) {
        this.actorContext = actorContext;
        this.eventService = eventService;
        this.loggerFactory = loggerFactory;
        this.log = loggerFactory.getLogger(actorContext, getClass());

    }

    public static <EventMessage> Behavior<EventMessage> behavior(IEventService eventService, JLoggerFactory loggerFactory) {
        return Behaviors.setup(ctx -> {
            return (AbstractBehavior<EventMessage>) new EventHandlerActor((ActorContext<EventHandlerActor.EventMessage>) ctx, eventService, loggerFactory);
        });
    }
    private Runnable demandGenerator = new Runnable() {
        double az = 0.0000001;
        double el = 0.0000001;
        @Override
        public void run() {
            counterMcs++;
            if(counterMcs < 100000){
                az += 0.0000001;
                el += 0.0000001;
                Event event = new SystemEvent(prefix, new EventName("mcsdemandpositions"))
                        .add(azDoubleKey.set(az))
                        .add(elDoubleKey.set(el))
                        .add(publishTimeKey.set(Instant.now()));
                eventService.defaultPublisher().publish(event);
            }
        }
    };

    @Override
    public Receive<EventMessage> createReceive() {

        ReceiveBuilder<EventMessage> builder = receiveBuilder()
                .onMessage(McsDemandMessage.class,
                        message -> {
                            scheduledExecutorService.scheduleWithFixedDelay(demandGenerator,10,10,TimeUnit.MILLISECONDS);
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

        public McsDemandMessage(double az, double el){
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
