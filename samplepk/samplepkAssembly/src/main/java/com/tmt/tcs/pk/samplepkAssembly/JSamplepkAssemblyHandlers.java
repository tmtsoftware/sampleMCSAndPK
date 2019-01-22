package com.tmt.tcs.pk.samplepkAssembly;

import akka.actor.typed.javadsl.ActorContext;
import csw.command.client.messages.TopLevelActorMessage;
import csw.framework.javadsl.JComponentHandlers;
import csw.framework.models.JCswContext;
import csw.location.api.models.TrackingEvent;
import csw.logging.javadsl.ILogger;
import csw.params.commands.CommandResponse;
import csw.params.commands.ControlCommand;
import csw.location.api.javadsl.ILocationService;
import csw.command.client.models.framework.ComponentInfo;
import java.util.concurrent.CompletableFuture;
import csw.config.api.javadsl.IConfigClientService;
import csw.config.client.javadsl.JConfigClientFactory;
import akka.actor.typed.ActorRef;

/**
 * Domain specific logic should be written in below handlers.
 * This handlers gets invoked when component receives messages/commands from other component/entity.
 * For example, if one component sends Submit(Setup(args)) command to SamplepkHcd,
 * This will be first validated in the supervisor and then forwarded to Component TLA which first invokes validateCommand hook
 * and if validation is successful, then onSubmit hook gets invoked.
 * You can find more information on this here : https://tmtsoftware.github.io/csw/commons/framework.html
 */
public class JSamplepkAssemblyHandlers extends JComponentHandlers {

    private ILogger log;

    private ActorContext<TopLevelActorMessage> actorContext;
    private ILocationService locationService;
    private ComponentInfo componentInfo;
    //private IConfigClientService clientApi;
    //private IConfigClientService clientApi;

    private ActorRef<EventHandlerActor.EventMessage> eventHandlerActor;


    JSamplepkAssemblyHandlers(ActorContext<TopLevelActorMessage> ctx,JCswContext cswCtx) {
        super(ctx, cswCtx);
        this.log = cswCtx.loggerFactory().getLogger(getClass());
        this.actorContext = ctx;
        this.locationService = cswCtx.locationService();
        this.componentInfo = cswCtx.componentInfo();
        // Handle to the config client service
       // clientApi = JConfigClientFactory.clientApi(Adapter.toUntyped(actorContext.getSystem()), locationService);

        eventHandlerActor = ctx.spawnAnonymous(EventHandlerActor.behavior(cswCtx.eventService(), cswCtx.loggerFactory()));
    }

    @Override
    public CompletableFuture<Void> jInitialize() {
    log.info("Initializing samplepk assembly...");
        return CompletableFuture.runAsync(() -> log.debug("Inside JSamplepkAssemblyHandlers: initialize()"));
    }

    @Override
    public CompletableFuture<Void> jOnShutdown() {
        return CompletableFuture.runAsync(() -> log.debug("Inside JSamplepkAssemblyHandlers: onShutdown()"));
    }

    @Override
    public void onLocationTrackingEvent(TrackingEvent trackingEvent) {
        log.debug("Inside JSamplepkAssemblyHandlers: onLocationTrackingEvent()");
    }

    @Override
    public CommandResponse.ValidateCommandResponse validateCommand(ControlCommand controlCommand) {
        log.debug("Inside JSamplepkAssemblyHandlers: validateCommand()");
        return new CommandResponse.Accepted(controlCommand.runId());
    }

    @Override
    public CommandResponse.SubmitResponse onSubmit(ControlCommand controlCommand) {
        eventHandlerActor.tell(new EventHandlerActor.McsDemandMessage(189.64,80.00));
        return new CommandResponse.Completed(controlCommand.runId());
    }

    @Override
    public void onOneway(ControlCommand controlCommand) {

    }

    @Override
    public void onGoOffline() {

    }

    @Override
    public void onGoOnline() {

    }
}
