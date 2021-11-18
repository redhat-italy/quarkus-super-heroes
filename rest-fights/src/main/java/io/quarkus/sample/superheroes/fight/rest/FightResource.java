package io.quarkus.sample.superheroes.fight.rest;

import static javax.ws.rs.core.MediaType.*;
import static org.eclipse.microprofile.openapi.annotations.enums.SchemaType.ARRAY;

import java.time.Duration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

import io.quarkus.sample.superheroes.fight.Fight;
import io.quarkus.sample.superheroes.fight.Fighters;
import io.quarkus.sample.superheroes.fight.client.Hero;
import io.quarkus.sample.superheroes.fight.config.FightConfig;
import io.quarkus.sample.superheroes.fight.service.FightService;

import io.smallrye.common.annotation.NonBlocking;
import io.smallrye.mutiny.Uni;

@Path("/api/fights")
@Produces(APPLICATION_JSON)
@Tag(name = "fights")
public class FightResource {
	private final Logger logger;
	private final FightService service;
	private final FightConfig fightConfig;

	public FightResource(Logger logger, FightService service, FightConfig fightConfig) {
		this.logger = logger;
		this.service = service;
		this.fightConfig = fightConfig;
	}

	private Uni<Fighters> addDelay(Uni<Fighters> fighters) {
		return fighters
			.onItem()
			.delayIt()
			.by(Duration.ofMillis(this.fightConfig.process().milliseconds()));
	}

	@GET
	@Path("/randomfighters")
	@Operation(summary = "Returns random fighters")
	@APIResponse(
		responseCode = "200",
		description = "Gets a random Hero and Villain fighter"
	)
	@Timeout(500)
	public Uni<Fighters> getRandomFighters() {
		return addDelay(
			this.service.findRandomFighters()
				.invoke(fighters -> this.logger.debugf("Got random fighters: %s", fighters))
		);
	}

	@GET
	@Operation(summary = "Returns all the fights")
	@APIResponse(
		responseCode = "200",
		description = "Gets all fights",
		content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Fight.class, type = ARRAY))
	)
	@APIResponse(
		responseCode = "204",
		description = "No Fights"
	)
	public Uni<Response> getAllFights() {
		return this.service.findAllFights()
			.map(fights -> {
				this.logger.debugf("Total number of fights: %d", fights.size());

				return !fights.isEmpty() ?
				       Response.ok(fights).build() :
				       Response.noContent().build();
			});
	}

	@GET
	@Path("/{id}")
	@Operation(summary = "Returns a fight for a given identifier")
	@APIResponse(
		responseCode = "200",
		description = "Gets a fight for a given id",
		content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = Hero.class))
	)
	@APIResponse(
		responseCode = "404",
		description = "The fight is not found for a given identifier"
	)
	public Uni<Response> getFight(@Parameter(name = "id", required = true) @PathParam("id") Long id) {
		return this.service.findFightById(id)
			.onItem().ifNotNull().transform(f -> {
				this.logger.debugf("Found fight: %s", f);
				return Response.ok(f).build();
			})
			.onItem().ifNull().continueWith(() -> {
				this.logger.debugf("No fight found with id %d", id);
				return Response.status(Status.NOT_FOUND).build();
			});
	}

	@POST
	@Consumes(APPLICATION_JSON)
	@Operation(summary = "Initiates a fight")
	@APIResponse(
		responseCode = "200",
		description = "The fight"
	)
	@APIResponse(
		responseCode = "400",
		description = "Invalid fighters passed in (or no request body found)"
	)
	public Uni<Fight> doFight(@NotNull @Valid Fighters fighters) {
		return this.service.persistFight(fighters);
	}

	@GET
	@Produces(TEXT_PLAIN)
	@Path("/hello")
	@Tag(name = "hello")
	@Operation(summary = "Ping hello")
	@APIResponse(
		responseCode = "200",
		description = "Ping hello"
	)
	@NonBlocking
	public String hello() {
		return "Hello Fight Resource";
	}
}